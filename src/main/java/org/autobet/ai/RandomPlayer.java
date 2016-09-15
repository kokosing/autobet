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
import java.util.Random;

public class RandomPlayer
        implements Player
{
    private final Random random = new Random(0);

    @Override
    public List<Bet> guess(Game game, List<Bet> availableBets)
    {
        double randomDouble = random.nextDouble();

        int guess = (int) (randomDouble * (availableBets.size() + 1));

        if (guess >= availableBets.size()) {
            return ImmutableList.of();
        }
        return ImmutableList.of(availableBets.get(guess));
    }

    @Override
    public String getName()
    {
        return "random";
    }
}
