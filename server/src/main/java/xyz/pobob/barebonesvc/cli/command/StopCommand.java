package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;

public class StopCommand implements Command {

    @Override
    public void execute(String[] args, BareBonesVCServer server) {
        BareBonesVC.LOGGER.info("Stopping Bare Bones VC server");
        server.close();
        System.exit(0);
    }
}
