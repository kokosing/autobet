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
import org.autobet.model.Team;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Date;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AITest
{
    public static final TemporaryDatabase temporaryDatabase = TemporaryDatabase.loaded();

    @BeforeClass
    public static void setUp()
            throws Throwable
    {
        temporaryDatabase.before();
    }

    @AfterClass
    public static void tearDown()
            throws Throwable
    {
        temporaryDatabase.after();
    }

    @Test
    public void evaluateRandomPlayer()
    {
        PlayerEvaluator evaluator = new PlayerEvaluator();

        RandomPlayer player = new RandomPlayer();
        double evaluation = evaluator.evaluate(player, Date.valueOf("2000-01-01"), Date.valueOf("2000-12-31"));

        assertTrue(evaluation < 100);
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
        TeamRaterStatsCollector statsCollector = new TeamRaterStatsCollector();

        TeamRaterStatsCollector.TeamRaterStats raterStats = statsCollector.collect(new GoalBasedTeamRater());

        assertFalse(raterStats.getAway(1000).isPresent());

        assertEquals(raterStats.getAway(0).get().getCount(), 16);
        assertEquals(raterStats.getAway(0).get().getWins(), 3);
        assertEquals(raterStats.getAway(0).get().getLoses(), 9);
        assertEquals(raterStats.getAway(0).get().getDraws(), 4);

        assertEquals(raterStats.getHome(0).get().getCount(), 16);
        assertEquals(raterStats.getHome(0).get().getWins(), 9);
        assertEquals(raterStats.getHome(0).get().getLoses(), 3);
        assertEquals(raterStats.getHome(0).get().getDraws(), 4);

        TeamRatersStatsApproximation approximation = new TeamRatersStatsApproximation(raterStats);
        assertEquals(approximation.getHomeWinChances(0), 0.43, 0.01);
        assertEquals(approximation.getHomeLoseChances(0), 0.27, 0.01);
        assertEquals(approximation.getHomeDrawChances(0), 0.29, 0.01);
        assertEquals(approximation.getAwayWinChances(0), 0.27, 0.01);
        assertEquals(approximation.getAwayLoseChances(0), 0.43, 0.01);
        assertEquals(approximation.getAwayDrawChances(0), 0.29, 0.01);

        // always win
        assertEquals(approximation.getHomeWinChances(100), 0, 0.01);
        assertEquals(approximation.getHomeLoseChances(100), 0, 0.01);
        assertEquals(approximation.getHomeDrawChances(100), 1, 0.01);
        assertEquals(approximation.getAwayWinChances(100), 0, 0.01);
        assertEquals(approximation.getAwayLoseChances(100), 0, 0.01);
        assertEquals(approximation.getAwayDrawChances(100), 1, 0.01);

        // always lose
        assertEquals(approximation.getHomeWinChances(-100), 0, 0.01);
        assertEquals(approximation.getHomeLoseChances(-100), 0, 0.01);
        assertEquals(approximation.getHomeDrawChances(-100), 1, 0.01);
        assertEquals(approximation.getAwayWinChances(-100), 0, 0.01);
        assertEquals(approximation.getAwayLoseChances(-100), 0, 0.01);
        assertEquals(approximation.getAwayDrawChances(-100), 1, 0.01);
    }
}
