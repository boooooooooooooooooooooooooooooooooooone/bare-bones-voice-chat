package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

/**
 * [ID : 4]
 */
public class ClientKeepAlivePacket implements Packet {

    private int id;

    public int getId() {
        return this.id;
    }

    public void create(int id) {
        this.id = id;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(this.createHeader(4, PacketType.CLIENT_KEEP_ALIVE), Bytes.of(this.id));
    }

    @Override
    public void deserialize(byte[] data) {
        this.id = Bytes.getInt(data, this.getPayloadIndex());
    }
}
