package xyz.pobob.barebonesvc.packet.retransmission;

import java.net.SocketAddress;

public final class PendingPacket {

    public final byte[] data;
    public final SocketAddress address;

    public long lastSent;
    public int retries;

    public PendingPacket(byte[] data, SocketAddress address, long lastSent) {
        this.data = data;
        this.address = address;
        this.lastSent = lastSent;
    }
}
