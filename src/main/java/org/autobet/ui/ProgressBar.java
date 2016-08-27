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

package org.autobet.ui;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;

import static java.time.Instant.now;
import static java.util.Objects.requireNonNull;

public class ProgressBar
{
    private static final Duration REFRESH_PERIOD = Duration.ofMillis(1000);
    private final Temporal start = now();
    private final long count;
    private final String itemsName;
    private long counter = 0;
    private Temporal lastDisplayTime = start;

    public ProgressBar(long count, String itemsName)
    {
        this.count = count;
        this.itemsName = requireNonNull(itemsName, "itemsName is null");
    }

    public void increment()
    {
        counter++;
        Instant now = now();
        Duration sinceLastDisplay = Duration.between(lastDisplayTime, now);
        if (sinceLastDisplay.compareTo(REFRESH_PERIOD) > 0) {
            lastDisplayTime = now;
            display(now);
        }
        if (counter == count) {
            System.out.println("");
        }
    }

    private void display(Instant now)
    {
        double percent = (int) (counter * 100 / count);
        Duration sinceStart = Duration.between(start, now);
        double throughput = (double) counter / sinceStart.getSeconds();
        Duration left = Duration.ofSeconds((long) ((count - counter) / throughput));
        String msg = String.format(
                "Processed %d out of %d %s - %.2f%%, %.2f per second, %s left                    ",
                counter,
                count,
                itemsName,
                percent,
                throughput,
                left);
        System.out.print("\r" + msg);
    }

    public long getCounter()
    {
        return counter;
    }
}
