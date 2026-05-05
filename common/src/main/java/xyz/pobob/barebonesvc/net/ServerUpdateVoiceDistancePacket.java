package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

/**
 * [VOICE DISTANCE : 8]
 */
public class ServerUpdateVoiceDistancePacket extends Packet {

    private double voiceDistance;

    public double getVoiceDistance() {return this.voiceDistance;}

    public void create(double voiceDistance) {
        this.voiceDistance = voiceDistance;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(
                Type.SERVER_UPDATE_VOICE_DISTANCE.createHeader(8),
                Bytes.of(Double.doubleToLongBits(this.voiceDistance))
        );
    }

    @Override
    public void deserialize(byte[] data) {
        this.voiceDistance = Double.longBitsToDouble(Bytes.getLong(data, 5));
    }
}
