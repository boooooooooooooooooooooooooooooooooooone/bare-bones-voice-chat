package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

import java.util.Arrays;
import java.util.UUID;

/**
 * [AUDIO DATA : len - 8 - 16][SEQUENCE NUMBER : 8][UUID : 16]
 */
public class ServerAudioPacket extends Packet {

    private byte[] audio;
    private long sequenceNumber;
    private UUID uuid;

    public byte[] getAudio() {
        return this.audio;
    }

    public long getSequenceNumber() {
        return this.sequenceNumber;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void create(ClientAudioPacket packet, UUID uuid) {
        this.audio = packet.getData();
        this.sequenceNumber = packet.getSequenceNumber();
        this.uuid = uuid;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(
                Type.SERVER_AUDIO.createHeader(this.audio.length + 8 + 16),
                this.audio,
                Bytes.of(this.sequenceNumber),
                Bytes.of(this.uuid.getMostSignificantBits()),
                Bytes.of(this.uuid.getLeastSignificantBits())
        );
    }

    @Override
    public void deserialize(byte[] data) {
        short len = Packet.getPayloadLength(data);
        this.audio = Arrays.copyOfRange(data, 5, 5 + len - 8 - 16);
        this.sequenceNumber = Bytes.getLong(data, 5 + len - 8 - 16);
        this.uuid = new UUID(
                Bytes.getLong(data, 5 + len - 16),
                Bytes.getLong(data, 5 + len - 8)
        );
    }

}
