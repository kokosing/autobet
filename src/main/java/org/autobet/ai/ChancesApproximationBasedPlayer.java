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
import org.autobet.model.Bet;
import org.autobet.model.Game;

import java.util.List;
import java.util.Optional;

public class ChancesApproximationBasedPlayer
        implements Player
{
    private final static double PLAYING_AWARD_THRESHOLD = 0.3;
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
    public List<Bet> guess(Game game, List<Bet> availableBets)
    {
        Optional<Integer> rate = teamRater.rate(game);
        if (!rate.isPresent()) {
            return ImmutableList.of();
        }
        double homeWinChances = statsApproximation.getHomeWinChances(rate.get());
        double homeLoseChances = statsApproximation.getHomeLoseChances(rate.get());
        double drawChances = statsApproximation.getDrawChances(rate.get());
        ImmutableList.Builder<Bet> selectedBets = ImmutableList.builder();
        for (Bet bet : availableBets) {
            double chancesToWin;
            if (bet.getBetType().isFullTimeHomeWin()) {
                chancesToWin = homeWinChances;
            }
            else if (bet.getBetType().isFullTimeDraw()) {
                chancesToWin = drawChances;
            }
            else if (bet.getBetType().isFullTimeAwayWin()) {
                chancesToWin = homeLoseChances;
            }
            else {
                throw new IllegalStateException("Unexpected bet type: " + bet.getBetType().toJson(false));
            }
            double award = bet.getOdds() - 1;
            double expectedAward = (chancesToWin * award - (1 - chancesToWin));
            if (expectedAward > PLAYING_AWARD_THRESHOLD) {
                selectedBets.add(bet);
            }
        }
        return selectedBets.build();
    }

    @Override
    public String getName()
    {
        return "chances_based_on_" + teamRater.getName() + "_rater";
    }
}
