package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

import java.util.Arrays;

/**
 * [AUDIO DATA : len - 8][SEQUENCE NUMBER : 8]
 */
public class ClientAudioPacket extends Packet {

    private byte[] data;
    private long sequenceNumber;

    public byte[] getData() {
        return this.data;
    }

    public long getSequenceNumber() {
        return this.sequenceNumber;
    }

    public void create(byte[] data, long sequenceNumber) {
        this.data = data;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(
                Type.CLIENT_AUDIO.createHeader(this.data.length + 8),
                this.data,
                Bytes.of(this.sequenceNumber)
        );
    }

    @Override
    public void deserialize(byte[] data) {
        short len = Packet.getPayloadLength(data);
        this.data = Arrays.copyOfRange(data, 5, 5 + len - 8);
        this.sequenceNumber = Bytes.getLong(data, 5 + len - 8);
    }
}