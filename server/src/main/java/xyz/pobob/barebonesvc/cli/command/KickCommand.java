package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.packet.ServerKickPacket;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;

import java.net.SocketAddress;
import java.util.Map;

public class KickCommand implements Command {

    private final ServerKickPacket serverKickPacket = new ServerKickPacket();

    @Override
    public void execute(String[] args, BareBonesVCServer server) {
        if (args.length == 0) {
            BareBonesVC.LOGGER.warning("No client specified");
            return;
        }

        String target = args[0];

        boolean didntKick = true;
        for (Map.Entry<SocketAddress, ClientConnection> entry : server.getAuthenticatedEntries()) {
            if (entry.getValue().getUsername().equalsIgnoreCase(target)
                    || entry.getValue().getUUID().toString().equalsIgnoreCase(target)) {
                didntKick = false;

                server.send(this.serverKickPacket, entry.getKey());
                server.onDisconnect(entry.getKey());

                BareBonesVC.LOGGER.info("Kicked " + entry.getValue().getUsername());
            }
        }

        if (didntKick) {
            BareBonesVC.LOGGER.info("No client found");
        }
    }
}
