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

import org.autobet.model.Team;
import org.javalite.activejdbc.Base;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Math.toIntExact;

public class GoalBasedTeamRater
        implements TeamRater
{
    @Override
    public Optional<Integer> rate(Team team, Date date)
    {
        List<Map> ratings = Base.findAll(
                "SELECT count(*) AS count, sum(scored) AS total_scored, sum(lost) AS total_lost FROM (" +
                        "SELECT CASE home_team_id " +
                        "WHEN true THEN full_time_home_team_goals ELSE full_time_away_team_goals END AS scored," +
                        "CASE home_team_id " +
                        "WHEN true THEN full_time_away_team_goals ELSE full_time_home_team_goals END AS lost " +
                        "FROM games " +
                        "WHERE ? IN (home_team_id, away_team_id) and played_at < ? " +
                        "ORDER BY played_at DESC " +
                        "LIMIT ?)",
                team.getId(),
                date,
                6);

        checkState(ratings.size() == 1, "Expected only one row, bug got: %s", ratings.size());

        Map rating = ratings.get(0);

        if ((Long) rating.get("count") != 6) {
            return Optional.empty();
        }

        long totalScored = (long) rating.get("total_scored");
        long totalLost = (long) rating.get("total_lost");

        return Optional.of(toIntExact(totalScored - totalLost));
    }
}
