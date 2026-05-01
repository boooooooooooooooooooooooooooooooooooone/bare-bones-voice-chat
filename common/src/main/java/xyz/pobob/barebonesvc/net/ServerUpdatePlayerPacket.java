package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

import java.util.UUID;

/**
 * [USERNAME : len-17][UUID : 16][DISABLED + DISCONNECTED : 1]
 */
public class ServerUpdatePlayerPacket extends Packet {

    private String username;
    private UUID uuid;
    private boolean disabled;
    private boolean disconnected;

    public String getUsername() {return this.username;}
    public UUID getUUID() {return this.uuid;}
    public boolean getDisabled() {return this.disabled;}
    public boolean getDisconnected() {return this.disconnected;}

    public void create(String username, UUID uuid, boolean disabled, boolean disconnected) {
        this.username = username;
        this.uuid = uuid;
        this.disabled = disabled;
        this.disconnected = disconnected;
    }

    @Override
    public byte[] serialize() {
        byte[] usernameBytes = Bytes.of(this.username);

        return Bytes.join(
                Type.SERVER_UPDATE_PLAYER.createHeader(usernameBytes.length + 16 + 1),
                usernameBytes,
                Bytes.of(this.uuid.getMostSignificantBits()),
                Bytes.of(this.uuid.getLeastSignificantBits()),
                new byte[] {(byte) ((this.disabled ? 1 : 0) + (this.disconnected ? 2 : 0))}
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
        this.disconnected = (data[5 + len - 1] & 2) == 2;
    }
}
