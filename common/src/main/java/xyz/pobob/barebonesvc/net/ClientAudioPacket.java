package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

import java.util.Arrays;

/**
 * [AUDIO DATA : len - 8][SEQUENCE NUMBER : 8]
 */
public class ClientAudioPacket implements Packet {

    private byte[] audio;
    private long sequenceNumber;

    public byte[] getAudio() {
        return this.audio;
    }

    public long getSequenceNumber() {
        return this.sequenceNumber;
    }

    public void create(byte[] data, long sequenceNumber) {
        this.audio = data;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(
                this.createHeader(this.audio.length + 8, PacketType.CLIENT_AUDIO),
                this.audio,
                Bytes.of(this.sequenceNumber)
        );
    }

    @Override
    public void deserialize(byte[] data) {
        int start = this.getPayloadIndex();
        short len = Packet.getPayloadLength(data);

        this.audio = Arrays.copyOfRange(data, start, start + len - 8);
        this.sequenceNumber = Bytes.getLong(data, start + len - 8);
    }
}