package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.packet.ClientUpdatePlayerPacket;
import xyz.pobob.barebonesvc.packet.ServerUpdatePlayerPacket;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;

import java.net.SocketAddress;

public class ClientUpdatePlayerHandler implements ClientPacketHandler {

    private final BareBonesVCServer server;

    public ClientUpdatePlayerHandler(BareBonesVCServer server) {
        this.server = server;
    }

    private final ThreadLocal<ClientUpdatePlayerPacket> localClientUpdatePlayerPacket = ThreadLocal.withInitial(ClientUpdatePlayerPacket::new);
    private final ThreadLocal<ServerUpdatePlayerPacket> localServerUpdatePlayerPacket = ThreadLocal.withInitial(ServerUpdatePlayerPacket::new);

    @Override
    public void handle(byte[] data, SocketAddress clientAddress) {

        ClientConnection conn = this.server.getAuthenticatedClient(clientAddress);
        if (conn != null) {

            this.localClientUpdatePlayerPacket.get().deserialize(data);

            if (this.localClientUpdatePlayerPacket.get().isDisconnected()) {
                this.server.onDisconnect(clientAddress);
            } else {
                conn.setDisabled(this.localClientUpdatePlayerPacket.get().isDisabled());
                this.localServerUpdatePlayerPacket.get().create(
                        conn.getUsername(),
                        conn.getUUID(),
                        this.localClientUpdatePlayerPacket.get().isDisabled(),
                        false,
                        false
                );

                this.server.announceExcluding(this.localServerUpdatePlayerPacket.get(), clientAddress);
            }
        }
    }
}
