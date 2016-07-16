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

package org.autobet;

import org.autobet.model.Bet;
import org.autobet.model.Division;
import org.autobet.model.Game;
import org.autobet.model.Team;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class LoaderTest
{
    @Rule
    public TemporaryDatabase temporaryDatabase = new TemporaryDatabase();

    @Test
    public void load()
    {
        Loader loader = new Loader();
        loader.load("data/www.football-data.co.uk/mmz4281/0001/B1.csv");
        loader.load("data/www.football-data.co.uk/mmz4281/0001/B1.csv");
        loader.load("data/www.football-data.co.uk/mmz4281/0001/B1.csv");

        assertEquals((long) Division.count(), 1);
        Division b1 = Division.findById(1);
        assertEquals(b1.get("name"), "B1");

        assertEquals((long) Team.count(), 18);
        Team anderlecht = Team.findById(18);
        assertEquals(anderlecht.get("id"), 18L);
        assertEquals(anderlecht.get("division_id"), b1.get("id"));
        assertEquals(anderlecht.get("name"), "Anderlecht");

        assertEquals((long) Game.count(), 306);
        Game game = Game.findById(1);
        assertEquals(game.get("id"), 1L);
        assertEquals(game.get("home_team_id"), 1L);
        assertEquals(game.get("away_team_id"), 2L);
        assertEquals(game.get("full_time_home_team_goals"), (byte) 1);
        assertEquals(game.get("full_time_away_team_goals"), (byte) 2);
        assertEquals(game.get("full_time_result"), "A");
        assertEquals(game.get("half_time_home_team_goals"), (byte) 1);
        assertEquals(game.get("half_time_away_team_goals"), (byte) 1);
        assertEquals(game.get("half_time_result"), "D");

        assertEquals((long) Bet.count(), 3600);
        Bet bet = Bet.findById(1);
        assertEquals(bet.getId(), 1L);
        assertEquals(bet.get("bet_vendor_id"), 4L);
        assertEquals(bet.get("bet_type_id"), 1L);
        assertEquals(bet.get("game_id"), 1L);
        assertEquals(bet.get("odds"), new BigDecimal("2.2000"));
    }
}
