package xyz.pobob.barebonesvc.packet.handler;

import de.maxhenkel.voicechat.voice.common.PlayerSoundPacket;
import xyz.pobob.barebonesvc.packet.ServerAudioPacket;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public class ServerAudioHandler implements ServerPacketHandler {

    private final ServerAudioPacket serverAudioPacket = new ServerAudioPacket();

    @Override
    public void handle(byte[] data) {
        if (BareBonesVCClient.INSTANCE.client != null) {
            this.serverAudioPacket.deserialize(data);
            BareBonesVCClient.INSTANCE.client.processSoundPacket(
                    new PlayerSoundPacket(
                            this.serverAudioPacket.getUUID(),
                            this.serverAudioPacket.getUUID(),
                            this.serverAudioPacket.getAudio(),
                            this.serverAudioPacket.getSequenceNumber(),
                            this.serverAudioPacket.isWhispering(),
                            this.serverAudioPacket.isWhispering() ?
                                    BareBonesVCClient.INSTANCE.config.getWhisperDistance() :
                                    BareBonesVCClient.INSTANCE.config.getVoiceDistance(),
                            null
                    )
            );
        }
    }
}