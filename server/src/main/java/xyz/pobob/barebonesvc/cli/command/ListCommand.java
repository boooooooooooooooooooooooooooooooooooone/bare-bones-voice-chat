package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;

public class ListCommand implements Command {

    @Override
    public void execute(String[] args, BareBonesVCServer server) {
        int n = server.getConnectedCount();
        BareBonesVC.LOGGER.info(
                "There " + (n == 1 ? ("is currently 1 player") : ("are currently " + n + " players")) + " connected: " +
                        String.join(", ", server.getAuthenticatedClients().stream().map(ClientConnection::getUsername).toList())
        );
    }
}