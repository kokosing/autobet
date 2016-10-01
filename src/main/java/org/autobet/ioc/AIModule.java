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

package org.autobet.ioc;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoSet;
import org.autobet.ai.ChancesApproximationBasedPlayer;
import org.autobet.ai.GoalBasedTeamRater;
import org.autobet.ai.LowBetPlayer;
import org.autobet.ai.Player;
import org.autobet.ai.PlayerEvaluator;
import org.autobet.ai.RandomPlayer;
import org.autobet.ai.TeamRater;
import org.autobet.ai.TeamRaterStatsCollector;
import org.autobet.ai.TeamRatersStatsApproximation;
import org.autobet.util.GamesProcessorDriver;

import javax.inject.Inject;
import javax.sql.DataSource;

import java.util.Optional;
import java.util.Set;

import static org.autobet.ImmutableCollectors.toImmutableSet;

@Module
public class AIModule
{
    @Provides
    @Inject
    public GamesProcessorDriver provideDriver(DataSource dataSource)
    {
        return new GamesProcessorDriver(dataSource);
    }

    @Provides
    @Inject
    public TeamRaterStatsCollector provideTeamRaterStatsCollector(GamesProcessorDriver driver)
    {
        return new TeamRaterStatsCollector(driver);
    }

    @Provides
    @Inject
    public PlayerEvaluator providePlayerEvaluator(GamesProcessorDriver driver)
    {
        return new PlayerEvaluator(driver);
    }

    @Provides
    @IntoSet
    public Player provideRandomPlayer()
    {
        return new RandomPlayer();
    }

    @Provides
    @IntoSet
    public Player provideLowBetPlayer()
    {
        return new LowBetPlayer();
    }

    @Provides
    @ElementsIntoSet
    @Inject
    public Set<Player> provideChancesBasedPlayers(TeamRaterStatsCollector statsCollector, Set<TeamRater> teamRaters)
    {
        return teamRaters.stream()
                .map(teamRater -> {
                    //TODO: do not collect stats here
                    TeamRaterStatsCollector.TeamRaterStats stats = statsCollector.collect(
                            teamRater,
                            Optional.of(100),
                            Optional.empty());
                    TeamRatersStatsApproximation approximation = new TeamRatersStatsApproximation(stats);
                    return new ChancesApproximationBasedPlayer(approximation, teamRater);
                })
                .collect(toImmutableSet());
    }

    @Provides
    @IntoSet
    public TeamRater provideGoalBasedRater()
    {
        return new GoalBasedTeamRater();
    }
}
