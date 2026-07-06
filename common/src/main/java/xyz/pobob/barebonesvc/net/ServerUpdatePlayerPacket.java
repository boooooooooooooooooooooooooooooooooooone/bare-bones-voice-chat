package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

import java.util.UUID;

/**
 * [USERNAME : len-17][UUID : 16][DISABLED + DISCONNECTED : 1]
 */
public class ServerUpdatePlayerPacket extends ReliablePacket {

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
                this.createHeader(usernameBytes.length + 16 + 1, PacketType.SERVER_UPDATE_PLAYER),
                usernameBytes,
                Bytes.of(this.uuid.getMostSignificantBits()),
                Bytes.of(this.uuid.getLeastSignificantBits()),
                new byte[] {(byte) ((this.disabled ? 1 : 0) + (this.disconnected ? 2 : 0))}
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
        this.disconnected = (data[start + len - 1] & 2) == 2;
    }
}
