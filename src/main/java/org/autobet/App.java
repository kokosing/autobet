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
import org.autobet.ioc.DatabaseConnectionModule.DatabaseConnection;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;
import static org.autobet.ImmutableCollectors.toImmutableMap;

public final class App
{
    private final Map<String, Command> commands = Stream.of(new LoadCommand(), new QueryCommand())
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
        try (DatabaseConnection _ = DaggerMainComponent.create().connectToDatabase()) {
            commands.get(jc.getParsedCommand()).go();
        }
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
            Loader loader = new Loader();
            for (String csvFile : csvFiles) {
                long start = currentTimeMillis();
                System.out.println("loading: " + csvFile);
                try {
                    loader.load(csvFile);
                    System.out.println("loaded in:" + (currentTimeMillis() - start) + "ms");
                }
                catch (DBException ex) {
                    System.out.println("Unable to load: " + csvFile);
                    ;
                    System.out.println(ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }

        @Override
        public String getName()
        {
            return "load";
        }
    }

    @Parameters(commandDescription = "Run query")
    public static final class QueryCommand
            implements Command
    {
        @Parameter(description = "query")
        private List<String> queries;

        @Override
        public void go()
        {
            requireNonNull(queries, "queries is null");
            for (String query : queries) {
                Base.find(query, row -> {
                    System.out.println(row);
                    return true;
                });
            }
        }

        @Override
        public String getName()
        {
            return "query";
        }
    }

    interface Command
    {
        void go();

        String getName();
    }
}
