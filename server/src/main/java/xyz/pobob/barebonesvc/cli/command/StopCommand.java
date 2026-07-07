package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;

public class StopCommand implements Command {

    private final BareBonesVCServer server;

    public StopCommand(BareBonesVCServer server) {
        this.server = server;
    }

    @Override
    public void execute(String[] args) {
        BareBonesVC.LOGGER.info("Stopping Bare Bones VC server");
        this.server.close();
        System.exit(0);
    }
}
