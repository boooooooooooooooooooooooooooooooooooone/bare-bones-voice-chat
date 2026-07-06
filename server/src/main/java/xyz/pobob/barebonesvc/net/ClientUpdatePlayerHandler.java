package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.voiceserver.ClientConnection;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

import java.net.SocketAddress;

public class ClientUpdatePlayerHandler extends ClientPacketHandler {

    public ClientUpdatePlayerHandler(VoiceServer server) {
        super(server);
    }

    private final ThreadLocal<ClientUpdatePlayerPacket> localClientUpdatePlayerPacket = ThreadLocal.withInitial(ClientUpdatePlayerPacket::new);
    private final ThreadLocal<ServerUpdatePlayerPacket> localServerUpdatePlayerPacket = ThreadLocal.withInitial(ServerUpdatePlayerPacket::new);

    @Override
    public void handle(byte[] data, SocketAddress clientAddress) {
        if (this.server.connected.containsKey(clientAddress)) {
            this.localClientUpdatePlayerPacket.get().deserialize(data);

            if (this.localClientUpdatePlayerPacket.get().isDisconnected()) {
                this.server.onDisconnect(clientAddress);
            } else {
                ClientConnection client = this.server.connected.get(clientAddress);
                client.setDisabled(this.localClientUpdatePlayerPacket.get().isDisabled());
                this.localServerUpdatePlayerPacket.get().create(
                        client.getUsername(),
                        client.getUUID(),
                        this.localClientUpdatePlayerPacket.get().isDisabled(),
                        false
                );

                this.server.announceExcluding(this.localServerUpdatePlayerPacket.get(), clientAddress);
            }
        }
    }
}
