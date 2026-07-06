package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

public class ServerAckPacket implements Packet {

    private int sequence;

    public int getSequence() {
        return this.sequence;
    }

    public void create(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(this.createHeader(4, PacketType.SERVER_ACK), Bytes.of(this.sequence));
    }

    @Override
    public void deserialize(byte[] data) {
        this.sequence = Bytes.getInt(data, this.getPayloadIndex());
    }
}
