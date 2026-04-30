package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;
import xyz.pobob.barebonesvc.voice.Codec;

/**
 * [MOJANG AUTH + GROUPS ENABLED + CODEC : 1][MTU SIZE : 4][KEEP ALIVE : 4][VOICE DISTANCE : 8]
 */
public class ServerHelloPacket extends Packet {

    private boolean mojangAuth;
    private int mtuSize;
    private int keepAliveInterval;
    private double voiceDistance;
    private Codec codec;
    private boolean groupsEnabled;

    public boolean getMojangAuth() {
        return this.mojangAuth;
    }

    public int getMtuSize() {
        return this.mtuSize;
    }

    public int getKeepAliveInterval() {
        return this.keepAliveInterval;
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

    public void create(boolean mojangAuth, int mtuSize, int keepAliveInterval, double voiceDistance, Codec codec, boolean groupsEnabled) {
        this.mojangAuth = mojangAuth;
        this.mtuSize = mtuSize;
        this.keepAliveInterval = keepAliveInterval;
        this.voiceDistance = voiceDistance;
        this.codec = codec;
        this.groupsEnabled = groupsEnabled;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(
                Type.SERVER_HELLO.createHeader(17),
                encodeSmallData(),
                Bytes.of(this.mtuSize),
                Bytes.of(this.keepAliveInterval),
                Bytes.of(Double.doubleToLongBits(this.voiceDistance))
        );
    }

    @Override
    public void deserialize(byte[] data) {
        this.decodeSmallData(data);
        this.mtuSize = Bytes.getInt(data, 6);
        this.keepAliveInterval = Bytes.getInt(data, 10);
        this.voiceDistance = Double.longBitsToDouble(Bytes.getLong(data, 14));
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