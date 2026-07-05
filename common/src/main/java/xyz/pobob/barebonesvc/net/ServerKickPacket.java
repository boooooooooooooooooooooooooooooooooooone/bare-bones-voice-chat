package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

public class ServerKickPacket implements Packet {
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
                        PacketType.SERVER_KICK.value
                },
                Bytes.of((short) len)
        );
    }
}
