package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.client.BareBonesVCClient;
import xyz.pobob.barebonesvc.packet.ServerUpdateVoiceDistancePacket;

public class ServerUpdateVoiceDistanceHandler implements ServerPacketHandler {

    private final ServerUpdateVoiceDistancePacket serverUpdateVoiceDistancePacket = new ServerUpdateVoiceDistancePacket();

    @Override
    public void handle(byte[] data) {
        if (BareBonesVCClient.instance.config != null) {
            this.serverUpdateVoiceDistancePacket.deserialize(data);

            float value = (float) this.serverUpdateVoiceDistancePacket.getVoiceDistance();
            if (this.serverUpdateVoiceDistancePacket.isWhisperDistance()) {
                BareBonesVCClient.instance.config.setWhisperDistance(value);
                BareBonesVCClient.instance.sendFeed("Whisper distance was set to " + value);
            } else {
                BareBonesVCClient.instance.config.setVoiceDistance(value);
                BareBonesVCClient.instance.sendFeed("Voice distance was set to " + value);
            }
        }
    }
}
