package xyz.pobob.barebonesvc.net;

public class ClientDisconnectPacket extends Packet {
    @Override
    public byte[] serialize() {
        return Type.CLIENT_DISCONNECT.createHeader(0);
    }

    @Override
    public void deserialize(byte[] data) {
    }
}
