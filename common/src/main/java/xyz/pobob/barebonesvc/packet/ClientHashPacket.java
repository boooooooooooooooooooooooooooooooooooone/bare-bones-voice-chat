package xyz.pobob.barebonesvc.packet;

import xyz.pobob.barebonesvc.util.Bytes;

public class ClientHashPacket implements Packet {

    private String hash;

    public String getHash() {
        return this.hash;
    }

    public void create(String hash) {
        this.hash = hash;
    }

    @Override
    public byte[] serialize() {
        byte[] hashBytes = Bytes.of(this.hash);

        return Bytes.join(
                this.createHeader(hashBytes.length),
                hashBytes
        );
    }

    @Override
    public void deserialize(byte[] data) {
        this.hash = Bytes.getString(data, this.getPayloadStart(), Packet.getPayloadLength(data));
    }
}
