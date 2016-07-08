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

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CsvFileReaderTest
{
    @Test
    public void readExampleFile()
    {
        int linesCount = 0;
        try (CsvFileReader csvFileReader = new CsvFileReader("data/www.football-data.co.uk/mmz4281/0001/B1.csv")) {
            for (Map<String, String> line : csvFileReader) {
                assertEquals("B1", line.get("div"));
                linesCount++;
            }
        }
        assertEquals(306, linesCount);
    }
}
