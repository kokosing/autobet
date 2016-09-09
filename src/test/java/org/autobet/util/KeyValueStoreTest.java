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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import org.autobet.TemporaryDatabase;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class KeyValueStoreTest
{
    @ClassRule
    public static final TemporaryDatabase temporaryDatabase = TemporaryDatabase.empty();

    @Test
    public void test()
    {
        assertFalse(KeyValueStore.loadLatest("key", SomeBean.class).isPresent());
        KeyValueStore.store("key", new SomeBean(0));
        KeyValueStore.store("key1", new SomeBean(1));
        KeyValueStore.store("key", new SomeBean(2));

        assertEquals(KeyValueStore.loadLatest("key", SomeBean.class).get(), new SomeBean(2));
    }

    public static class SomeBean
    {
        private final int value;

        @JsonCreator
        public SomeBean(@JsonProperty("value") int value)
        {
            this.value = value;
        }

        @JsonProperty("value")
        public int getValue()
        {
            return value;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SomeBean someBean = (SomeBean) o;
            return value == someBean.value;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(value);
        }

        @Override
        public String toString()
        {
            return MoreObjects.toStringHelper(this)
                    .add("value", value)
                    .toString();
        }
    }
}
