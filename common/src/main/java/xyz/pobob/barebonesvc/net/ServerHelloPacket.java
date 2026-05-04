package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;
import xyz.pobob.barebonesvc.voice.Codec;

/**
 * [MOJANG AUTH + GROUPS ENABLED + CODEC : 1][VOICE DISTANCE : 8]
 */
public class ServerHelloPacket extends Packet {

    private boolean mojangAuth;
    private double voiceDistance;
    private Codec codec;
    private boolean groupsEnabled;

    public boolean getMojangAuth() {
        return this.mojangAuth;
    }

    public double getVoiceDistance() {
        return this.voiceDistance;
    }

    public Codec getCodec() {
        return this.codec;
    }

    public boolean getGroupsEnabled() {
        return this.groupsEnabled;
    }

    public void create(boolean mojangAuth, double voiceDistance, Codec codec, boolean groupsEnabled) {
        this.mojangAuth = mojangAuth;
        this.voiceDistance = voiceDistance;
        this.codec = codec;
        this.groupsEnabled = groupsEnabled;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(
                Type.SERVER_HELLO.createHeader(9),
                encodeSmallData(),
                Bytes.of(Double.doubleToLongBits(this.voiceDistance))
        );
    }

    @Override
    public void deserialize(byte[] data) {
        this.decodeSmallData(data);
        this.voiceDistance = Double.longBitsToDouble(Bytes.getLong(data, 6));
    }

    private byte[] encodeSmallData() {
        return new byte[] {
                (byte) ((this.mojangAuth ? 1 : 0)
                        | (this.groupsEnabled ? 2 : 0)
                        | switch (this.codec) {
                    case VOIP -> 4;
                    case AUDIO -> 8;
                    case RESTRICTED_LOWDELAY -> 12;
                })
        };
    }

    private void decodeSmallData(byte[] data) {
        this.mojangAuth = (data[5] & 1) == 1;
        this.groupsEnabled = (data[5] & 2) == 2;
        this.codec = switch (data[5] & 12) {
            case 4 -> Codec.VOIP;
            case 8 -> Codec.AUDIO;
            default -> Codec.RESTRICTED_LOWDELAY;
        };
    }
}