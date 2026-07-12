package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.packet.ClientAckPacket;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;

import java.net.SocketAddress;

public class ClientAckHandler implements ClientPacketHandler {
    
    private final BareBonesVCServer server;

    public ClientAckHandler(BareBonesVCServer server) {
        this.server = server;
    }

    private final ThreadLocal<ClientAckPacket> localClientKeepAlivePacket = ThreadLocal.withInitial(ClientAckPacket::new);

    @Override
    public void handle(byte[] data, SocketAddress clientAddress) {
        this.localClientKeepAlivePacket.get().deserialize(data);
        this.server.reliablePacketManager.onClientAcknowledge(this.localClientKeepAlivePacket.get().getSequence(), clientAddress);
    }
}
