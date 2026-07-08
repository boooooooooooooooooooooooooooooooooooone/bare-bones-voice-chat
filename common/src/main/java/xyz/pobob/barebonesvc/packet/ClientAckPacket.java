package xyz.pobob.barebonesvc.packet;

import xyz.pobob.barebonesvc.util.Bytes;

/**
 * [SEQUENCE : 4]
 */
public class ClientAckPacket implements Packet {

    private int sequence;

    public int getSequence() {
        return this.sequence;
    }

    public void create(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(this.createHeader(4), Bytes.of(this.sequence));
    }

    @Override
    public void deserialize(byte[] data) {
        this.sequence = Bytes.getInt(data, this.getPayloadStart());
    }
}
