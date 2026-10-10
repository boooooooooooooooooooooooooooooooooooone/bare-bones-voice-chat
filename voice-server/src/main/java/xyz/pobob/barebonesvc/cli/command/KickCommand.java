package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.packet.ServerKickPacket;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KickCommand implements Command {

    private final ServerKickPacket serverKickPacket = new ServerKickPacket();

    public KickCommand() {
        this.serverKickPacket.create(false);
    }

    @Override
    public void execute(String[] args, BareBonesVCServer server) {
        if (args.length == 0) {
            BareBonesVC.LOGGER.warning("No client specified");
            return;
        }

        var targets = getTargets(args[0], server);
        if (!targets.isEmpty()) {
            for (var target : targets) {
                server.send(this.serverKickPacket, target.getKey());
                server.onDisconnect(target.getKey());

                BareBonesVC.LOGGER.info("Kicked client: " + target.getValue().getUsername() + " (" + target.getValue().getUUID() + ")");
            }
        } else {
            BareBonesVC.LOGGER.warning("Unable to find target client");
        }
    }

    public static List<Map.Entry<SocketAddress, ClientConnection>> getTargets(String target, BareBonesVCServer server) {
        List<Map.Entry<SocketAddress, ClientConnection>> kicked = new ArrayList<>();

        for (var entry : server.getAuthenticatedEntries()) {
            if (entry.getValue().getUsername().equalsIgnoreCase(target)
                    || entry.getValue().getUUID().toString().equalsIgnoreCase(target)) {
                kicked.add(entry);
            }
        }

        return kicked;
    }
}
