package xyz.pobob.barebonesvc.packet;

import xyz.pobob.barebonesvc.util.Bytes;
import xyz.pobob.barebonesvc.util.Util;

/**
 * [ID : 4]
 */
public class ServerKeepAlivePacket implements Packet {

    private int id;

    public int getId() {
        return this.id;
    }

    public void create() {
        this.id = Util.RANDOM.nextInt();
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(this.createHeader(4), Bytes.of(this.id));
    }

    @Override
    public void deserialize(byte[] data) {
        this.id = Bytes.getInt(data, this.getPayloadStart());
    }
}
