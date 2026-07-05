package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.voice.BareBonesVCClient;

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
