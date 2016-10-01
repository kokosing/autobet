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

import org.autobet.TemporaryDatabase;
import org.autobet.ai.TeamRaterStatsCollector.TeamRaterStats;
import org.autobet.model.Team;
import org.junit.ClassRule;
import org.junit.Test;

import java.sql.Date;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AITest
{
    @ClassRule
    public static final TemporaryDatabase temporaryDatabase = TemporaryDatabase.loaded();

    @Test
    public void evaluateRandomPlayer()
    {
        PlayerEvaluator.Statistics evaluation = evaluate(new RandomPlayer());

        assertTrue(evaluation.getResult() < 100);
    }

    @Test
    public void evaluateLowBetPlayer()
    {
        PlayerEvaluator.Statistics evaluation = evaluate(new LowBetPlayer());

        assertTrue(evaluation.getResult() < 100);
    }

    private PlayerEvaluator.Statistics evaluate(Player player)
    {
        PlayerEvaluator evaluator = temporaryDatabase.getComponent().getPlayerEvaluator();

        return evaluator.evaluate(player, Optional.of(100), Optional.empty());
    }

    @Test
    public void goalBasedTeamRater()
    {
        GoalBasedTeamRater rater = new GoalBasedTeamRater();

        Team team = (Team) Team.findAll().get(0);
        assertFalse(rater.rate(team, Date.valueOf("2000-01-01")).isPresent());
        Optional<Integer> rating = rater.rate(team, Date.valueOf("2000-12-31"));
        assertTrue(rating.isPresent());
        assertEquals((int) rating.get(), -3);
    }

    @Test
    public void testStats()
    {
        GoalBasedTeamRater teamRater = new GoalBasedTeamRater();
        TeamRaterStatsCollector statsCollector = temporaryDatabase.getComponent().getStatsCollector();

        TeamRaterStats raterStats = statsCollector.collect(teamRater, Optional.of(100), Optional.empty());
        assertEquals(raterStats.getCount(), 46);

        raterStats = statsCollector.collect(teamRater, Optional.of(300), Optional.empty());
        assertEquals(raterStats.getCount(), 252);

        assertEquals(raterStats.getHome(1000).getCount(), 0);

        assertEquals(raterStats.getHome(0).getCount(), 14);
        assertEquals(raterStats.getHome(0).getWins(), 3);
        assertEquals(raterStats.getHome(0).getLoses(), 6);
        assertEquals(raterStats.getHome(0).getDraws(), 5);

        TeamRatersStatsApproximation approximation = new TeamRatersStatsApproximation(raterStats);
        assertEquals(approximation.getHomeWinChances(-10), 0.33, 0.01);
        assertEquals(approximation.getHomeLoseChances(-10), 0.4, 0.01);
        assertEquals(approximation.getDrawChances(-10), 0.28, 0.01);

        assertEquals(approximation.getHomeWinChances(-3), 0.44, 0.01);
        assertEquals(approximation.getHomeLoseChances(-3), 0.3, 0.01);
        assertEquals(approximation.getDrawChances(-3), 0.26, 0.01);

        assertEquals(approximation.getHomeWinChances(0), 0.49, 0.01);
        assertEquals(approximation.getHomeLoseChances(0), 0.26, 0.01);
        assertEquals(approximation.getDrawChances(0), 0.25, 0.01);

        assertEquals(approximation.getHomeWinChances(3), 0.54, 0.01);
        assertEquals(approximation.getHomeLoseChances(3), 0.22, 0.01);
        assertEquals(approximation.getDrawChances(3), 0.25, 0.01);

        assertEquals(approximation.getHomeWinChances(10), 0.65, 0.01);
        assertEquals(approximation.getHomeLoseChances(10), 0.12, 0.01);
        assertEquals(approximation.getDrawChances(10), 0.23, 0.01);

        ChancesApproximationBasedPlayer player = new ChancesApproximationBasedPlayer(approximation, teamRater);
        PlayerEvaluator playerEvaluator = temporaryDatabase.getComponent().getPlayerEvaluator();
        PlayerEvaluator.Statistics playerStats = playerEvaluator.evaluate(player, Optional.empty(), Optional.empty());
        assertEquals(playerStats.getResult(), -115.05, 0.01);
        assertEquals(playerStats.getBetsCount(), 3600);
        assertEquals(playerStats.getPlayedBetsCount(), 187);
        assertEquals(playerStats.getWinningBetsCount(), 18);
    }
}
