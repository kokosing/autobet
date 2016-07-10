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

import com.google.common.collect.ImmutableList;
import org.autobet.model.Division;
import org.autobet.model.Team;
import org.javalite.activejdbc.LazyList;

import java.util.Map;

public class Loader
{
    public void load(String csvFile)
    {
        try (CsvFileReader csvFileReader = new CsvFileReader(csvFile)) {
            for (Map<String, String> line : csvFileReader) {
                Division division = loadDivision(line);
                loadTeams(division, line);
            }
        }
    }

    private Division loadDivision(Map<String, String> line)
    {
        String divisionName = line.get("div");
        LazyList<Division> divisions = Division.find("name = ?", divisionName);
        if (divisions.size() == 1) {
            return divisions.get(0);
        }
        else if (divisions.size() == 0) {
            Division division = new Division().set("name", divisionName);
            division.saveIt();
            System.out.println("Created division: " + division.toJson(true));
            return division;
        }
        else {
            throw new RuntimeException("Multiple divisions found with the same name: " + divisionName);
        }
    }

    private void loadTeams(Division division, Map<String, String> line)
    {
        for (String teamName : ImmutableList.of(line.get("hometeam"), line.get("awayteam"))) {
            LazyList<Team> teams = Team.find("name = ? and division_id = ?", teamName, division.get("id"));
            if (teams.size() == 1) {
                continue;
            }
            else if (teams.size() == 0) {
                Team team = new Team().set("name", teamName);
                division.add(team);
                team.saveIt();
                System.out.println("Created team: " + team.toJson(true));
            }
            else {
                throw new RuntimeException("Multiple teams found with the same name: " + teamName);
            }
        }
    }
}
