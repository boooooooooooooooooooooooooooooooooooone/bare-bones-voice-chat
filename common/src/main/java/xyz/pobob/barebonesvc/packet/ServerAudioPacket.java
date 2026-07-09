package xyz.pobob.barebonesvc.packet;

import xyz.pobob.barebonesvc.util.Bytes;

import java.util.Arrays;
import java.util.UUID;

/**
 * [AUDIO DATA : len - 8 - 1 - 16][SEQUENCE NUMBER : 8][WHISPERING : 1][UUID : 16]
 */
public class ServerAudioPacket implements Packet {

    private byte[] audio;
    private long sequenceNumber;
    private boolean whispering;
    private UUID uuid;

    public byte[] getAudio() {
        return this.audio;
    }

    public long getSequenceNumber() {
        return this.sequenceNumber;
    }

    public boolean isWhispering() {
        return this.whispering;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void create(ClientAudioPacket packet, UUID uuid) {
        this.audio = packet.getAudio();
        this.sequenceNumber = packet.getSequenceNumber();
        this.whispering = packet.isWhispering();
        this.uuid = uuid;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(
                this.createHeader(this.audio.length + 8 + 1 + 16),
                this.audio,
                Bytes.of(this.sequenceNumber),
                new byte[] {(byte) (this.whispering ? 1 : 0)},
                Bytes.of(this.uuid.getMostSignificantBits()),
                Bytes.of(this.uuid.getLeastSignificantBits())
        );
    }

    @Override
    public void deserialize(byte[] data) {
        int start = this.getPayloadStart();
        short len = Packet.getPayloadLength(data);

        this.audio = Arrays.copyOfRange(data, start, start + len - 8 - 1 - 16);
        this.sequenceNumber = Bytes.getLong(data, start + len - 8 - 1 - 16);
        this.whispering = (data[start + len - 1 - 16] & 1) == 1;
        this.uuid = new UUID(
                Bytes.getLong(data, start + len - 16),
                Bytes.getLong(data, start + len - 8)
        );
    }
}
