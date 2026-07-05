package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.voiceserver.ClientConnection;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

import java.net.SocketAddress;

public class ClientKeepAliveHandler implements ClientPacketHandler {

    private final VoiceServer server;

    public ClientKeepAliveHandler(VoiceServer server) {
        this.server = server;
    }

    private final ThreadLocal<ClientKeepAlivePacket> localClientKeepAlivePacket = ThreadLocal.withInitial(ClientKeepAlivePacket::new);

    @Override
    public void handle(byte[] data, SocketAddress clientAddress) {
        ClientConnection client = this.server.connected.get(clientAddress);
        if (client != null) {
            client.setLastKeepAliveResponse(System.currentTimeMillis());

            this.localClientKeepAlivePacket.get().deserialize(data);
            this.server.latencyManager.updateClientLatency(client, this.localClientKeepAlivePacket.get().getId());
        }
    }
}
