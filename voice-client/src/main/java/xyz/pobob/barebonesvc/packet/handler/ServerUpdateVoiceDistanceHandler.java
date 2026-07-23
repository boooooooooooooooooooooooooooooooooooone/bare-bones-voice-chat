package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.client.BareBonesVCClient;
import xyz.pobob.barebonesvc.packet.ServerUpdateVoiceDistancePacket;

public class ServerUpdateVoiceDistanceHandler implements ServerPacketHandler {

    private final ServerUpdateVoiceDistancePacket serverUpdateVoiceDistancePacket = new ServerUpdateVoiceDistancePacket();

    @Override
    public void handle(byte[] data) {
        if (BareBonesVCClient.INSTANCE.config != null) {
            this.serverUpdateVoiceDistancePacket.deserialize(data);

            float value = (float) this.serverUpdateVoiceDistancePacket.getVoiceDistance();
            if (this.serverUpdateVoiceDistancePacket.isWhisperDistance()) {
                BareBonesVCClient.INSTANCE.config.setWhisperDistance(value);
                BareBonesVCClient.INSTANCE.sendFeed("Whisper distance was set to " + value);
            } else {
                BareBonesVCClient.INSTANCE.config.setVoiceDistance(value);
                BareBonesVCClient.INSTANCE.sendFeed("Voice distance was set to " + value);
            }
        }
    }
}
