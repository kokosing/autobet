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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.autobet.ioc.DaggerMainComponent;
import org.autobet.model.Division;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.autobet.ImmutableCollectors.toImmutableMap;

public final class App
{
    private final Map<String, Command> commands = Stream.of(new LoadCommand())
            .collect(toImmutableMap(Command::getName));

    @Parameter(names = {"--help", "-h"}, help = true)
    private boolean help;

    public static final void main(String[] args)
    {
        App app = new App();
        JCommander jc = new JCommander(app);
        for (Command command : app.commands.values()) {
            jc.addCommand(command.getName(), command);
        }

        jc.parse(args);

        if (app.help || jc.getParsedCommand() == null) {
            jc.usage();
        }
        else {
            app.start(jc);
        }
    }

    private void start(JCommander jc)
    {
        DaggerMainComponent.create().getFlyway().migrate();

        commands.get(jc.getParsedCommand()).go();
    }

    @Parameters(commandDescription = "Load csv data into database")
    public static final class LoadCommand
            implements Command
    {
        @Parameter(description = "CSV files")
        private List<String> csvFiles;

        @Override
        public void go()
        {
            System.out.println("loading: " + csvFiles);
            for (String csvFile : csvFiles) {
                load(csvFile);
            }
        }

        public void load(String csvFile)
        {
            Set<String> divisions = new HashSet<>();
            try (CsvFileReader csvFileReader = new CsvFileReader(csvFile)) {
                for (Map<String, String> line : csvFileReader) {
                    String division = line.get("div");
                    if (!divisions.contains(division)) {
                        new Division().set("name", division).saveIt();
                        divisions.add(division);
                        System.out.println("Created division: " + division);
                    }
                }
            }
        }

        @Override
        public String getName()
        {
            return "load";
        }
    }

    interface Command
    {
        void go();

        String getName();
    }
}
