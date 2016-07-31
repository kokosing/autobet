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

import org.autobet.model.Game;

import java.sql.Date;
import java.util.List;

public class PlayerEvaluator
{
    public double evaluate(Player player, Date start, Date end)
    {
        List<Game> games = Game.find("played_at >= ? and played_at <= ?", start, end);

        double result = 0;
        for (Game game : games) {
            for (Player.Guess guess : player.guess(game)) {
                boolean won = game.getString("full_time_result").equals(guess.getBetSuffix());
                result += won ? 1 : -1;
            }
        }

        return result;
    }
}
