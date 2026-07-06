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
                this.createHeader(usernameBytes.length + 17, PacketType.CLIENT_HELLO),
                usernameBytes,
                Bytes.of(this.uuid.getMostSignificantBits()),
                Bytes.of(this.uuid.getLeastSignificantBits()),
                new byte[] {(byte) (this.disabled ? 1 : 0)}
        );
    }

    @Override
    public void deserialize(byte[] data) {
        int start = this.getPayloadIndex();
        short len = Packet.getPayloadLength(data);

        this.username = Bytes.getString(data, start, len - 16 - 1);
        this.uuid = new UUID(
                Bytes.getLong(data, start + len - 16 - 1),
                Bytes.getLong(data, start + len - 8 - 1)
        );
        this.disabled = (data[start + len - 1] & 1) == 1;
    }
}