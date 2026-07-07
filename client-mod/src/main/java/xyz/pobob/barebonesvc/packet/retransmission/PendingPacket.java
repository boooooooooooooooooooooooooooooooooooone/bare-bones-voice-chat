package xyz.pobob.barebonesvc.packet.retransmission;

import xyz.pobob.barebonesvc.packet.ReliablePacket;

public final class PendingPacket {

    public final ReliablePacket packet;

    public long lastSent;
    public int retries;

    public PendingPacket(ReliablePacket packet, long lastSent) {
        this.packet = packet;
        this.lastSent = lastSent;
    }
}
