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

import com.google.common.collect.ImmutableMap;
import org.autobet.model.BetType;
import org.autobet.model.BetVendor;
import org.autobet.model.Division;
import org.autobet.model.Game;
import org.autobet.model.Team;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.autobet.ImmutableCollectors.toImmutableList;

class Loader
{
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");

    private final Map<String, BetFactory> betFactories;

    Loader()
    {
        betFactories = createBetFactories();
    }

    private Map<String, BetFactory> createBetFactories()
    {
        ImmutableMap.Builder<String, BetFactory> builder = ImmutableMap.builder();
        List<BetVendor> betVendors = BetVendor.findAll();
        List<BetType> betTypes = BetType.findAll();
        for (BetVendor betVendor : betVendors) {
            for (BetType betType : betTypes) {
                String key = betVendor.getString("bet_prefix") + betType.getString("bet_suffix");
                key = key.toLowerCase();
                builder.put(key, (preparedStatement, game, odds) -> {
                    Base.addBatch(preparedStatement, betVendor.getId(), betType.getId(), game.getId(), odds);
                    return true;
                });
            }
        }
        return builder.build();
    }

    public int load(String csvFile)
    {
        Base.openTransaction();
        AtomicInteger counter = new AtomicInteger();
        try (CsvFileReader csvFileReader = new CsvFileReader(csvFile)) {
            for (Map<String, String> line : csvFileReader) {
                Division division = loadDivision(line, counter);
                List<Team> teams = loadTeams(division, line, counter);
                loadGame(teams, line, counter);
            }
        }
        Base.commitTransaction();
        Division.purgeCache();
        Team.purgeCache();
        return counter.get();
    }

    private Division loadDivision(Map<String, String> line, AtomicInteger counter)
    {
        String divisionName = line.get("div");
        Optional<Division> division = findSingle(Division.find("name = ?", divisionName));
        return division.orElseGet(() -> {
            Division newDivision = new Division().set("name", divisionName);
            newDivision.saveIt();
            counter.incrementAndGet();
            return newDivision;
        });
    }

    private List<Team> loadTeams(Division division, Map<String, String> line, AtomicInteger counter)
    {
        return Stream.of(line.get("hometeam"), line.get("awayteam"))
                .map(teamName -> {
                    Optional<Team> team = findSingle(Team.find("name = ? and division_id = ?", teamName, division.get("id")));
                    return team.orElseGet(() -> {
                        Team newTeam = new Team().set("name", teamName);
                        division.add(newTeam);
                        newTeam.saveIt();
                        counter.incrementAndGet();
                        return newTeam;
                    });
                })
                .collect(toImmutableList());
    }

    private void loadGame(List<Team> teams, Map<String, String> line, AtomicInteger counter)
    {
        Date date = parseDate(line.get("date"));
        Object homeTeamId = teams.get(0).getId();
        Object awayTeamId = teams.get(1).getId();
        Optional<Game> game = findSingle(Game.find("home_team_id = ? and away_team_id = ? and played_at = ?", homeTeamId, awayTeamId, date));
        if (!game.isPresent()) {
            Game newGame = new Game()
                    .set("home_team_id", homeTeamId)
                    .set("away_team_id", awayTeamId)
                    .set("played_at", date)
                    .set("full_time_home_team_goals", line.get("fthg"))
                    .set("full_time_away_team_goals", line.get("ftag"))
                    .set("full_time_result", line.get("ftr"))
                    .set("half_time_home_team_goals", line.get("hthg"))
                    .set("half_time_away_team_goals", line.get("htag"))
                    .set("half_time_result", line.get("htr"))
                    .set("attendance", line.get("attendance"))
                    .set("referee", line.get("referee"))
                    .set("home_team_shots", line.get("hs"))
                    .set("away_team_shots", line.get("as"))
                    .set("home_team_shots_on_target", line.get("hst"))
                    .set("away_team_shots_on_target", line.get("ast"))
                    .set("home_team_hit_woodwork", line.get("hhw"))
                    .set("away_team_hit_woodwork", line.get("ahw"))
                    .set("home_team_corners", line.get("hc"))
                    .set("away_team_corners", line.get("ac"))
                    .set("home_team_fouls_committed", line.get("hf"))
                    .set("away_team_fouls_committed", line.get("af"))
                    .set("home_team_offsides", line.get("ho"))
                    .set("away_team_offsides", line.get("ao"))
                    .set("home_team_yellow_cards", line.get("hy"))
                    .set("away_team_yellow_cards", line.get("ay"))
                    .set("home_team_red_cards", line.get("hr"))
                    .set("away_team_red_cards", line.get("ar"))
                    .set("home_team_red_cards", line.get("hr"))
                    .set("away_team_red_cards", line.get("ar"));

            newGame.saveIt();
            counter.incrementAndGet();
            loadBets(newGame, line, counter);
        }
    }

    private Date parseDate(String date)
    {
        try {
            return new Date(dateFormat.parse(date + "+0000").getTime());
        }
        catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends Model> Optional<T> findSingle(List<T> elements)
    {
        if (elements.size() == 1) {
            return Optional.of(elements.get(0));
        }
        else if (elements.size() == 0) {
            return Optional.empty();
        }
        else {
            throw new RuntimeException("Multiple elements found while only one was expected: " + elements);
        }
    }

    private void loadBets(Game game, Map<String, String> line, AtomicInteger counter)
    {
        PreparedStatement preparedStatement = Base.startBatch(
                "INSERT INTO bets(bet_vendor_id, bet_type_id, game_id, odds) values(?, ?, ?, ?)");
        for (String betKey : betFactories.keySet()) {
            String odds = line.get(betKey);
            if (odds != null) {
                if (betFactories.get(betKey).createIfNotExists(preparedStatement, game, odds)) {
                    counter.incrementAndGet();
                }
            }
        }
        Base.executeBatch(preparedStatement);
        try {
            preparedStatement.close();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private interface BetFactory
    {
        boolean createIfNotExists(PreparedStatement preparedStatement, Game game, String odds);
    }
}
