package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.packet.ServerKickPacket;
import xyz.pobob.barebonesvc.packet.ServerUpdatePlayerPacket;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;

import java.net.SocketAddress;
import java.util.Map;

public class KickCommand implements Command {

    private final BareBonesVCServer server;
    private final ServerKickPacket serverKickPacket = new ServerKickPacket();
    private final ServerUpdatePlayerPacket serverUpdatePlayerPacket = new ServerUpdatePlayerPacket();

    public KickCommand(BareBonesVCServer server) {
        this.server = server;
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            BareBonesVC.LOGGER.warning("No client specified");
            return;
        }

        String target = args[0];

        boolean didntKick = true;
        for (Map.Entry<SocketAddress, ClientConnection> entry : this.server.connected.entrySet()) {
            if (entry.getValue().getUsername().equalsIgnoreCase(target)
                    || entry.getValue().getUUID().toString().equalsIgnoreCase(target)) {
                didntKick = false;

                this.server.send(this.serverKickPacket, entry.getKey());
                this.serverUpdatePlayerPacket.create(
                        entry.getValue().getUsername(),
                        entry.getValue().getUUID(),
                        entry.getValue().isDisabled(),
                        true
                );
                this.server.announceExcluding(this.serverUpdatePlayerPacket, entry.getKey());
                this.server.connected.remove(entry.getKey());

                BareBonesVC.LOGGER.info("Client kicked: " + entry.getValue().getUsername() + " (" + entry.getValue().getUUID() + ")");
            }
        }

        if (didntKick) {
            BareBonesVC.LOGGER.info("No client found");
        }
    }
}
