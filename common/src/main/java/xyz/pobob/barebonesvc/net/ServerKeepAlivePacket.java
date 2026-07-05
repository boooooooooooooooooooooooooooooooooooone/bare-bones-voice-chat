package xyz.pobob.barebonesvc.net;

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
        this.id = Bytes.getInt(data, 5);
    }

    @Override
    public byte[] createHeader(int len) {
        return Bytes.join(
                new byte[] {
                        Packet.MAGIC_BYTE,
                        Packet.VERSION,
                        PacketType.SERVER_KEEP_ALIVE.value
                },
                Bytes.of((short) len)
        );
    }

}
