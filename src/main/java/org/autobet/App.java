/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.autobet;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.autobet.ai.ChancesApproximationBasedPlayer;
import org.autobet.ai.GoalBasedTeamRater;
import org.autobet.ai.Player;
import org.autobet.ai.PlayerEvaluator;
import org.autobet.ai.RandomPlayer;
import org.autobet.ai.TeamRaterStatsCollector;
import org.autobet.ai.TeamRatersStatsApproximation;
import org.autobet.ioc.DaggerMainComponent;
import org.autobet.ioc.DatabaseConnectionModule.DatabaseConnection;
import org.autobet.ioc.MainComponent;
import org.autobet.util.GamesProcessorDriver;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Double.isNaN;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Comparator.naturalOrder;
import static java.util.Objects.requireNonNull;
import static org.autobet.ImmutableCollectors.toImmutableMap;

public final class App
{
    private final Map<String, Command> commands = Stream.of(
            new LoadCommand(), new QueryCommand(), new StatsCalculatorCommand(), new PlayerEvaluatorCommand())
            .collect(toImmutableMap(Command::getName));

    @Parameter(names = {"--help", "-h"}, help = true)
    private boolean help;

    public static final void main(String[] args)
    {
        App app = new App();
        JCommander jc = new JCommander(app);
        for (Command command : app.commands.values()) {
            jc.addCommand(command.getName(), command);
        }

        jc.parse(args);

        if (app.help || jc.getParsedCommand() == null) {
            jc.usage();
        }
        else {
            app.start(jc);
        }
    }

    private void start(JCommander jc)
    {
        MainComponent mainComponent = DaggerMainComponent.create();
        try (DatabaseConnection connection = mainComponent.connectToDatabase()) {
            commands.get(jc.getParsedCommand()).go(mainComponent);
        }
    }

    @Parameters(commandDescription = "Load csv data into database")
    public static final class LoadCommand
            implements Command
    {
        @Parameter(description = "CSV files")
        private List<String> csvFiles;

        @Override
        public void go(MainComponent component)
        {
            Loader loader = new Loader();
            for (String csvFile : csvFiles) {
                long start = currentTimeMillis();
                System.out.println("loading: " + csvFile);
                try {
                    int load = loader.load(csvFile);
                    System.out.println("loaded " + load + " new objects in:" + (currentTimeMillis() - start) + "ms");
                }
                catch (DBException ex) {
                    System.out.println("Unable to load: " + csvFile);
                    ;
                    System.out.println(ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }

        @Override
        public String getName()
        {
            return "load";
        }
    }

    @Parameters(commandDescription = "Run query")
    public static final class QueryCommand
            implements Command
    {
        @Parameter(description = "query")
        private List<String> queries;

        @Override
        public void go(MainComponent component)
        {
            requireNonNull(queries, "queries is null");
            for (String query : queries) {
                Base.find(query, row -> {
                    System.out.println(row);
                    return true;
                });
            }
        }

        @Override
        public String getName()
        {
            return "query";
        }
    }

    @Parameters(commandDescription = "Calculate team rater statistics")
    public static final class StatsCalculatorCommand
            implements Command
    {
        @Parameter(
                names = {"-c", "--max-count"},
                description = "number of maximum games to to process (default: unlimited)")
        private int limit = -1;

        @Override
        public void go(MainComponent component)
        {
            GamesProcessorDriver gamesProcessorDriver = new GamesProcessorDriver(component);
            GoalBasedTeamRater teamRater = new GoalBasedTeamRater();
            TeamRaterStatsCollector statsCollector = new TeamRaterStatsCollector(gamesProcessorDriver, teamRater);
            long start = currentTimeMillis();
            TeamRaterStatsCollector.TeamRaterStats teamRaterStats = statsCollector.collect(getLimit());
            long end = currentTimeMillis();
            TeamRatersStatsApproximation approximation = new TeamRatersStatsApproximation(teamRaterStats);
            System.out.println("Stats collection took: " + (end - start) + "ms");

            System.out.println("Rate -     WINS      -     DRAWS     -    LOSES      - APPROX ERR");
            List<Integer> rates = teamRaterStats.getRates();
            int maxRate = rates.stream().max(naturalOrder()).orElse(0);
            int minRate = rates.stream().min(naturalOrder()).orElse(0);
            double totalApproximationError = 0;
            for (int rate = minRate; rate <= maxRate; rate++) {
                TeamRaterStatsCollector.RateStats stats = teamRaterStats.getHome(rate);
                double approximationHomeWinChances = approximation.getHomeWinChances(rate);
                double approximationHomeDrawChances = approximation.getHomeDrawChances(rate);
                double approximationHomeLoseChances = approximation.getHomeLoseChances(rate);
                double homeWinChances = (double) stats.getWins() / stats.getCount();
                double homeDrawChances = (double) stats.getDraws() / stats.getCount();
                double homeLoseChances = (double) stats.getLoses() / stats.getCount();
                double approximationError = 0;
                if (!isNaN(homeWinChances)) {
                    approximationError += Math.abs(approximationHomeWinChances - homeWinChances);
                }
                if (!isNaN(homeDrawChances)) {
                    approximationError += Math.abs(approximationHomeDrawChances - homeDrawChances);
                }
                if (!isNaN(homeLoseChances)) {
                    approximationError += Math.abs(approximationHomeLoseChances - homeLoseChances);
                }
                totalApproximationError += approximationError;
                System.out.println(format(
                        "%4d - %3d/%.2f/%.2f - %3d/%.2f/%.2f - %3d/%.2f/%.2f -   %.2f",
                        rate,
                        stats.getWins(), homeWinChances, approximationHomeWinChances,
                        stats.getDraws(), homeDrawChances, approximationHomeDrawChances,
                        stats.getLoses(), homeLoseChances, approximationHomeLoseChances,
                        approximationError));
            }
            System.out.println(format("Approximation error: %.2f", totalApproximationError));
        }

        private Optional<Integer> getLimit()
        {
            if (limit > 0) {
                return Optional.of(limit);
            }
            return Optional.empty();
        }

        @Override
        public String getName()
        {
            return "stats";
        }
    }

    @Parameters(commandDescription = "Evaluate the player strategy")
    public static final class PlayerEvaluatorCommand
            implements Command
    {
        @Parameter(
                names = {"-c", "--max-count"},
                description = "number of maximum games to to process (default: unlimited)")
        private int limit = -1;

        @Parameter(names = {"-s", "--strategy"}, description = "strategy to test (default: goal_based)")
        private String strategy = "goal_based";

        @Parameter(names = {"-l", "--list-strategies"}, description = "list available strategies", arity = 1)
        private boolean listStrategies = false;

        @Override
        public void go(MainComponent component)
        {
            if (listStrategies) {
                System.out.println("random, goal_based");
                return;
            }

            Player player;
            GamesProcessorDriver driver = new GamesProcessorDriver(component);
            switch (strategy) {
                case "goal_based":
                    GoalBasedTeamRater teamRater = new GoalBasedTeamRater();
                    //TODO: do not collect stats here
                    TeamRaterStatsCollector statsCollector = new TeamRaterStatsCollector(driver, teamRater);
                    TeamRaterStatsCollector.TeamRaterStats stats = statsCollector.collect(Optional.of(100));
                    TeamRatersStatsApproximation approximation = new TeamRatersStatsApproximation(stats);
                    player = new ChancesApproximationBasedPlayer(approximation, teamRater);
                    break;
                case "random":
                    player = new RandomPlayer();
                    break;
                default:
                    throw new IllegalStateException("Unknown player strategy: " + strategy);
            }

            PlayerEvaluator evaluator = new PlayerEvaluator(driver, player);
            PlayerEvaluator.Statistics evaluation = evaluator.evaluate(getLimit());
            System.out.println(format("Evaluation result: %.2f", evaluation.getResult()));
        }

        private Optional<Integer> getLimit()
        {
            if (limit > 0) {
                return Optional.of(limit);
            }
            return Optional.empty();
        }

        @Override
        public String getName()
        {
            return "eval";
        }
    }

    interface Command
    {
        void go(MainComponent component);

        String getName();
    }
}
