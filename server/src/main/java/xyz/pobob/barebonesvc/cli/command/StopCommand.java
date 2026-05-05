package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

public class StopCommand implements Command {

    private final VoiceServer server;

    public StopCommand(VoiceServer server) {
        this.server = server;
    }

    @Override
    public void execute(String[] args) {
        BareBonesVCServer.LOGGER.info("Stopping Bare Bones VC server");
        this.server.close();
        System.exit(0);
    }
}
