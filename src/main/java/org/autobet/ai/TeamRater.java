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

import org.autobet.model.Game;
import org.autobet.model.Team;
import org.autobet.util.Named;

import java.sql.Date;
import java.util.Optional;

public interface TeamRater extends Named
{
    Optional<Integer> rate(Team team, Date date);

    default Optional<Integer> rate(Game game)
    {
        Team homeTeam = Team.findById(game.getLong("home_team_id"));
        Team awayTeam = Team.findById(game.getLong("away_team_id"));
        Date playedAt = game.getDate("played_at");

        Optional<Integer> homeTeamRate = rate(homeTeam, playedAt);
        Optional<Integer> awayTeamRate = rate(awayTeam, playedAt);
        if (homeTeamRate.isPresent() && awayTeamRate.isPresent()) {
            return Optional.of(homeTeamRate.get() - awayTeamRate.get());
        }
        return Optional.empty();
    }
}
