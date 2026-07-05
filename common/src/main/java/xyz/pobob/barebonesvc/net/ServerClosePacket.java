package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

public class ServerClosePacket implements Packet {
    @Override
    public byte[] serialize() {
        return this.createHeader(0);
    }

    @Override
    public void deserialize(byte[] data) {

    }

    @Override
    public byte[] createHeader(int len) {
        return Bytes.join(
                new byte[] {
                        Packet.MAGIC_BYTE,
                        Packet.VERSION,
                        PacketType.SERVER_CLOSE.value
                },
                Bytes.of((short) len)
        );
    }
}
