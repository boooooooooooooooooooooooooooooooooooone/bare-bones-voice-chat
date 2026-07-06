package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public class ServerAckHandler implements ServerPacketHandler {

    private final ServerAckPacket serverAckPacket = new ServerAckPacket();

    @Override
    public void handle(byte[] data) {
        this.serverAckPacket.deserialize(data);
        BareBonesVCClient.INSTANCE.reliablePacketManager.onServerAcknowledge(this.serverAckPacket.getSequence());
    }
}
