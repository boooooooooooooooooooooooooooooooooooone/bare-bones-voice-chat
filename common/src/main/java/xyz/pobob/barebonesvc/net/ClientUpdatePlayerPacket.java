package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

/**
 * [DISABLED + DISCONNECTED : 1]
 */
public class ClientUpdatePlayerPacket implements Packet {

    private boolean disabled;
    private boolean disconnected;

    public boolean isDisabled() {
        return this.disabled;
    }

    public boolean isDisconnected() {
        return this.disconnected;
    }

    public void create(boolean disabled, boolean disconnected) {
        this.disabled = disabled;
        this.disconnected = disconnected;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(
                this.createHeader(1),
                new byte[] {(byte) ((this.disabled ? 1 : 0) + (this.disconnected ? 2 : 0))}
        );
    }

    @Override
    public void deserialize(byte[] data) {
        this.disabled = (data[5] & 1) == 1;
        this.disconnected = (data[5] & 2) == 2;
    }

    @Override
    public byte[] createHeader(int len) {
        return Bytes.join(
                new byte[] {
                        Packet.MAGIC_BYTE,
                        Packet.VERSION,
                        PacketType.CLIENT_UPDATE_PLAYER.value
                },
                Bytes.of((short) len)
        );
    }
}
