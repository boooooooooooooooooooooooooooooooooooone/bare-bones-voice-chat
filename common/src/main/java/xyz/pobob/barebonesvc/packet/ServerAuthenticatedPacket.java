package xyz.pobob.barebonesvc.packet;

import xyz.pobob.barebonesvc.packet.retransmission.ReliablePacket;

public class ServerAuthenticatedPacket extends ReliablePacket {
    @Override
    public byte[] serialize() {
        return this.createHeader(0);
    }

    @Override
    public void deserialize(byte[] data) {

    }
}
