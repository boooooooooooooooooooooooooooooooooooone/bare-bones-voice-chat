package xyz.pobob.barebonesvc.packet.retransmission;

import xyz.pobob.barebonesvc.packet.ReliablePacket;

import java.net.SocketAddress;

public final class PendingPacket {

    public final ReliablePacket packet;
    public final SocketAddress address;

    public long lastSent;
    public int retries;

    public PendingPacket(ReliablePacket packet, SocketAddress address, long lastSent) {
        this.packet = packet;
        this.address = address;
        this.lastSent = lastSent;
    }
}
