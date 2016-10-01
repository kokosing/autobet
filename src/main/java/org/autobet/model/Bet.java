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
package org.autobet.model;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("bets")
public class Bet
        extends Model
{
    public BetType getBetType()
    {
        return BetType.findById(getLong("bet_type_id"));
    }

    public boolean isWinning(Game game)
    {
        return getBetType().getString("bet_suffix").equalsIgnoreCase(game.getString("full_time_result"));
    }

    public double getOdds() {
        return getDouble("odds");
    }
}
