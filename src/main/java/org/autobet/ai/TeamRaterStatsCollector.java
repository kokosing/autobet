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
    public enum GameResult
    {
        WIN, DRAW, LOSE;
    }

    public TeamRaterStats collect(TeamRater teamRater)
    {
        TeamRaterStats.Builder stats = TeamRaterStats.builder();
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
                    homeTeamRate.ifPresent(rate -> stats.incrementHome(rate, GameResult.WIN));
                    awayTeamRate.ifPresent(rate -> stats.incrementAway(rate, GameResult.LOSE));
                    break;
                case "D":
                    homeTeamRate.ifPresent(rate -> stats.incrementHome(rate, GameResult.DRAW));
                    awayTeamRate.ifPresent(rate -> stats.incrementAway(rate, GameResult.DRAW));
                    break;
                case "A":
                    homeTeamRate.ifPresent(rate -> stats.incrementHome(rate, GameResult.LOSE));
                    awayTeamRate.ifPresent(rate -> stats.incrementAway(rate, GameResult.WIN));
                    break;

                default:
                    throw new IllegalStateException("Unknown full time game result: " + fullTimeResult);
            }
        }
        return stats.build();
    }

    public static class TeamRaterStats
    {
        private final Map<Integer, RateStats> homeStats = new HashMap<>();
        private final Map<Integer, RateStats> awayStats = new HashMap<>();

        public static Builder builder() {
            return new TeamRaterStats().new Builder();
        }

        private TeamRaterStats() {
        }

        public Optional<RateStats> getAway(int rate)
        {
            return Optional.ofNullable(awayStats.get(rate));
        }

        public Optional<RateStats> getHome(int rate)
        {
            return Optional.ofNullable(homeStats.get(rate));
        }

        public Map<Integer, RateStats> getAwayStats()
        {
            return ImmutableMap.copyOf(awayStats);
        }

        public Map<Integer, RateStats> getHomeStats()
        {
            return ImmutableMap.copyOf(homeStats);
        }

        public class Builder {
            public void incrementHome(int rate, GameResult gameResult)
            {
                increment(homeStats, rate, gameResult);
            }

            public void incrementAway(int rate, GameResult gameResult)
            {
                increment(awayStats, rate, gameResult);
            }

            private void increment(Map<Integer, RateStats> stats, int rate, GameResult gameResult)
            {
                if (!stats.containsKey(rate)) {
                    stats.put(rate, new RateStats());
                }
                stats.get(rate).increment(gameResult);
            }

            public TeamRaterStats build() {
                return TeamRaterStats.this;
            }
        }
    }

    public static class RateStats
    {
        private Map<GameResult, Integer> stats = new HashMap<>();

        private void increment(GameResult result)
        {
            stats.compute(result, TeamRaterStatsCollector::increment);
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
    }

    private static <T> int increment(T key, Integer value)
    {
        return value != null ? value + 1 : 1;
    }
}
