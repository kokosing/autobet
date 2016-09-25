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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.autobet.model.Game;
import org.autobet.util.GamesProcessorDriver;
import org.autobet.util.KeyValueStore;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static org.autobet.ImmutableCollectors.toImmutableList;

public class TeamRaterStatsCollector
{
    public enum GameResult
    {
        WIN, DRAW, LOSE
    }

    private final GamesProcessorDriver gamesProcessorDriver;

    public TeamRaterStatsCollector(GamesProcessorDriver gamesProcessorDriver)
    {
        this.gamesProcessorDriver = gamesProcessorDriver;
    }

    public TeamRaterStats collect(TeamRater teamRater, Optional<Integer> gamesLimit, Optional<Duration> timeLimit)
    {
        return gamesProcessorDriver.driveProcessors(() -> new GameProcessor(teamRater), gamesLimit, timeLimit);
    }

    private static class GameProcessor
            implements GamesProcessorDriver.GamesProcessor<TeamRaterStats>
    {
        private final TeamRater teamRater;
        private final TeamRaterStats.Builder builder;

        public GameProcessor(TeamRater teamRater)
        {
            this.teamRater = teamRater;
            this.builder = TeamRaterStats.builder(teamRater.getName());
        }

        @Override
        public void process(Game game)
        {
            String fullTimeResult = game.getString("full_time_result");
            if (fullTimeResult == null) {
                return;
            }

            Optional<Integer> rate = teamRater.rate(game);
            if (rate.isPresent()) {
                switch (fullTimeResult) {
                    case "H":
                        builder.incrementHome(rate.get(), GameResult.WIN);
                        break;
                    case "D":
                        builder.incrementHome(rate.get(), GameResult.DRAW);
                        break;
                    case "A":
                        builder.incrementHome(rate.get(), GameResult.LOSE);
                        break;

                    default:
                        throw new IllegalStateException("Unknown full time game result: " + fullTimeResult);
                }
            }
        }

        @Override
        public TeamRaterStats finish()
        {
            return builder.build();
        }
    }

    public static class TeamRaterStats
            implements KeyValueStore.Storable<TeamRaterStats>
    {
        private final Map<Integer, RateStats> homeStats;
        private final String storageKey;

        public static Builder builder(String storageKey)
        {
            return new Builder(storageKey);
        }

        @JsonCreator
        private TeamRaterStats(
                @JsonProperty("storageKey") String storageKey,
                @JsonProperty("homeStats") Map<Integer, RateStats> homeStats)
        {
            this.storageKey = storageKey;
            this.homeStats = ImmutableMap.copyOf(homeStats);
        }

        public RateStats getHome(int rate)
        {
            if (homeStats.containsKey(rate)) {
                return homeStats.get(rate);
            }
            return new RateStats(ImmutableMap.of());
        }

        @JsonProperty("homeStats")
        public Map<Integer, RateStats> getHomeStats()
        {
            return homeStats;
        }

        @JsonIgnore
        public List<Integer> getRates()
        {
            return homeStats.keySet()
                    .stream()
                    .sorted()
                    .collect(toImmutableList());
        }

        @JsonIgnore
        public int getCount()
        {
            return getRates().stream()
                    .map(rate -> homeStats.get(rate).getCount())
                    .reduce((x, y) -> x + y)
                    .orElse(0);
        }

        @Override
        public TeamRaterStats merge(TeamRaterStats other)
        {
            checkArgument(storageKey.equals(other.getStorageKey()), "Storage keys are different");
            TeamRaterStats.Builder builder = builder(storageKey);
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

        @JsonProperty("storageKey")
        @Override
        public String getStorageKey()
        {
            return storageKey;
        }

        public static class Builder
        {
            private final Map<Integer, RateStats> homeStats = new HashMap<>();
            private final String storageKey;

            public Builder(String storageKey)
            {
                this.storageKey = storageKey;
            }

            public Builder incrementHome(int rate, GameResult gameResult)
            {
                return addHome(rate, gameResult, 1);
            }

            public Builder addHome(int rate, GameResult gameResult, int count)
            {
                RateStats value = RateStats.builder().add(gameResult, count).build();
                homeStats.merge(rate, value, RateStats::merge);
                return this;
            }

            public TeamRaterStats build()
            {
                return new TeamRaterStats(storageKey, homeStats);
            }
        }
    }

    public static class RateStats
    {
        public static Builder builder()
        {
            return new Builder();
        }

        private final Map<GameResult, Integer> stats;

        @JsonCreator
        public RateStats(@JsonProperty("stats") Map<GameResult, Integer> stats)
        {
            this.stats = ImmutableMap.copyOf(stats);
        }

        @JsonIgnore
        public int getCount()
        {
            return stats.values().stream()
                    .reduce((x, y) -> x + y)
                    .orElse(0);
        }

        @JsonIgnore
        public int getWins()
        {
            return get(GameResult.WIN);
        }

        @JsonIgnore
        public int getDraws()
        {
            return get(GameResult.DRAW);
        }

        @JsonIgnore
        public int getLoses()
        {
            return get(GameResult.LOSE);
        }

        @JsonIgnore
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

        @JsonProperty("stats")
        public Map<GameResult, Integer> getStats()
        {
            return stats;
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
