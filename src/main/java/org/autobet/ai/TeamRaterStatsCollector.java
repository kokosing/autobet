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

package org.autobet.ai;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.autobet.ioc.MainComponent;
import org.autobet.model.Game;
import org.autobet.model.Team;
import org.autobet.ui.ProgressBar;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;

import java.sql.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.autobet.ImmutableCollectors.toImmutableList;

public class TeamRaterStatsCollector
{
    public enum GameResult
    {
        WIN, DRAW, LOSE
    }

    public TeamRaterStats collect(TeamRater teamRater, long limit, MainComponent component)
    {
        long count = Game.count();
        if (limit > 0) {
            count = limit;
        }
        ProgressBar progressBar = new ProgressBar(count, "games");

        Iterator<Model> games = Game.findAll().iterator();
        List<CompletableFuture<TeamRaterStats>> futures = IntStream.range(0, Runtime.getRuntime().availableProcessors())
                .mapToObj(i -> supplyAsync(() -> {
                    Base.open(component.getDataSource());
                    try {
                        TeamRaterStats.Builder stats = TeamRaterStats.builder();
                        Game game;
                        while (true) {
                            synchronized (games) {
                                if (games.hasNext()) {
                                    game = (Game) games.next();
                                }
                                else {
                                    break;
                                }
                            }
                            evaluateGame(teamRater, stats, game);
                            progressBar.increment();
                        }
                        return stats.build();
                    }
                    finally {
                        Base.close();
                    }
                })).collect(toImmutableList());

        TeamRaterStats stats = TeamRaterStats.create();
        for (CompletableFuture<TeamRaterStats> future : futures) {
            try {
                stats = stats.merge(future.get());
            }
            catch (InterruptedException | ExecutionException e) {
                throw Throwables.propagate(e);
            }
        }
        return stats;
    }

    private void evaluateGame(TeamRater teamRater, TeamRaterStats.Builder stats, Game game)
    {
        Team homeTeam = Team.findById(game.getLong("home_team_id"));
        Team awayTeam = Team.findById(game.getLong("away_team_id"));
        Date playedAt = game.getDate("played_at");

        Optional<Integer> homeTeamRate = teamRater.rate(homeTeam, playedAt);
        Optional<Integer> awayTeamRate = teamRater.rate(awayTeam, playedAt);

        if (homeTeamRate.isPresent() && awayTeamRate.isPresent()) {
            int rateDiff = homeTeamRate.get() - awayTeamRate.get();

            String fullTimeResult = game.getString("full_time_result");
            switch (fullTimeResult) {
                case "H":
                    stats.incrementHome(rateDiff, GameResult.WIN);
                    break;
                case "D":
                    stats.incrementHome(rateDiff, GameResult.DRAW);
                    break;
                case "A":
                    stats.incrementHome(rateDiff, GameResult.LOSE);
                    break;

                default:
                    throw new IllegalStateException("Unknown full time game result: " + fullTimeResult);
            }
        }
    }

    public static class TeamRaterStats
    {
        private final Map<Integer, RateStats> homeStats;

        public static TeamRaterStats create()
        {
            return builder().build();
        }

        public static Builder builder()
        {
            return new Builder();
        }

        private TeamRaterStats(Map<Integer, RateStats> homeStats)
        {
            this.homeStats = ImmutableMap.copyOf(homeStats);
        }

        public Optional<RateStats> getHome(int rate)
        {
            return Optional.ofNullable(homeStats.get(rate));
        }

        public Map<Integer, RateStats> getHomeStats()
        {
            return ImmutableMap.copyOf(homeStats);
        }

        public List<Integer> getRates()
        {
            return homeStats.keySet()
                    .stream()
                    .sorted()
                    .collect(toImmutableList());
        }

        public TeamRaterStats merge(TeamRaterStats other)
        {
            TeamRaterStats.Builder builder = builder();
            for (TeamRaterStats stats : ImmutableList.of(this, other)) {
                for (int rate : stats.homeStats.keySet()) {
                    RateStats rateStats = stats.homeStats.get(rate);
                    for (GameResult gameResult : GameResult.values()) {
                        builder.addHome(rate, gameResult, rateStats.get(gameResult));
                    }
                }
            }
            return builder.build();
        }

        public static class Builder
        {
            private final Map<Integer, RateStats> homeStats = new HashMap<>();

            public Builder incrementHome(int rate, GameResult gameResult)
            {
                return addHome(rate, gameResult, 1);
            }

            public Builder addHome(int rate, GameResult gameResult, int count)
            {
                RateStats value = RateStats.builder().add(gameResult, count).build();
                homeStats.merge(rate, value, (left, right) -> left.merge(right));
                return this;
            }

            public TeamRaterStats build()
            {
                return new TeamRaterStats(homeStats);
            }
        }
    }

    public static class RateStats
    {
        public static RateStats create()
        {
            return builder().build();
        }

        public static Builder builder()
        {
            return new Builder();
        }

        private final Map<GameResult, Integer> stats;

        public RateStats(Map<GameResult, Integer> stats)
        {
            this.stats = ImmutableMap.copyOf(stats);
        }

        public int getCount()
        {
            return stats.values().stream()
                    .reduce((x, y) -> x + y)
                    .get();
        }

        public int getWins()
        {
            return get(GameResult.WIN);
        }

        public int getDraws()
        {
            return get(GameResult.DRAW);
        }

        public int getLoses()
        {
            return get(GameResult.LOSE);
        }

        public int get(GameResult gameResult)
        {
            Integer gameResultStats = stats.get(gameResult);
            return gameResultStats == null ? 0 : gameResultStats;
        }

        public RateStats merge(RateStats other)
        {
            Builder builder = builder();
            for (RateStats rateStats : ImmutableList.of(this, other)) {
                for (GameResult gameResult : GameResult.values()) {
                    builder.add(gameResult, rateStats.get(gameResult));
                }
            }
            return builder.build();
        }

        public static class Builder
        {
            private Map<GameResult, Integer> stats = new HashMap<>();

            private Builder add(GameResult result, int count)
            {
                stats.merge(result, count, (left, right) -> left + right);
                return this;
            }

            public RateStats build()
            {
                return new RateStats(stats);
            }
        }
    }
}
