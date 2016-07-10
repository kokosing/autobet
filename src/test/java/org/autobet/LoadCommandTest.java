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

import org.autobet.model.Division;
import org.autobet.model.Team;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoadCommandTest
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

        assertTrue(Division.count() == 1);
        Division b1 = Division.findById(1);
        assertEquals(b1.get("name"), "B1");

        assertTrue(Team.count() == 18);
        Team anderlecht = Team.findById(18);
        assertEquals(anderlecht.get("id"), 18L);
        assertEquals(anderlecht.get("division_id"), b1.get("id"));
        assertEquals(anderlecht.get("name"), "Anderlecht");

    }
}
