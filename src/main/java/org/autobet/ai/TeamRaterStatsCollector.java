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

import com.google.common.collect.ImmutableMap;
import org.autobet.model.Game;
import org.autobet.model.Team;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TeamRaterStatsCollector
{
    public TeamRaterStats collect(TeamRater teamRater)
    {
        TeamRaterStatsBuilder statsBuilder = new TeamRaterStatsBuilder();
        List<Game> games = Game.findAll();
        for (Game game : games) {
            Team homeTeam = Team.findById(game.getLong("home_team_id"));
            Team awayTeam = Team.findById(game.getLong("away_team_id"));
            Date playedAt = game.getDate("played_at");

            Optional<Integer> homeTeamRate = teamRater.rate(homeTeam, playedAt);
            Optional<Integer> awayTeamRate = teamRater.rate(awayTeam, playedAt);
            String fullTimeResult = game.getString("full_time_result");

            switch (fullTimeResult) {
                case "H":
                    homeTeamRate.ifPresent(statsBuilder::incrementHomeWin);
                    awayTeamRate.ifPresent(statsBuilder::incrementAwayLose);
                    break;
                case "D":
                    homeTeamRate.ifPresent(statsBuilder::incrementHomeDraw);
                    awayTeamRate.ifPresent(statsBuilder::incrementAwayDraw);
                    break;
                case "A":
                    homeTeamRate.ifPresent(statsBuilder::incrementHomeLose);
                    awayTeamRate.ifPresent(statsBuilder::incrementAwayWin);
                    break;

                default:
                    throw new IllegalStateException("Unknown full time game result: " + fullTimeResult);
            }
        }
        return statsBuilder.build();
    }

    private static class TeamRaterStatsBuilder
    {
        private final Map<Integer, Integer> homeLoses = new HashMap<>();
        private final Map<Integer, Integer> homeWins = new HashMap<>();
        private final Map<Integer, Integer> homeDraws = new HashMap<>();
        private final Map<Integer, Integer> awayLoses = new HashMap<>();
        private final Map<Integer, Integer> awayDraws = new HashMap<>();
        private final Map<Integer, Integer> awayWins = new HashMap<>();

        public void incrementHomeLose(int rate)
        {
            increment(homeLoses, rate);
        }

        public void incrementHomeDraw(int rate)
        {
            increment(homeDraws, rate);
        }

        public void incrementHomeWin(int rate)
        {
            increment(homeWins, rate);
        }

        public void incrementAwayLose(int rate)
        {
            increment(awayLoses, rate);
        }

        public void incrementAwayDraw(int rate)
        {
            increment(awayDraws, rate);
        }

        public void incrementAwayWin(int rate)
        {
            increment(awayWins, rate);
        }

        private Integer increment(Map<Integer, Integer> stats, int rate)
        {
            return stats.compute(rate, TeamRaterStatsBuilder::increment);
        }

        private static int increment(Integer key, Integer value)
        {
            return value != null ? value + 1 : 1;
        }

        public TeamRaterStats build() {
            //TODO return stats
            return new TeamRaterStats();
        }
    }

    public static class TeamRaterStats
    {

    }
}
