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

import org.junit.Assert;
import org.junit.Test;

import static org.autobet.ai.TeamRaterStatsCollector.GameResult.DRAW;
import static org.autobet.ai.TeamRaterStatsCollector.GameResult.LOSE;
import static org.autobet.ai.TeamRaterStatsCollector.GameResult.WIN;
import static org.junit.Assert.assertEquals;

public class TeamRatersStatsApproximationTest
{
    @Test
    public void test()
    {
        TeamRaterStatsCollector.TeamRaterStats.Builder stats = TeamRaterStatsCollector.TeamRaterStats.builder();
        stats.incrementAway(-2, LOSE);
        stats.incrementAway(-2, LOSE);
        stats.incrementAway(-2, LOSE);
        stats.incrementAway(-2, LOSE);
        stats.incrementAway(-2, DRAW);
        stats.incrementAway(-2, DRAW);
        stats.incrementAway(-2, DRAW);

        stats.incrementAway(-1, LOSE);
        stats.incrementAway(-1, LOSE);
        stats.incrementAway(-1, LOSE);
        stats.incrementAway(-1, DRAW);
        stats.incrementAway(-1, DRAW);
        stats.incrementAway(-1, WIN);

        stats.incrementAway(0, LOSE);
        stats.incrementAway(0, LOSE);
        stats.incrementAway(0, DRAW);
        stats.incrementAway(0, DRAW);
        stats.incrementAway(0, DRAW);
        stats.incrementAway(0, WIN);
        stats.incrementAway(0, WIN);

        stats.incrementAway(1, LOSE);
        stats.incrementAway(1, DRAW);
        stats.incrementAway(1, DRAW);
        stats.incrementAway(1, WIN);
        stats.incrementAway(1, WIN);
        stats.incrementAway(1, WIN);

        stats.incrementAway(2, DRAW);
        stats.incrementAway(2, DRAW);
        stats.incrementAway(2, DRAW);
        stats.incrementAway(2, WIN);
        stats.incrementAway(2, WIN);
        stats.incrementAway(2, WIN);
        stats.incrementAway(2, WIN);

        stats.incrementHome(2, DRAW);
        stats.incrementHome(2, WIN);
        stats.incrementHome(2, LOSE);

        TeamRatersStatsApproximation approximation = new TeamRatersStatsApproximation(stats.build());

        assertEquals(approximation.getAwayDrawChances(-2), 1, 0.01);
        assertEquals(approximation.getAwayLoseChances(-2), 0, 0.01);
        assertEquals(approximation.getAwayWinChances(-2), 0, 0.01);

        assertEquals(approximation.getAwayDrawChances(-1), 0.33, 0.01);
        assertEquals(approximation.getAwayLoseChances(-1), 0.52, 0.01);
        assertEquals(approximation.getAwayWinChances(-1), 0.15, 0.01);

        assertEquals(approximation.getAwayDrawChances(0), 0.42, 0.01);
        assertEquals(approximation.getAwayLoseChances(0), 0.28, 0.01);
        assertEquals(approximation.getAwayWinChances(0), 0.28, 0.01);

        assertEquals(approximation.getAwayDrawChances(1), 0.33, 0.01);
        assertEquals(approximation.getAwayLoseChances(1), 0.16, 0.01);
        assertEquals(approximation.getAwayWinChances(1), 0.5, 0.01);

        assertEquals(approximation.getAwayDrawChances(2), 1, 0.01);
        assertEquals(approximation.getAwayLoseChances(2), 0, 0.01);
        assertEquals(approximation.getAwayWinChances(2), 0, 0.01);
    }
}
