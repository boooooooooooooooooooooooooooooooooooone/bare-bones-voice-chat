package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.util.Bytes;

import java.util.UUID;

/**
 * [UUID : 16][LATENCY : 8]
 * this is not a ping packet
 */
public class ServerPlayerLatencyPacket implements Packet {

    private UUID uuid;
    private long latencyNano;

    public UUID getUUID() {
        return this.uuid;
    }

    public long getLatencyNano() {
        return this.latencyNano;
    }

    public void create(UUID uuid, long latencyNano) {
        this.uuid = uuid;
        this.latencyNano = latencyNano;
    }

    @Override
    public byte[] serialize() {
        return Bytes.join(
                this.createHeader(24, PacketType.SERVER_PLAYER_LATENCY),
                Bytes.of(this.uuid.getMostSignificantBits()),
                Bytes.of(this.uuid.getLeastSignificantBits()),
                Bytes.of(this.latencyNano)
        );
    }

    @Override
    public void deserialize(byte[] data) {
        int start = this.getPayloadIndex();

        this.uuid = new UUID(
                Bytes.getLong(data, start),
                Bytes.getLong(data, start + 8)
        );
        this.latencyNano = Bytes.getLong(data, start + 16);
    }
}
