package ru.nanit.limbo.server;

import ru.nanit.limbo.server.commands.CmdConn;
import ru.nanit.limbo.server.commands.CmdHelp;
import ru.nanit.limbo.server.commands.CmdMem;
import ru.nanit.limbo.server.commands.CmdStop;

import java.io.InputStream;
import java.util.*;

public final class CommandManager extends Thread {

    private InputStream in;
    private final Map<String, Command> commands = new HashMap<>();

    /**
     * @param in if null nothing will be read. The stream to listen for commands. Normally this is {@link System#in} to listen for user input
     *           on the console/terminal.
     */
    public CommandManager(InputStream in) {
        this.in = in;
    }

    public Map<String, Command> getCommands() {
        return Collections.unmodifiableMap(commands);
    }

    public Command getCommand(String name) {
        return commands.get(name.toLowerCase());
    }

    public void register(String name, Command cmd) {
        commands.put(name.toLowerCase(), cmd);
    }

    @Override
    public void run() {
        if(in==null){
            return;
        }
        Scanner scanner = new Scanner(in);
        String command;

        while (true) {
            try {
                command = scanner.nextLine().trim();
            } catch (NoSuchElementException e) {
                break;
            }

            Command handler = getCommand(command);

            if (handler != null) {
                try {
                    handler.execute();
                } catch (Throwable t) {
                    Logger.error("Cannot execute command:", t);
                }
                continue;
            }

            Logger.info("Unknown command. Type \"help\" to get commands list");
        }
    }

    public void registerAll(LimboServer server) {
        register("help", new CmdHelp(server));
        register("conn", new CmdConn(server));
        register("mem", new CmdMem());
        register("stop", new CmdStop());
    }
}
