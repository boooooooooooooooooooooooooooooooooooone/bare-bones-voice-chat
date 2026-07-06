package xyz.pobob.barebonesvc.voiceserver;

import xyz.pobob.barebonesvc.voiceserver.retransmission.PendingPacket;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientConnection {

    private final String username;
    private final UUID uuid;
    private boolean disabled;
    private long lastKeepAliveResponse;
    private long latencyNano = -1;

    public ClientConnection(String username, UUID uuid, boolean disabled) {
        this.username = username;
        this.uuid = uuid;
        this.disabled = disabled;
        this.lastKeepAliveResponse = System.currentTimeMillis();
    }

    public String getUsername() {
        return this.username;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public void setDisabled(boolean val) {
        this.disabled = val;
    }

    public synchronized long getLastKeepAliveSynced() {
        return lastKeepAliveResponse;
    }

    public void setLastKeepAliveResponse(long lastKeepAliveResponse) {
        this.lastKeepAliveResponse = lastKeepAliveResponse;
    }

    public long getLatencyNano() {
        return this.latencyNano;
    }

    public void setLatencyNano(long latencyNano) {
        this.latencyNano = latencyNano;
    }



    private final AtomicInteger nextSendSequence = new AtomicInteger();
    private final Map<Integer, PendingPacket> sentPendingPackets = new ConcurrentHashMap<>();

    private final AtomicInteger expectedReceiveSequence = new AtomicInteger();
    private final TreeMap<Integer, byte[]> receivedQueue = new TreeMap<>();

    public int getAndIncrementNextSendSequence() {
        return nextSendSequence.getAndIncrement();
    }

    public void setSentPendingPacket(int sequence, PendingPacket pending) {
        this.sentPendingPackets.put(sequence, pending);
    }

    public void removeSentPendingPacket(int sequence) {
        this.sentPendingPackets.remove(sequence);
    }

    public Collection<PendingPacket> getSentPendingPackets() {
        return this.sentPendingPackets.values();
    }

    public int getExpectedReceiveSequence() {
        return this.expectedReceiveSequence.get();
    }

    public int getAndIncrementExpectedReceiveSequence() {
        return this.expectedReceiveSequence.getAndIncrement();
    }

    public void addToReceivedQueue(int sequence, byte[] data) {
        this.receivedQueue.put(sequence, data);
    }

    public byte[] pollReceivedQueue(int sequence) {
        return this.receivedQueue.remove(sequence);
    }
}
