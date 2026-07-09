package xyz.pobob.barebonesvc.packet;

import xyz.pobob.barebonesvc.util.Bytes;

/**
 * [VOICE DISTANCE : 8][IS WHISPER : 1]
 */
public class ServerUpdateVoiceDistancePacket extends ReliablePacket {

    private double voiceDistance;
    private boolean isWhisperDistance;

    public double getVoiceDistance() {
        return this.voiceDistance;
    }

    public boolean isWhisperDistance() {
        return this.isWhisperDistance;
    }

    public void create(double voiceDistance, boolean isWhisperDistance) {
        this.voiceDistance = voiceDistance;
        this.isWhisperDistance = isWhisperDistance;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(
                this.createHeader(9),
                Bytes.of(Double.doubleToLongBits(this.voiceDistance)),
                new byte[] {(byte) (this.isWhisperDistance ? 1 : 0)}
        );
    }

    @Override
    public void deserialize(byte[] data) {
        int start = this.getPayloadStart();

        this.voiceDistance = Double.longBitsToDouble(Bytes.getLong(data, start));
        this.isWhisperDistance = (data[start + 8] & 1) == 1;
    }
}
