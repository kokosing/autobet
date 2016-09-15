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
        TeamRaterStatsCollector.TeamRaterStats.Builder stats = TeamRaterStatsCollector.TeamRaterStats.builder("");
        stats.addHome(-2, LOSE, 4);
        stats.addHome(-2, DRAW, 3);

        stats.addHome(-1, LOSE, 3);
        stats.addHome(-1, DRAW, 2);
        stats.incrementHome(-1, WIN);

        stats.addHome(0, LOSE, 2);
        stats.addHome(0, DRAW, 3);
        stats.addHome(0, WIN, 2);

        stats.incrementHome(1, LOSE);
        stats.addHome(1, DRAW, 2);
        stats.addHome(1, WIN, 3);

        stats.addHome(2, DRAW, 3);
        stats.addHome(2, WIN, 4);

        TeamRatersStatsApproximation approximation = new TeamRatersStatsApproximation(stats.build());

        assertEquals(approximation.getDrawChances(-2), 0.4, 0.01);
        assertEquals(approximation.getHomeLoseChances(-2), 0.6, 0.01);
        assertEquals(approximation.getHomeWinChances(-2), 0, 0.01);

        assertEquals(approximation.getDrawChances(-1), 0.4, 0.01);
        assertEquals(approximation.getHomeLoseChances(-1), 0.45, 0.01);
        assertEquals(approximation.getHomeWinChances(-1), 0.15, 0.01);

        assertEquals(approximation.getDrawChances(0), 0.39, 0.01);
        assertEquals(approximation.getHomeLoseChances(0), 0.30, 0.01);
        assertEquals(approximation.getHomeWinChances(0), 0.30, 0.01);

        assertEquals(approximation.getDrawChances(1), 0.39, 0.01);
        assertEquals(approximation.getHomeLoseChances(1), 0.16, 0.01);
        assertEquals(approximation.getHomeWinChances(1), 0.45, 0.01);

        assertEquals(approximation.getDrawChances(2), 0.39, 0.01);
        assertEquals(approximation.getHomeLoseChances(2), 0, 0.01);
        assertEquals(approximation.getHomeWinChances(2), 0.6, 0.01);
    }
}
