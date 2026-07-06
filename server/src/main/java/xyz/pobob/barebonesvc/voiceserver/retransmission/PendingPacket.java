package xyz.pobob.barebonesvc.voiceserver.retransmission;

import xyz.pobob.barebonesvc.net.ReliablePacket;

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
