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

import com.google.common.collect.ImmutableList;
import org.autobet.model.Game;

import java.util.List;
import java.util.Optional;

public class ChancesApproximationBasedPlayer
        implements Player
{
    private static final double GUESS_THRESHOLD = 0.6;
    private final TeamRatersStatsApproximation statsApproximation;
    private final TeamRater teamRater;

    public ChancesApproximationBasedPlayer(
            TeamRatersStatsApproximation statsApproximation,
            TeamRater teamRater)
    {
        this.statsApproximation = statsApproximation;
        this.teamRater = teamRater;
    }

    @Override
    public List<Guess> guess(Game game)
    {
        Optional<Integer> rate = teamRater.rate(game);
        if (!rate.isPresent()) {
            return ImmutableList.of();
        }
        ImmutableList.Builder<Guess> guess = ImmutableList.builder();
        if (statsApproximation.getHomeLoseChances(rate.get()) > GUESS_THRESHOLD) {
            guess.add(Guess.FULL_TIME_AWAY_TEAM_WIN);
        }
        if (statsApproximation.getDrawChances(rate.get()) > GUESS_THRESHOLD) {
            guess.add(Guess.FULL_TIME_DRAW);
        }
        if (statsApproximation.getHomeWinChances(rate.get()) > GUESS_THRESHOLD) {
            guess.add(Guess.FULL_TIME_HOME_TEAM_WIN);
        }
        return guess.build();
    }

    @Override
    public String getName()
    {
        return "chances_based_on_" + teamRater.getName() + "_rater";
    }
}
