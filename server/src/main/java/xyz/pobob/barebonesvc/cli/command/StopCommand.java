package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

public class StopCommand implements Command {

    private final VoiceServer voiceServer;

    public StopCommand(VoiceServer voiceServer) {
        this.voiceServer = voiceServer;
    }

    @Override
    public void execute(String[] args) {
        BareBonesVCServer.LOGGER.info("Stopping Bare Bones VC server");
        this.voiceServer.close();
        System.exit(0);
    }
}
