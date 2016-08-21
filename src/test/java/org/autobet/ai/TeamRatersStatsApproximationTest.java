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
        stats.incrementHome(-2, LOSE);
        stats.incrementHome(-2, LOSE);
        stats.incrementHome(-2, LOSE);
        stats.incrementHome(-2, LOSE);
        stats.incrementHome(-2, DRAW);
        stats.incrementHome(-2, DRAW);
        stats.incrementHome(-2, DRAW);

        stats.incrementHome(-1, LOSE);
        stats.incrementHome(-1, LOSE);
        stats.incrementHome(-1, LOSE);
        stats.incrementHome(-1, DRAW);
        stats.incrementHome(-1, DRAW);
        stats.incrementHome(-1, WIN);

        stats.incrementHome(0, LOSE);
        stats.incrementHome(0, LOSE);
        stats.incrementHome(0, DRAW);
        stats.incrementHome(0, DRAW);
        stats.incrementHome(0, DRAW);
        stats.incrementHome(0, WIN);
        stats.incrementHome(0, WIN);

        stats.incrementHome(1, LOSE);
        stats.incrementHome(1, DRAW);
        stats.incrementHome(1, DRAW);
        stats.incrementHome(1, WIN);
        stats.incrementHome(1, WIN);
        stats.incrementHome(1, WIN);

        stats.incrementHome(2, DRAW);
        stats.incrementHome(2, DRAW);
        stats.incrementHome(2, DRAW);
        stats.incrementHome(2, WIN);
        stats.incrementHome(2, WIN);
        stats.incrementHome(2, WIN);
        stats.incrementHome(2, WIN);

        TeamRatersStatsApproximation approximation = new TeamRatersStatsApproximation(stats.build());

        assertEquals(approximation.getHomeDrawChances(-2), 1, 0.01);
        assertEquals(approximation.getHomeLoseChances(-2), 0, 0.01);
        assertEquals(approximation.getHomeWinChances(-2), 0, 0.01);

        assertEquals(approximation.getHomeDrawChances(-1), 0.33, 0.01);
        assertEquals(approximation.getHomeLoseChances(-1), 0.52, 0.01);
        assertEquals(approximation.getHomeWinChances(-1), 0.15, 0.01);

        assertEquals(approximation.getHomeDrawChances(0), 0.42, 0.01);
        assertEquals(approximation.getHomeLoseChances(0), 0.28, 0.01);
        assertEquals(approximation.getHomeWinChances(0), 0.28, 0.01);

        assertEquals(approximation.getHomeDrawChances(1), 0.33, 0.01);
        assertEquals(approximation.getHomeLoseChances(1), 0.16, 0.01);
        assertEquals(approximation.getHomeWinChances(1), 0.5, 0.01);

        assertEquals(approximation.getHomeDrawChances(2), 1, 0.01);
        assertEquals(approximation.getHomeLoseChances(2), 0, 0.01);
        assertEquals(approximation.getHomeWinChances(2), 0, 0.01);
    }
}
