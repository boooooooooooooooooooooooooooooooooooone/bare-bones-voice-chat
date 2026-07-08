package xyz.pobob.barebonesvc.packet.retransmission;

public final class PendingPacket {

    public final byte[] data;

    public long lastSent;
    public int retries;

    public PendingPacket(byte[] data, long lastSent) {
        this.data = data;
        this.lastSent = lastSent;
    }
}
