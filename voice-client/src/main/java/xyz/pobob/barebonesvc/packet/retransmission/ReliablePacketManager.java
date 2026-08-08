package xyz.pobob.barebonesvc.packet.retransmission;

import xyz.pobob.barebonesvc.client.BareBonesVCClient;
import xyz.pobob.barebonesvc.packet.ClientAckPacket;
import xyz.pobob.barebonesvc.packet.registry.PacketRegistry;
import xyz.pobob.barebonesvc.util.Bytes;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ReliablePacketManager {

    private static final long RETRANSMIT_TIMEOUT = 1000;
    private static final int MAX_RETRIES = 15;

    private final AtomicInteger nextSendSequence = new AtomicInteger(0);
    private final Map<Integer, PendingPacket> sentPendingPackets = new ConcurrentHashMap<>();

    private final AtomicInteger expectedReceiveSequence = new AtomicInteger(0);
    private final TreeMap<Integer, byte[]> receivedQueue = new TreeMap<>();

    private ScheduledFuture<?> checkPendingPackets;

    public void registerSequence(ReliablePacket packet) {
        int sequence = this.nextSendSequence.getAndIncrement();

        packet.setSequenceNumber(sequence);
        this.sentPendingPackets.put(sequence, new PendingPacket(packet.serialize(), System.currentTimeMillis()));
    }

    public void onServerAcknowledge(int sequence) {
        this.sentPendingPackets.remove(sequence);
    }

    public void startCheckingPendingPackets() {
        try {
            this.checkPendingPackets = BareBonesVCClient.instance.scheduler.scheduleAtFixedRate(() -> {
                long now = System.currentTimeMillis();

                for (PendingPacket pending : this.sentPendingPackets.values()) {
                    if (now - pending.lastSent < RETRANSMIT_TIMEOUT) {
                        continue;
                    }

                    if (pending.retries >= MAX_RETRIES) {
                        BareBonesVCClient.instance.onTimeout();
                        continue;
                    }

                    BareBonesVCClient.instance.send(pending.data);

                    pending.lastSent = now;
                    pending.retries++;
                }

                if (!BareBonesVCClient.instance.isRunning()) {
                    this.checkPendingPackets.cancel(false);
                }
            }, 0L, 100L, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException ignored) {
        }
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
        BareBonesVCClient.instance.send(this.clientAckPacket);
    }

    public void clear() {
        this.sentPendingPackets.clear();
        this.receivedQueue.clear();
    }
}
