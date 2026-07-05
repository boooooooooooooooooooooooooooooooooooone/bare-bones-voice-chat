package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

import java.util.UUID;

/**
 * [USERNAME : len-17][UUID : 16][DISABLED : 1]
 */
public class ClientHelloPacket implements Packet {

    private String username;
    private UUID uuid;
    private boolean disabled;

    public String getUsername() {
        return this.username;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public void create(String username, UUID playerId, boolean disabled) {
        this.username = (username.length() <= 16) ? username : username.substring(0, 16);
        this.uuid = playerId;
        this.disabled = disabled;
    }

    @Override
    public byte[] serialize() {
        byte[] usernameBytes = Bytes.of(this.username);

        return Bytes.join(
                this.createHeader(usernameBytes.length + 17),
                usernameBytes,
                Bytes.of(this.uuid.getMostSignificantBits()),
                Bytes.of(this.uuid.getLeastSignificantBits()),
                new byte[] {(byte) (this.disabled ? 1 : 0)}
        );
    }

    @Override
    public void deserialize(byte[] data) {
        short len = Packet.getPayloadLength(data);
        this.username = Bytes.getString(data, 5, len - 16 - 1);
        this.uuid = new UUID(
                Bytes.getLong(data, 5 + len - 16 - 1),
                Bytes.getLong(data, 5 + len - 8 - 1)
        );
        this.disabled = (data[5 + len - 1] & 1) == 1;
    }

    @Override
    public byte[] createHeader(int len) {
        return Bytes.join(
                new byte[] {
                        Packet.MAGIC_BYTE,
                        Packet.VERSION,
                        PacketType.CLIENT_HELLO.value
                },
                Bytes.of((short) len)
        );
    }
}