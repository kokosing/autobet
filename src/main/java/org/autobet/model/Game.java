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

import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;

import java.util.List;
import java.util.Optional;

public class Game
        extends Model
{
    public static LazyList<Game> findAll(int startGame, Optional<Integer> limit)
    {
        if (limit.isPresent()) {
            return Game.findBySQL("SELECT * FROM games WHERE id > ? ORDER BY id LIMIT ?", startGame, limit.get());
        }
        return Game.findBySQL("SELECT * FROM games WHERE id > ? ORDER BY id", startGame);
    }

    public List<Bet> getBets() {
        return Bet.find("game_id = ? ", getId());
    }
}
