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

package org.autobet;/*
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

import com.google.common.collect.ImmutableList;
import org.autobet.model.Division;
import org.autobet.model.Team;

import java.util.HashMap;
import java.util.Map;

public class Loader
{
    private final Map<String, Division> divisions = new HashMap<>();
    private final Map<String, Team> teams = new HashMap<>();

    public void load(String csvFile)
    {
        try (CsvFileReader csvFileReader = new CsvFileReader(csvFile)) {
            for (Map<String, String> line : csvFileReader) {
                loadDivision(line);
                loadTeams(line);
            }
        }
    }

    private void loadDivision(Map<String, String> line)
    {
        String divisionName = line.get("div");
        if (!divisions.containsKey(divisionName)) {
            Division division = new Division().set("name", divisionName);
            division.saveIt();
            divisions.put(divisionName, division);
            System.out.println("Created division: " + division.toJson(true));
        }
    }

    private void loadTeams(Map<String, String> line)
    {
        Division division = divisions.get(line.get("div"));
        for (String teamName : ImmutableList.of(line.get("hometeam"), line.get("awayteam"))) {
            if (!teams.containsKey(teamName)) {
                Team team = new Team().set("name", teamName);
                division.add(team);
                team.saveIt();
                teams.put(teamName, team);
                System.out.println("Created team: " + team.toJson(true));
            }
        }
    }
}
