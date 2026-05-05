package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVCServer;
import xyz.pobob.barebonesvc.net.ServerKickPlayerPacket;
import xyz.pobob.barebonesvc.net.ServerUpdatePlayerPacket;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

import java.net.SocketAddress;
import java.util.Map;

public class KickCommand implements Command {

    private final VoiceServer server;
    private final ServerKickPlayerPacket serverKickPlayerPacket = new ServerKickPlayerPacket();
    private final ServerUpdatePlayerPacket serverUpdatePlayerPacket = new ServerUpdatePlayerPacket();

    public KickCommand(VoiceServer server) {
        this.server = server;
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            BareBonesVCServer.LOGGER.warning("No client specified");
            return;
        }

        String target = args[0];

        boolean didntKick = true;
        for (Map.Entry<SocketAddress, ClientConnection> entry : this.server.connected.entrySet()) {
            if (entry.getValue().getUsername().equalsIgnoreCase(target)
                    || entry.getValue().getUUID().toString().equalsIgnoreCase(target)) {
                didntKick = false;

                this.server.send(this.serverKickPlayerPacket.serialize(), entry.getKey());
                this.serverUpdatePlayerPacket.create(
                        entry.getValue().getUsername(),
                        entry.getValue().getUUID(),
                        entry.getValue().isDisabled(),
                        true
                );
                this.server.announceExcluding(this.serverUpdatePlayerPacket.serialize(), entry.getKey());
                this.server.connected.remove(entry.getKey());

                BareBonesVCServer.LOGGER.info("Client kicked: " + entry.getValue().getUsername() + " (" + entry.getValue().getUUID() + ")");
            }
        }

        if (didntKick) {
            BareBonesVCServer.LOGGER.info("No client found");
        }
    }
}
