package xyz.pobob.barebonesvc.packet;

import xyz.pobob.barebonesvc.util.Bytes;

import java.util.UUID;

/**
 * [USERNAME : len-17][UUID : 16][DISABLED + DISCONNECTED + SHOULD LOG : 1]
 */
public class ServerUpdatePlayerPacket extends ReliablePacket {

    private String username;
    private UUID uuid;
    private boolean disabled;
    private boolean disconnected;
    private boolean shouldLog;

    public String getUsername() {return this.username;}
    public UUID getUUID() {return this.uuid;}
    public boolean isDisabled() {return this.disabled;}
    public boolean isDisconnected() {return this.disconnected;}
    public boolean shouldLog() {return this.shouldLog;}

    public void create(String username, UUID uuid, boolean disabled, boolean disconnected, boolean shouldLog) {
        this.username = username;
        this.uuid = uuid;
        this.disabled = disabled;
        this.disconnected = disconnected;
        this.shouldLog = shouldLog;
    }

    @Override
    public byte[] serialize() {
        byte[] usernameBytes = Bytes.of(this.username);

        return Bytes.join(
                this.createHeader(usernameBytes.length + 16 + 1),
                usernameBytes,
                Bytes.of(this.uuid.getMostSignificantBits()),
                Bytes.of(this.uuid.getLeastSignificantBits()),
                new byte[] {(byte) ((this.disabled ? 1 : 0)
                        + (this.disconnected ? 2 : 0)
                        + (this.shouldLog ? 4 : 0))}
        );
    }

    @Override
    public void deserialize(byte[] data) {
        int start = this.getPayloadStart();
        short len = Packet.getPayloadLength(data);

        this.username = Bytes.getString(data, start, len - 16 - 1);
        this.uuid = new UUID(
                Bytes.getLong(data, start + len - 16 - 1),
                Bytes.getLong(data, start + len - 8 - 1)
        );
        this.disabled = (data[start + len - 1] & 1) == 1;
        this.disconnected = (data[start + len - 1] & 2) == 2;
        this.shouldLog = (data[start + len - 1] & 4) == 4;
    }
}
