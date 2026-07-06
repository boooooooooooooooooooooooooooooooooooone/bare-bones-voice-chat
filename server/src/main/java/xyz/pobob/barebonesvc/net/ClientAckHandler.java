package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

import java.net.SocketAddress;

public class ClientAckHandler extends ClientPacketHandler {

    public ClientAckHandler(VoiceServer server) {
        super(server);
    }

    private final ThreadLocal<ClientAckPacket> localClientKeepAlivePacket = ThreadLocal.withInitial(ClientAckPacket::new);

    @Override
    public void handle(byte[] data, SocketAddress clientAddress) {
        this.localClientKeepAlivePacket.get().deserialize(data);
        this.server.reliablePacketManager.onClientAcknowledge(this.localClientKeepAlivePacket.get().getSequence(), clientAddress);
    }
}
