package xyz.pobob.barebonesvc.packet;

import xyz.pobob.barebonesvc.util.Bytes;

/**
 * [VOICE DISTANCE : 8]
 */
public class ServerUpdateVoiceDistancePacket extends ReliablePacket {

    private double voiceDistance;

    public double getVoiceDistance() {
        return this.voiceDistance;
    }

    public void create(double voiceDistance) {
        this.voiceDistance = voiceDistance;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(
                this.createHeader(8),
                Bytes.of(Double.doubleToLongBits(this.voiceDistance))
        );
    }

    @Override
    public void deserialize(byte[] data) {
        this.voiceDistance = Double.longBitsToDouble(Bytes.getLong(data, this.getPayloadStart()));
    }
}
