package xyz.pobob.barebonesvc.packet;

import xyz.pobob.barebonesvc.packet.retransmission.ReliablePacket;
import xyz.pobob.barebonesvc.util.Bytes;
import xyz.pobob.barebonesvc.voice.Codec;

import java.util.Arrays;

/**
 * [MOJANG AUTH + CODEC : 1][VOICE DISTANCE : 8][WHISPER DISTANCE : 8][PUBLIC KEY : 162]
 */
public class ServerHelloPacket extends ReliablePacket {

    private static final int ENCODED_PUBLIC_KEY_LENGTH = 162;

    private boolean mojangAuth;
    private double voiceDistance;
    private double whisperDistance;
    private Codec codec;
    private byte[] publicKey;

    public boolean getMojangAuth() {
        return this.mojangAuth;
    }

    public double getVoiceDistance() {
        return this.voiceDistance;
    }

    public double getWhisperDistance() {
        return this.whisperDistance;
    }

    public Codec getCodec() {
        return this.codec;
    }

    public byte[] getPublicKey() {
        return this.publicKey;
    }

    public void create(boolean mojangAuth, double voiceDistance, double whisperDistance, Codec codec, byte[] publicKey) {
        this.mojangAuth = mojangAuth;
        this.voiceDistance = voiceDistance;
        this.whisperDistance = whisperDistance;
        this.codec = codec;
        this.publicKey = publicKey;
    }

    @Override
    public byte[] serialize() {
        int len = this.mojangAuth ? (17 + ENCODED_PUBLIC_KEY_LENGTH) : 17;

        return Bytes.join(
                this.createHeader(len),
                codecAsByte(),
                Bytes.of(Double.doubleToLongBits(this.voiceDistance)),
                Bytes.of(Double.doubleToLongBits(this.whisperDistance)),
                this.publicKey
        );
    }

    @Override
    public void deserialize(byte[] data) {
        int start = this.getPayloadStart();
        short len = Packet.getPayloadLength(data);

        this.mojangAuth = (len != 17);
        this.codec = switch (data[start] & 3) {
            case 0 -> Codec.VOIP;
            case 1 -> Codec.AUDIO;
            default -> Codec.RESTRICTED_LOWDELAY;
        };
        this.voiceDistance = Double.longBitsToDouble(Bytes.getLong(data, start + 1));
        this.whisperDistance = Double.longBitsToDouble(Bytes.getLong(data, start + 9));
        if (this.mojangAuth) {
            this.publicKey = Arrays.copyOfRange(data, start + len - ENCODED_PUBLIC_KEY_LENGTH, start + len);
        }
    }

    private byte[] codecAsByte() {
        return new byte[] {
                switch (this.codec) {
                    case VOIP -> 0;
                    case AUDIO -> 1;
                    case RESTRICTED_LOWDELAY -> 2;
                }
        };
    }
}