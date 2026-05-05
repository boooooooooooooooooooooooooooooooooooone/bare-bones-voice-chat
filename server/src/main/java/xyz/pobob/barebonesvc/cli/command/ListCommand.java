package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

public class ListCommand implements Command {

    private final VoiceServer server;

    public ListCommand(VoiceServer server) {
        this.server = server;
    }

    @Override
    public void execute(String[] args) {
        int n = this.server.connected.size();
        BareBonesVCServer.LOGGER.info(
                "There " + (n == 1 ? ("is currently 1 player") : ("are currently " + n + " players")) + " connected: " +
                        String.join(
                        ", ",
                                this.server.connected.values().stream().map(ClientConnection::getUsername).toList()
                        )
        );
    }
}
