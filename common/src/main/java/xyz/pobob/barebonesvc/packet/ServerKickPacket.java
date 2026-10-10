package xyz.pobob.barebonesvc.packet;

import xyz.pobob.barebonesvc.util.Bytes;

/**
 * [IS BAN : 1]
 */
public class ServerKickPacket implements Packet {

    private boolean banned;

    public boolean isBanned() {
        return this.banned;
    }

    public void create(boolean banned) {
        this.banned = banned;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(
                this.createHeader(1),
                new byte[]{(byte) (this.banned ? 1 : 0)}
        );
    }

    @Override
    public void deserialize(byte[] data) {
        this.banned = data[this.getPayloadStart()] == 1;
    }
}
