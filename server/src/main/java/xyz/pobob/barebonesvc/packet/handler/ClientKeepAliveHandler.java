package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.packet.ClientKeepAlivePacket;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;

import java.net.SocketAddress;

public class ClientKeepAliveHandler implements ClientPacketHandler {

    private final BareBonesVCServer server;

    public ClientKeepAliveHandler(BareBonesVCServer server) {
        this.server = server;
    }

    private final ThreadLocal<ClientKeepAlivePacket> localClientKeepAlivePacket = ThreadLocal.withInitial(ClientKeepAlivePacket::new);

    @Override
    public void handle(byte[] data, SocketAddress clientAddress) {
        ClientConnection client = this.server.connected.get(clientAddress);
        if (client != null) {
            client.setLastKeepAlive(System.currentTimeMillis());

            this.localClientKeepAlivePacket.get().deserialize(data);
            this.server.latencyManager.updateClientLatency(client, this.localClientKeepAlivePacket.get().getId());
        }
    }
}
