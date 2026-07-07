package xyz.pobob.barebonesvc.packet;

import xyz.pobob.barebonesvc.util.Bytes;
import xyz.pobob.barebonesvc.voice.Codec;

/**
 * [MOJANG AUTH + CODEC : 1][VOICE DISTANCE : 8]
 */
public class ServerHelloPacket extends ReliablePacket implements Packet {

    private boolean mojangAuth;
    private double voiceDistance;
    private Codec codec;

    public boolean getMojangAuth() {
        return this.mojangAuth;
    }

    public double getVoiceDistance() {
        return this.voiceDistance;
    }

    public Codec getCodec() {
        return this.codec;
    }

    public void create(boolean mojangAuth, double voiceDistance, Codec codec) {
        this.mojangAuth = mojangAuth;
        this.voiceDistance = voiceDistance;
        this.codec = codec;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(
                this.createHeader(9),
                encodeSmallData(),
                Bytes.of(Double.doubleToLongBits(this.voiceDistance))
        );
    }

    @Override
    public void deserialize(byte[] data) {
        int start = this.getPayloadIndex();

        this.mojangAuth = (data[start] & 1) == 1;
        this.codec = switch (data[start] & 6) {
            case 0 -> Codec.VOIP;
            case 2 -> Codec.AUDIO;
            default -> Codec.RESTRICTED_LOWDELAY;
        };
        this.voiceDistance = Double.longBitsToDouble(Bytes.getLong(data, start + 1));
    }

    private byte[] encodeSmallData() {
        return new byte[] {
                (byte) ((this.mojangAuth ? 1 : 0)
                        | switch (this.codec) {
                    case VOIP -> 0;
                    case AUDIO -> 2;
                    case RESTRICTED_LOWDELAY -> 4;
                })
        };
    }
}