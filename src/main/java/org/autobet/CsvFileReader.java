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

package org.autobet;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.autobet.ImmutableCollectors.toImmutableList;

public class CsvFileReader
        implements CloseableIterator<Map<String, String>>
{
    private final BufferedReader fileReader;
    private final List<String> header;

    public CsvFileReader(String csvFile)
    {
        fileReader = openFile(csvFile);
        header = split(nextLine()).stream().map(String::toLowerCase).collect(toImmutableList());
    }

    private ImmutableList<String> split(Optional<String> headerLine)
    {
        if (!headerLine.isPresent()) {
            return ImmutableList.of();
        }
        else {
            return ImmutableList.copyOf(Splitter.on(",").split(headerLine.get()));
        }
    }

    private Optional<String> nextLine()
    {
        try {
            return Optional.ofNullable(fileReader.readLine());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedReader openFile(String file)
    {
        try {
            return new BufferedReader(new FileReader(file));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close()
    {
        try {
            fileReader.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNext()
    {
        try {
            return fileReader.ready();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, String> next()
    {
        List<String> lineValues = split(nextLine());
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (int i = 0; i < Math.min(header.size(), lineValues.size()); i++) {
            String value = lineValues.get(i);
            String key = header.get(i);
            if (key.length() > 0 && value.length() > 0) {
                builder.put(key, value);
            }
        }
        return builder.build();
    }
}
