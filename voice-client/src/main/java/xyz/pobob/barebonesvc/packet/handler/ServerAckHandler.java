package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.client.BareBonesVCClient;
import xyz.pobob.barebonesvc.packet.ServerAckPacket;

public class ServerAckHandler implements ServerPacketHandler {

    private final ServerAckPacket serverAckPacket = new ServerAckPacket();

    @Override
    public void handle(byte[] data) {
        this.serverAckPacket.deserialize(data);
        BareBonesVCClient.INSTANCE.reliablePacketManager.onServerAcknowledge(this.serverAckPacket.getSequence());
    }
}
