package xyz.pobob.barebonesvc.packet.retransmission;

import xyz.pobob.barebonesvc.packet.ClientAckPacket;
import xyz.pobob.barebonesvc.packet.ReliablePacket;
import xyz.pobob.barebonesvc.packet.registry.PacketRegistry;
import xyz.pobob.barebonesvc.util.Bytes;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ReliablePacketManager {

    private static final long RETRANSMIT_TIMEOUT = 1000;
    private static final int MAX_RETRIES = 10;

    private final AtomicInteger nextSendSequence = new AtomicInteger();
    private final Map<Integer, PendingPacket> sentPendingPackets = new ConcurrentHashMap<>();

    private final AtomicInteger expectedReceiveSequence = new AtomicInteger();
    private final TreeMap<Integer, byte[]> receivedQueue = new TreeMap<>();

    public ScheduledFuture<?> checkPendingPackets;

    public void registerSequence(ReliablePacket packet) {
        packet.setSequenceNumber(this.nextSendSequence.getAndIncrement());
        this.sentPendingPackets.put(packet.getSequenceNumber(), new PendingPacket(packet, System.currentTimeMillis()));
    }

    public void onServerAcknowledge(final int sequence) {
        this.sentPendingPackets.remove(sequence);
    }

    public void start() {
        this.checkPendingPackets = BareBonesVCClient.INSTANCE.scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();

            for (PendingPacket pending : this.sentPendingPackets.values()) {
                if (now - pending.lastSent < RETRANSMIT_TIMEOUT) {
                    continue;
                }

                if (pending.retries >= MAX_RETRIES) {
                    BareBonesVCClient.INSTANCE.onTimeout();
                    continue;
                }

                BareBonesVCClient.INSTANCE.send(pending.packet);

                pending.lastSent = now;
                pending.retries++;
            }
        }, 0L, 100L, TimeUnit.MILLISECONDS);
    }

    public void receive(byte[] data) {
        int sequence = Bytes.getInt(data, ReliablePacket.SEQUENCE_INDEX);
        this.sendAck(sequence);

        if (sequence < this.expectedReceiveSequence.get()) return;

        if (sequence > this.expectedReceiveSequence.get()) {
            this.receivedQueue.put(sequence, data);
            return;
        }

        this.processSequential(data);
    }

    private void processSequential(byte[] data) {
        byte[] current = data;

        while (current != null) {

            PacketRegistry.dispatchServerPacket(data);

            current = this.receivedQueue.remove(this.expectedReceiveSequence.getAndIncrement());
        }
    }

    private final ClientAckPacket clientAckPacket = new ClientAckPacket();

    private void sendAck(int sequence) {
        this.clientAckPacket.create(sequence);
        BareBonesVCClient.INSTANCE.send(this.clientAckPacket);
    }

    public void clear() {
        this.sentPendingPackets.clear();
        this.receivedQueue.clear();
    }
}
