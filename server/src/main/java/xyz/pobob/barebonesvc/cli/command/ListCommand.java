package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;

public class ListCommand implements Command {

    private final BareBonesVCServer server;

    public ListCommand(BareBonesVCServer server) {
        this.server = server;
    }

    @Override
    public void execute(String[] args) {
        int n = this.server.connected.size();
        BareBonesVC.LOGGER.info(
                "There " + (n == 1 ? ("is currently 1 player") : ("are currently " + n + " players")) + " connected: " +
                        String.join(
                        ", ",
                                this.server.connected.values().stream().map(ClientConnection::getUsername).toList()
                        )
        );
    }
}
