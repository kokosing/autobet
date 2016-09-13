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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.autobet.model.BetType;
import org.autobet.model.Game;
import org.autobet.util.GamesProcessorDriver;
import org.autobet.util.KeyValueStore;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.RowListener;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public class PlayerEvaluator
{
    private final Map<Player.Guess, BetType> guessToBetTypes;
    private final GamesProcessorDriver gamesProcessorDriver;
    private final Player player;

    public PlayerEvaluator(GamesProcessorDriver gamesProcessorDriver, Player player)
    {
        this.gamesProcessorDriver = gamesProcessorDriver;
        this.player = player;
        guessToBetTypes = buildGuessToBetTypes();
    }

    private Map<Player.Guess, BetType> buildGuessToBetTypes()
    {
        List<BetType> betTypes = BetType.findAll();
        ImmutableMap.Builder<Player.Guess, BetType> builder = ImmutableMap.builder();
        for (Player.Guess guess : Player.Guess.values()) {
            builder.put(
                    guess,
                    betTypes.stream()
                            .filter(betType -> betType.getString("bet_suffix").equals(guess.getBetSuffix()))
                            .findFirst()
                            .get());
        }
        return builder.build();
    }

    public Statistics evaluate(Optional<Integer> limit)
    {
        return gamesProcessorDriver.driveProcessors(() -> new GameProcessor(), limit);
    }

    private final class GameProcessor
            implements GamesProcessorDriver.GamesProcessor<Statistics>
    {
        private double result = 0;

        @Override
        public void process(Game game)
        {
            for (Player.Guess guess : player.guess(game)) {
                boolean won = game.getString("full_time_result").equals(guess.getBetSuffix());
                if (won) {
                    BigDecimal winningOdds = querySingleValue(
                            "select sum(odds) from bets where game_id = ? and bet_type_id=?",
                            game.getId(),
                            guessToBetTypes.get(guess).getId());
                    result += winningOdds.doubleValue();
                }
                long costOfBets = querySingleValue(
                        "select count(odds) from bets where game_id = ? and bet_type_id=?",
                        game.getId(),
                        guessToBetTypes.get(guess).getId());
                result -= costOfBets;
            }
        }

        @Override
        public Statistics finish()
        {
            return new Statistics(player.getName(), result);
        }
    }

    public static class Statistics
            implements KeyValueStore.Storable<Statistics>
    {
        private final String storageKey;
        private final double result;

        @JsonCreator
        public Statistics(@JsonProperty("storageKey") String storageKey, @JsonProperty("result") double result)
        {
            this.storageKey = storageKey;
            this.result = result;
        }

        @JsonProperty("result")
        public double getResult()
        {
            return result;
        }

        @JsonProperty("storageKey")
        @Override
        public String getStorageKey()
        {
            return storageKey;
        }

        @Override
        public Statistics merge(Statistics other)
        {
            checkArgument(
                    storageKey.equals(other.getStorageKey()),
                    "Statistics have to have same storageKey to be merged: %s vs %s",
                    storageKey,
                    other.getStorageKey());

            return new Statistics(storageKey, result + other.getResult());
        }
    }

    private static <T> T querySingleValue(String query, Object... params)
    {
        SingleValueRowProcessor singleValueRowProcessor = new SingleValueRowProcessor();
        Base.find(query, params)
                .with(singleValueRowProcessor);
        return (T) singleValueRowProcessor.get();
    }

    private static class SingleValueRowProcessor
            implements RowListener
    {
        Object value;

        @Override
        public boolean next(Map<String, Object> row)
        {
            checkState(value == null, "Query returned to many rows");
            checkState(row.size() == 1, "Query returned to many columns");
            value = Iterables.getOnlyElement(row.values());
            return true;
        }

        public <T> T get()
        {
            return (T) value;
        }
    }
}
