package xyz.pobob.barebonesvc.net;

public class ServerDisconnectPacket extends Packet {
    @Override
    public byte[] serialize() {
        return Type.SERVER_DISCONNECT.createHeader(0);
    }

    @Override
    public void deserialize(byte[] data) {

    }
}
