package xyz.pobob.barebonesvc.net;

public class ServerKickPlayerPacket extends Packet {
    @Override
    public byte[] serialize() {
        return Type.SERVER_KICK_PLAYER.createHeader(0);
    }

    @Override
    public void deserialize(byte[] data) {

    }
}
