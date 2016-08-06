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

import org.autobet.TemporaryDatabase;
import org.junit.Rule;
import org.junit.Test;

import java.sql.Date;

import static org.junit.Assert.assertTrue;

public class PlayerEvaluatorTest
{
    @Rule
    public TemporaryDatabase temporaryDatabase = TemporaryDatabase.loaded();

    @Test
    public void evaluateRandomPlayer()
    {
        PlayerEvaluator evaluator = new PlayerEvaluator();

        RandomPlayer player = new RandomPlayer();
        double evaluation = evaluator.evaluate(player, Date.valueOf("2000-01-01"), Date.valueOf("2000-12-31"));

        assertTrue(evaluation < 100);
    }
}
