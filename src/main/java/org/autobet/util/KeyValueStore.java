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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.autobet.model.KeyValueStoreEntry;

import java.io.IOException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

public class KeyValueStore
{
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void store(String key, Object value)
    {
        try {
            String valueAsString = objectMapper.writeValueAsString(value);
            KeyValueStoreEntry entry = new KeyValueStoreEntry().set("_key", key).set("_value", valueAsString);
            checkState(entry.save(), "Unable to store: %s -> %s", key, valueAsString);
        }
        catch (JsonProcessingException e) {
            Throwables.propagate(e);
        }
    }

    public static <T> Optional<T> loadLatest(String key, Class<T> clazz)
    {
        KeyValueStoreEntry entry = KeyValueStoreEntry.findFirst("_key = ? ORDER BY id DESC", key);
        if (entry == null) {
            return Optional.empty();
        }
        try {
            T value = objectMapper.readValue((String) entry.get("_value"), clazz);
            return Optional.of(value);
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static <T> Optional<T> loadLatest(String key, TypeReference<T> typeReference)
    {
        KeyValueStoreEntry entry = KeyValueStoreEntry.findFirst("_key = ? ORDER BY id DESC", key);
        if (entry == null) {
            return Optional.empty();
        }
        try {
            T value = objectMapper.readValue((String) entry.get("_value"), typeReference);
            return Optional.of(value);
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public interface Storable <T extends Storable> {
        String getStorageKey();

        T merge(T other);
    }
}
