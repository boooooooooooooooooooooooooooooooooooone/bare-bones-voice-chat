package xyz.pobob.barebonesvc.net;

public class ServerClosePacket implements Packet {
    @Override
    public byte[] serialize() {
        return this.createHeader(0, PacketType.SERVER_CLOSE);
    }

    @Override
    public void deserialize(byte[] data) {
    }
}
