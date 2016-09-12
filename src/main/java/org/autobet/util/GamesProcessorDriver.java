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
import org.autobet.ioc.MainComponent;
import org.autobet.model.Game;
import org.autobet.ui.ProgressBar;
import org.javalite.activejdbc.Base;

import javax.inject.Provider;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.autobet.ImmutableCollectors.toImmutableList;

public class GamesProcessorDriver
{
    private final MainComponent mainComponent;

    public GamesProcessorDriver(MainComponent mainComponent)
    {
        this.mainComponent = mainComponent;
    }

    public <T extends KeyValueStore.Storable> T driveProcessors(
            Provider<GamesProcessor<T>> gamesProcessorProvider,
            BiFunction<T, T, T> merger,
            Optional<Integer> limit)
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
        if (limit.isPresent() && limit.get() < count) {
            count = limit.get();
        }
        ProgressBar progressBar = new ProgressBar(count, "games");

        Iterator<Game> games = Game.findAll(startGame, limit).iterator();
        List<CompletableFuture<T>> futures = IntStream.range(0, Runtime.getRuntime().availableProcessors())
                .mapToObj(i -> supplyAsync(() -> {
                    Base.open(mainComponent.getDataSource());
                    try {
                        GamesProcessor<T> gamesProcessor = gamesProcessorProvider.get();
                        Game game;
                        while (true) {
                            synchronized (games) {
                                if (games.hasNext()) {
                                    game = games.next();
                                }
                                else {
                                    break;
                                }
                            }

                            gamesProcessor.process(game);
                            progressBar.increment();
                        }
                        return gamesProcessor.finish();
                    }
                    finally {
                        Base.close();
                    }
                })).collect(toImmutableList());

        T result = cachedResult.orElse(union);
        for (CompletableFuture<T> future : futures) {
            try {
                result = merger.apply(result, future.get());
            }
            catch (InterruptedException | ExecutionException e) {
                throw Throwables.propagate(e);
            }
        }

        KeyValueStore.store(storeKey, result);
        KeyValueStore.store(countKey, Ints.checkedCast(startGame + count));
        return result;
    }

    public interface GamesProcessor<T extends KeyValueStore.Storable>
    {
        void process(Game game);

        T finish();
    }
}