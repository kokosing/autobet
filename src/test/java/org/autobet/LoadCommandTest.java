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

import org.autobet.ioc.DaggerTestMainComponent;
import org.autobet.ioc.DatabaseConnectionModule.DatabaseConnection;
import org.autobet.model.Division;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoadCommandTest
{
    private DatabaseConnection connection;

    @Before
    public void setUp()
    {
        connection = DaggerTestMainComponent.create().connectToDatabase();
    }

    @After
    public void tearDown()
    {
        connection.close();
    }

    @Test
    public void load()
    {
        new App.LoadCommand().load("data/www.football-data.co.uk/mmz4281/0001/B1.csv");
        assertTrue(Division.count() == 1);
        assertEquals(Division.where("true").get(0).get("name"), "B1");
    }
}
