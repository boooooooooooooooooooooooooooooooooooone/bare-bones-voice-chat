package xyz.pobob.barebonesvc.packet;

public class ServerAuthenticatedPacket extends ReliablePacket {
    @Override
    public byte[] serialize() {
        return this.createHeader(0);
    }

    @Override
    public void deserialize(byte[] data) {

    }
}
