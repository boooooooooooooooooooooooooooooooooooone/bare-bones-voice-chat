package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;

public class HelpCommand implements Command {
    @Override
    public void execute(String[] args, BareBonesVCServer server) {
        BareBonesVC.LOGGER.info("Current commands:\n"
                + String.join("\n", CommandDispatcher.getCommandRoots().stream().sorted().toList()));
    }
}