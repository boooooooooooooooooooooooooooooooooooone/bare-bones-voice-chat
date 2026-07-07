package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.packet.ServerUpdateVoiceDistancePacket;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public class ServerUpdateVoiceDistanceHandler implements ServerPacketHandler {

    private final ServerUpdateVoiceDistancePacket serverUpdateVoiceDistancePacket = new ServerUpdateVoiceDistancePacket();

    @Override
    public void handle(byte[] data) {
        if (BareBonesVCClient.INSTANCE.config != null) {
            this.serverUpdateVoiceDistancePacket.deserialize(data);
            BareBonesVCClient.INSTANCE.config.setVoiceDistance((float) this.serverUpdateVoiceDistancePacket.getVoiceDistance());
        }
    }
}
