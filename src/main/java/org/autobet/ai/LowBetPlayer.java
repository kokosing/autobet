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

import org.autobet.model.Bet;
import org.autobet.model.Game;

import java.util.List;

import static org.autobet.ImmutableCollectors.toImmutableList;

public class LowBetPlayer
        implements Player
{
    @Override
    public String getName()
    {
        return "low_bet";
    }

    @Override
    public List<Bet> guess(Game game, List<Bet> availableBets)
    {
        return availableBets.stream()
                .filter(bet -> bet.getOdds() < 1.2d)
                .collect(toImmutableList());
    }
}
