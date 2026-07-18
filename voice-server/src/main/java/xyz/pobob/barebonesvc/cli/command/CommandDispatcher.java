package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;

import java.util.*;

public class CommandDispatcher {

    private static final Map<String, Command> COMMANDS = new HashMap<>();

    static {
        COMMANDS.put("help", new HelpCommand());
        COMMANDS.put("stop", new StopCommand());
        COMMANDS.put("list", new ListCommand());
        COMMANDS.put("kick", new KickCommand());
        COMMANDS.put("voicedistance", new VoiceDistanceCommand());
        COMMANDS.put("whisperdistance", new WhisperDistanceCommand());
    }

    private final BareBonesVCServer server;

    public CommandDispatcher(BareBonesVCServer server) {
        this.server = server;
    }

    public void dispatch(String input) {
        String[] parts = input.split("\\s+");

        Command command = COMMANDS.get(parts[0].toLowerCase(Locale.ROOT));

        if (command == null) {
            BareBonesVC.LOGGER.warning("Unknown command");
            return;
        }

        command.execute(Arrays.copyOfRange(parts, 1, parts.length), this.server);
    }

    public static Set<String> getCommandRoots() {
        return COMMANDS.keySet();
    }
}