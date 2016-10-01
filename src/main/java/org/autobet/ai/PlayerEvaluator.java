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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.autobet.model.Bet;
import org.autobet.model.Game;
import org.autobet.util.GamesProcessorDriver;
import org.autobet.util.KeyValueStore;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

public class PlayerEvaluator
{
    private final GamesProcessorDriver gamesProcessorDriver;

    public PlayerEvaluator(GamesProcessorDriver gamesProcessorDriver)
    {
        this.gamesProcessorDriver = gamesProcessorDriver;
    }

    public Statistics evaluate(Player player, Optional<Integer> gamesLimit, Optional<Duration> timeLimit)
    {
        return gamesProcessorDriver.driveProcessors(() -> new GameProcessor(player), gamesLimit, timeLimit);
    }

    private final class GameProcessor
            implements GamesProcessorDriver.GamesProcessor<Statistics>
    {
        private final Player player;
        private double result = 0;
        private int betsCount = 0;
        private int playedBetsCount = 0;
        private int winningBetCount = 0;

        public GameProcessor(Player player)
        {
            this.player = player;
        }

        @Override
        public void process(Game game)
        {
            List<Bet> bets = game.getBets();
            List<Bet> selectedBets = player.guess(game, bets);
            for (Bet bet : selectedBets) {
                if (bet.isWinning(game)) {
                    result += bet.getOdds();
                    winningBetCount++;
                }
                result -= 1;
            }
            betsCount += bets.size();
            playedBetsCount += selectedBets.size();
        }

        @Override
        public Statistics finish()
        {
            return new Statistics(player.getName(), result, betsCount, playedBetsCount, winningBetCount);
        }
    }

    public static class Statistics
            implements KeyValueStore.Storable<Statistics>
    {
        private final String storageKey;
        private final double result;
        private final int betsCount;
        private final int playedBetsCount;
        private final int winningBetsCount;

        @JsonCreator
        public Statistics(
                @JsonProperty("storageKey") String storageKey,
                @JsonProperty("result") double result,
                @JsonProperty("betsCount") int betsCount,
                @JsonProperty("playedBetsCount") int playedBetsCount,
                @JsonProperty("winningBetsCount") int winningBetsCount)
        {
            this.storageKey = storageKey;
            this.result = result;
            this.betsCount = betsCount;
            this.playedBetsCount = playedBetsCount;
            this.winningBetsCount = winningBetsCount;
        }

        @JsonProperty("result")
        public double getResult()
        {
            return result;
        }

        @JsonProperty("storageKey")
        @Override
        public String getStorageKey()
        {
            return storageKey;
        }

        @JsonProperty("betsCount")
        public int getBetsCount()
        {
            return betsCount;
        }

        @JsonProperty("playedBetsCount")
        public int getPlayedBetsCount()
        {
            return playedBetsCount;
        }

        @JsonProperty("winningBetsCount")
        public int getWinningBetsCount()
        {
            return winningBetsCount;
        }

        @Override
        public Statistics merge(Statistics other)
        {
            checkArgument(
                    storageKey.equals(other.getStorageKey()),
                    "Statistics have to have same storageKey to be merged: %s vs %s",
                    storageKey,
                    other.getStorageKey());

            return new Statistics(storageKey,
                    result + other.getResult(),
                    betsCount + other.betsCount,
                    playedBetsCount + other.playedBetsCount,
                    winningBetsCount + other.winningBetsCount);
        }
    }
}
