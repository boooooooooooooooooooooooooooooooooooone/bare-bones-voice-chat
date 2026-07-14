package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.packet.ServerAudioPacket;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public class ServerAudioHandler implements ServerPacketHandler {

    private final ServerAudioPacket serverAudioPacket = new ServerAudioPacket();

    @Override
    public void handle(byte[] data) {
        if (BareBonesVCClient.INSTANCE.isOurSVCRunning()) {
            this.serverAudioPacket.deserialize(data);
            BareBonesVCClient.INSTANCE.passSoundPacketToSVC(
                    this.serverAudioPacket.getAudio(),
                    this.serverAudioPacket.getSequenceNumber(),
                    this.serverAudioPacket.getUUID(),
                    this.serverAudioPacket.isWhispering()
            );
        }
    }
}