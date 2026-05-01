package xyz.pobob.barebonesvc.net;

public class ServerClosePacket extends Packet {
    @Override
    public byte[] serialize() {
        return Type.SERVER_CLOSE.createHeader(0);
    }

    @Override
    public void deserialize(byte[] data) {

    }
}
