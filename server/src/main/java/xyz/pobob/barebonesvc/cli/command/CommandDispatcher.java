package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVCServer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CommandDispatcher {

    private final Map<String, Command> commands = new HashMap<>();

    public void register(String name, Command command) {
        this.commands.put(name, command);
    }

    public void dispatch(String input) {
        String[] parts = input.split("\\s+");

        Command command = commands.get(parts[0].toLowerCase(Locale.ROOT));

        if (command == null) {
            BareBonesVCServer.LOGGER.warning("Unknown command");
            return;
        }

        command.execute(parts);
    }
}