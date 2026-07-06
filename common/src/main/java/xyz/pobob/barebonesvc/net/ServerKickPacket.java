package xyz.pobob.barebonesvc.net;

public class ServerKickPacket implements Packet {
    @Override
    public byte[] serialize() {
        return this.createHeader(0, PacketType.SERVER_KICK);
    }

    @Override
    public void deserialize(byte[] data) {
    }
}
