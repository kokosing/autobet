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
import com.google.common.collect.Iterables;
import org.autobet.model.BetType;
import org.autobet.model.Game;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.RowListener;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

public class PlayerEvaluator
{
    private final Map<Player.Guess, BetType> guessToBetTypes;

    public PlayerEvaluator()
    {
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

    public double evaluate(Player player, Date start, Date end)
    {
        List<Game> games = Game.find("played_at >= ? and played_at <= ?", start, end);

        double result = 0;
        for (Game game : games) {
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

        return result;
    }

    private <T> T querySingleValue(String query, Object... params)
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
