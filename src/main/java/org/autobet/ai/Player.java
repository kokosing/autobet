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

import java.util.List;

public interface Player
{
    enum Guess
    {
        FULL_TIME_HOME_TEAM_WIN("H"),
        FULL_TIME_DRAW("D"),
        FULL_TIME_AWAY_TEAM_WIN("A");

        private final String betSuffix;

        Guess(String betSuffix)
        {
            this.betSuffix = betSuffix;
        }

        public String getBetSuffix()
        {
            return betSuffix;
        }
    }

    List<Guess> guess(Game game);
}
