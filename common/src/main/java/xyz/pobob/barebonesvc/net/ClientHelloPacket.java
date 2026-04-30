package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

import java.util.UUID;

/**
 * [USERNAME : len-16][UUID : 16]
 */
public class ClientHelloPacket extends Packet {

    private String username;
    private UUID uuid;

    public String getUsername() {
        return this.username;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void create(String username, UUID playerId) {
        this.username = (username.length() <= 16) ? username : username.substring(0,16);
        this.uuid = playerId;
    }

    @Override
    public byte[] serialize() {
        byte[] usernameBytes = Bytes.of(this.username);

        return Bytes.join(
                Type.CLIENT_HELLO.createHeader(usernameBytes.length + 16),
                usernameBytes,
                Bytes.of(this.uuid.getMostSignificantBits()),
                Bytes.of(this.uuid.getLeastSignificantBits())
        );
    }

    @Override
    public void deserialize(byte[] data) {
        short len = Packet.getPayloadLength(data);
        this.username = Bytes.getString(data, 5, len - 16);
        this.uuid = new UUID(
                Bytes.getLong(data, 5 + len - 16),
                Bytes.getLong(data, 5 + len - 8)
        );
    }
}