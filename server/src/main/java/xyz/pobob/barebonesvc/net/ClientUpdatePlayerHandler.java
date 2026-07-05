package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class ClientUpdatePlayerHandler implements ClientPacketHandler {

    private final VoiceServer server;

    public ClientUpdatePlayerHandler(VoiceServer server) {
        this.server = server;
    }

    private final ThreadLocal<ClientUpdatePlayerPacket> localClientUpdatePlayerPacket = ThreadLocal.withInitial(ClientUpdatePlayerPacket::new);
    private final ThreadLocal<ServerUpdatePlayerPacket> localServerUpdatePlayerPacket = ThreadLocal.withInitial(ServerUpdatePlayerPacket::new);

    @Override
    public void handle(byte[] data, SocketAddress clientAddress) {
        if (this.server.connected.containsKey(clientAddress)) {
            this.localClientUpdatePlayerPacket.get().deserialize(data);

            if (this.localClientUpdatePlayerPacket.get().isDisconnected()) {
                ClientConnection disconnected = this.server.connected.remove(clientAddress);
                this.localServerUpdatePlayerPacket.get().create(
                        disconnected.getUsername(),
                        disconnected.getUUID(),
                        this.localClientUpdatePlayerPacket.get().isDisabled(),
                        true
                );

                BareBonesVCServer.LOGGER.info("Client disconnected: " + disconnected.getUsername() + " (" + disconnected.getUUID() + ")");
            } else {
                ClientConnection client = this.server.connected.get(clientAddress);
                client.setDisabled(this.localClientUpdatePlayerPacket.get().isDisabled());
                this.localServerUpdatePlayerPacket.get().create(
                        client.getUsername(),
                        client.getUUID(),
                        this.localClientUpdatePlayerPacket.get().isDisabled(),
                        false
                );
            }

            byte[] serialized = this.localServerUpdatePlayerPacket.get().serialize();
            this.server.announceExcluding(serialized, clientAddress);
            this.server.scheduler.schedule(() -> this.server.announceExcluding(serialized, clientAddress), 1000, TimeUnit.MILLISECONDS);
            this.server.scheduler.schedule(() -> this.server.announceExcluding(serialized, clientAddress), 2000, TimeUnit.MILLISECONDS);
        }
    }
}
