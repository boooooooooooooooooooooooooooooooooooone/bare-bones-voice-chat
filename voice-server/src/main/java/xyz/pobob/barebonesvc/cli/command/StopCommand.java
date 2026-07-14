package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;

public class StopCommand implements Command {

    @Override
    public void execute(String[] args, BareBonesVCServer server) {
        System.exit(0);
    }
}
