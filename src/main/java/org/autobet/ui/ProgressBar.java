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

import net.jcip.annotations.ThreadSafe;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkState;
import static java.time.Instant.now;
import static java.util.Objects.requireNonNull;

@ThreadSafe
public class ProgressBar
{
    private static final Duration REFRESH_PERIOD = Duration.ofMillis(1000);
    private final Temporal start = now();
    private final long count;
    private final String itemsName;
    private AtomicLong counter = new AtomicLong();
    private AtomicReference<Temporal> lastDisplayTime = new AtomicReference<>(start);

    public ProgressBar(long count, String itemsName)
    {
        this.count = count;
        this.itemsName = requireNonNull(itemsName, "itemsName is null");
    }

    public void increment()
    {
        checkState(counter.incrementAndGet() <= count, "increment called to many times");
        Instant now = now();
        Temporal localLastDisplayTime = lastDisplayTime.get();
        Duration sinceLastDisplay = Duration.between(lastDisplayTime.get(), now);
        if (sinceLastDisplay.compareTo(REFRESH_PERIOD) > 0) {
            if (lastDisplayTime.compareAndSet(localLastDisplayTime, now)) {
                display(now);
            }
        }
        if (counter.get() >= count) {
            display(now);
            finish();
        }
    }

    private void finish()
    {
        System.out.println("");
    }

    private void display(Instant now)
    {
        long localCounter = getCounter();
        double percent = (double) localCounter * 100 / count;
        Duration sinceStart = Duration.between(start, now);
        double throughput = (double) localCounter / sinceStart.getSeconds();
        Duration left = Duration.ofSeconds((long) ((count - localCounter) / throughput));
        String msg = String.format(
                "Processed %d out of %d %s - %.2f%%, %.2f per second, duration %s, %s left                    ",
                localCounter,
                count,
                itemsName,
                percent,
                throughput,
                format(sinceStart),
                format(left));
        System.out.print("\r" + msg);
    }

    private String format(Duration duration)
    {
        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        long hours = minutes / 60;
        if (minutes == 0) {
            return String.format("%ds", seconds);
        }
        else if (hours == 0) {
            seconds = seconds % 60;
            return String.format("%dmin %ds", minutes, seconds);
        }
        seconds = seconds % 60;
        seconds = seconds % 60;
        minutes = minutes % 60;
        return String.format("%dh %dmin %ds", hours, minutes, seconds);
    }

    public long getCounter()
    {
        return counter.get();
    }

    public void stop(String reason)
    {
        finish();
        System.out.println("Processing interrupted: " + reason);
    }
}
