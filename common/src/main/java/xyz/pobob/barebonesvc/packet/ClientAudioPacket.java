package xyz.pobob.barebonesvc.packet;

import xyz.pobob.barebonesvc.util.Bytes;

import java.util.Arrays;

/**
 * [AUDIO DATA : len - 9][SEQUENCE NUMBER : 8][WHISPERING : 1]
 */
public class ClientAudioPacket implements Packet {

    private byte[] audio;
    private long sequenceNumber;
    private boolean whispering;

    public byte[] getAudio() {
        return this.audio;
    }

    public long getSequenceNumber() {
        return this.sequenceNumber;
    }

    public boolean isWhispering() {
        return this.whispering;
    }

    public void create(byte[] data, long sequenceNumber, boolean whispering) {
        this.audio = data;
        this.sequenceNumber = sequenceNumber;
        this.whispering = whispering;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(
                this.createHeader(this.audio.length + 8 + 1),
                this.audio,
                Bytes.of(this.sequenceNumber),
                new byte[] {(byte) (this.whispering ? 1 : 0)}
        );
    }

    @Override
    public void deserialize(byte[] data) {
        int start = this.getPayloadStart();
        short len = Packet.getPayloadLength(data);

        this.audio = Arrays.copyOfRange(data, start, start + len - 8 - 1);
        this.sequenceNumber = Bytes.getLong(data, start + len - 8 - 1);
        this.whispering = (data[start + len - 1] & 1) == 1;
    }
}