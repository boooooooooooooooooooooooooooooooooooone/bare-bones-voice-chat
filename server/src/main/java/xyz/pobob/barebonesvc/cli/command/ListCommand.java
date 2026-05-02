package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

import java.util.stream.Collectors;

public class ListCommand implements Command {

    private final VoiceServer voiceServer;

    public ListCommand(VoiceServer voiceServer) {
        this.voiceServer = voiceServer;
    }

    @Override
    public void execute(String[] args) {

        int n = this.voiceServer.connected.size();
        BareBonesVCServer.LOGGER.info(
                "There " + (n == 1 ? ("is currently 1 player") : ("are currently " + n + " players")) + " connected: " +
                        String.join(
                        ", ",
                                this.voiceServer.connected.values().stream().map(ClientConnection::getUsername).collect(Collectors.toSet())
                        )
        );
    }
}
