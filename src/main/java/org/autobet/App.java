package org.autobet;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class App
{
    @Parameter(names = {"--help", "-h"}, help = true)
    private boolean help;

    public static final void main(String[] args)
    {
        App app = new App();
        JCommander jc = new JCommander(app);
        Map<String, Command> commands = new HashMap<>();
        for (Command command : Arrays.<Command>asList(new LoadCommand())) {
            commands.put(command.getName(), command);
            jc.addCommand(command.getName(), command);
        }

        jc.parse(args);

        if (app.help || jc.getParsedCommand() == null) {
            jc.usage();
        }
        else {
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
            System.out.println("loading: " + csvFiles);
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
