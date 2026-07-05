package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

/**
 * [VOICE DISTANCE : 8]
 */
public class ServerUpdateVoiceDistancePacket implements Packet {

    private double voiceDistance;

    public double getVoiceDistance() {return this.voiceDistance;}

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
        this.voiceDistance = Double.longBitsToDouble(Bytes.getLong(data, 5));
    }

    @Override
    public byte[] createHeader(int len) {
        return Bytes.join(
                new byte[] {
                        Packet.MAGIC_BYTE,
                        Packet.VERSION,
                        PacketType.SERVER_UPDATE_VOICE_DISTANCE.value
                },
                Bytes.of((short) len)
        );
    }
}
