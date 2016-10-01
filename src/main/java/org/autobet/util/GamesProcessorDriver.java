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

package org.autobet.util;

import com.google.common.base.Throwables;
import com.google.common.primitives.Ints;
import net.jcip.annotations.ThreadSafe;
import org.autobet.model.Game;
import org.autobet.ui.ProgressBar;
import org.javalite.activejdbc.Base;

import javax.inject.Provider;
import javax.sql.DataSource;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.autobet.ImmutableCollectors.toImmutableList;

public class GamesProcessorDriver
{
    private final Timer timer = new Timer(true);
    private final DataSource dataSource;
    private final int threadsCount;

    public GamesProcessorDriver(DataSource dataSource)
    {
        this(dataSource, Runtime.getRuntime().availableProcessors());
    }

    public GamesProcessorDriver(DataSource dataSource, int threadsCount)
    {
        this.dataSource = dataSource;
        this.threadsCount = threadsCount;
    }

    public <T extends KeyValueStore.Storable> T driveProcessors(
            Provider<GamesProcessor<T>> gamesProcessorProvider,
            Optional<Integer> gamesLimit,
            Optional<Duration> timeLimit)
    {
        T union = gamesProcessorProvider.get().finish();
        String storeKey = union.getStorageKey();
        String countKey = "count_" + storeKey;
        Optional<T> cachedResult = KeyValueStore.loadLatest(storeKey, (Class<T>) union.getClass());
        Optional<Integer> cachedCount = KeyValueStore.loadLatest(countKey, Integer.class);

        int startGame = 0;
        if (cachedCount.isPresent()) {
            startGame = cachedCount.get();
        }

        long count = Game.count() - startGame;
        if (gamesLimit.isPresent() && gamesLimit.get() < count) {
            count = gamesLimit.get();
        }
        ProgressBar progressBar = new ProgressBar(count, "games");

        GameSupplier games = new GameSupplier(startGame, gamesLimit);
        if (timeLimit.isPresent()) {
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    progressBar.stop("Timeout");
                    games.close();
                }
            }, timeLimit.get().toMillis());
        }
        T result = processGames(gamesProcessorProvider, cachedResult.orElse(union), progressBar, games);

        KeyValueStore.store(storeKey, result);
        KeyValueStore.store(countKey, Ints.checkedCast(startGame + games.getCount()));
        return result;
    }

    private <T extends KeyValueStore.Storable> T processGames(
            Provider<GamesProcessor<T>> gamesProcessorProvider,
            T cachedResult,
            ProgressBar progressBar,
            GameSupplier games)
    {
        List<CompletableFuture<T>> futures = IntStream.range(0, threadsCount)
                .mapToObj(i -> supplyAsync(() -> {
                    Base.open(dataSource);
                    try {
                        GamesProcessor<T> gamesProcessor = gamesProcessorProvider.get();
                        while (true) {
                            Optional<Game> game = games.next();
                            if (!game.isPresent()) {
                                break;
                            }
                            gamesProcessor.process(game.get());
                            progressBar.increment();
                        }
                        return gamesProcessor.finish();
                    }
                    finally {
                        Base.close();
                    }
                })).collect(toImmutableList());

        T result = cachedResult;
        for (CompletableFuture<T> future : futures) {
            try {
                result = (T) result.merge(future.get());
            }
            catch (InterruptedException | ExecutionException e) {
                throw Throwables.propagate(e);
            }
        }
        return result;
    }

    public interface GamesProcessor<T extends KeyValueStore.Storable>
    {
        void process(Game game);

        T finish();
    }

    @ThreadSafe
    private class GameSupplier
    {
        private final Iterator<Game> games;
        private int count = 0;
        private boolean finished = false;

        private GameSupplier(int startGame, Optional<Integer> gamesLimit)
        {
            games = Game.findAll(startGame, gamesLimit).iterator();
        }

        private synchronized Optional<Game> next()
        {
            if (!finished && games.hasNext()) {
                count++;
                return Optional.of(games.next());
            }
            return Optional.empty();
        }

        public synchronized void close()
        {
            finished = true;
        }

        public int getCount()
        {
            return count;
        }
    }
}
