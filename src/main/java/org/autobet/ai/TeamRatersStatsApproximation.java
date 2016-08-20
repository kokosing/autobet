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

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.autobet.ai.TeamRaterStatsCollector.GameResult;
import org.autobet.ai.TeamRaterStatsCollector.RateStats;
import org.autobet.ai.TeamRaterStatsCollector.TeamRaterStats;
import org.autobet.math.Polynomial;

import java.util.List;
import java.util.Map;

import static org.autobet.ImmutableCollectors.toImmutableList;
import static org.autobet.ai.TeamRaterStatsCollector.GameResult.DRAW;
import static org.autobet.ai.TeamRaterStatsCollector.GameResult.LOSE;
import static org.autobet.ai.TeamRaterStatsCollector.GameResult.WIN;

public class TeamRatersStatsApproximation
{
    private static final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(4);

    private final Polynomial homeWinChances;
    private final Polynomial homeLoseChances;
    private final Polynomial homeDrawChances;
    private final Polynomial awayWinChances;
    private final Polynomial awayLoseChances;
    private final Polynomial awayDrawChances;

    public TeamRatersStatsApproximation(TeamRaterStats teamRaterStats)
    {
        homeWinChances = approximate(teamRaterStats.getHomeStats(), WIN);
        homeLoseChances = approximate(teamRaterStats.getHomeStats(), LOSE);
        homeDrawChances = approximate(teamRaterStats.getHomeStats(), DRAW);

        awayWinChances = approximate(teamRaterStats.getAwayStats(), WIN);
        awayLoseChances = approximate(teamRaterStats.getAwayStats(), LOSE);
        awayDrawChances = approximate(teamRaterStats.getAwayStats(), DRAW);
    }

    private Polynomial approximate(Map<Integer, RateStats> stats, GameResult gameResult)
    {
        final WeightedObservedPoints obs = new WeightedObservedPoints();
        List<Integer> rates = stats.keySet()
                .stream()
                .sorted()
                .collect(toImmutableList());

        for (int rate : rates) {
            RateStats rateStats = stats.get(rate);
            int stat = rateStats.get(gameResult);
            int count = rateStats.getCount();
            obs.add(count, rate, (double) stat / count);
        }
        return new Polynomial(fitter.fit(obs.toList()));
    }

    public double getHomeWinChances(int rate)
    {
        return cap(homeWinChances.calculate(rate));
    }

    public double getHomeLoseChances(int rate)
    {
        return cap(homeLoseChances.calculate(rate));
    }

    public double getHomeDrawChances(int rate)
    {
        return cap(homeDrawChances.calculate(rate));
    }

    public double getAwayWinChances(int rate)
    {
        return cap(awayWinChances.calculate(rate));
    }

    public double getAwayLoseChances(int rate)
    {
        return cap(awayLoseChances.calculate(rate));
    }

    public double getAwayDrawChances(int rate)
    {
        return cap(awayDrawChances.calculate(rate));
    }

    private double cap(double value)
    {
        return Math.max(Math.min(value, 1.0), 0);
    }
}
