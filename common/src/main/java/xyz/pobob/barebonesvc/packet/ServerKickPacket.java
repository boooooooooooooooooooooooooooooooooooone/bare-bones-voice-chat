package xyz.pobob.barebonesvc.packet;

public class ServerKickPacket implements Packet {
    @Override
    public byte[] serialize() {
        return this.createHeader(0);
    }

    @Override
    public void deserialize(byte[] data) {
    }
}
