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

import static org.autobet.ai.TeamRaterStatsCollector.GameResult.DRAW;
import static org.autobet.ai.TeamRaterStatsCollector.GameResult.LOSE;
import static org.autobet.ai.TeamRaterStatsCollector.GameResult.WIN;

public class TeamRatersStatsApproximation
{
    private static final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);

    private final Polynomial homeWinChances;
    private final Polynomial homeLoseChances;
    private final Polynomial drawChances;

    public TeamRatersStatsApproximation(TeamRaterStats teamRaterStats)
    {
        homeWinChances = approximate(teamRaterStats, WIN);
        homeLoseChances = approximate(teamRaterStats, LOSE);
        drawChances = approximate(teamRaterStats, DRAW);
    }

    private Polynomial approximate(TeamRaterStats teamRaterStats, GameResult gameResult)
    {
        Map<Integer, RateStats> stats = teamRaterStats.getHomeStats();
        List<Integer> rates = teamRaterStats.getRates();
        final WeightedObservedPoints obs = new WeightedObservedPoints();

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

    public double getDrawChances(int rate)
    {
        return cap(drawChances.calculate(rate));
    }

    private double cap(double value)
    {
        return Math.max(Math.min(value, 1.0), 0);
    }
}
