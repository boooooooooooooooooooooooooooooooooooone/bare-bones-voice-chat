package xyz.pobob.barebonesvc.voiceserver.thread;

import xyz.pobob.barebonesvc.net.ServerPlayerLatencyPacket;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class LatencyManager {

    private final VoiceServer server;
    private final ServerPlayerLatencyPacket serverPlayerLatencyPacket = new ServerPlayerLatencyPacket();

    private final Map<Integer, Long> sentTimes = new ConcurrentHashMap<>();

    public LatencyManager(VoiceServer server) {
        this.server = server;
    }

    public void registerSentTime(final int id) {
        this.sentTimes.put(id, System.nanoTime());
        this.server.scheduler.schedule(() -> this.sentTimes.remove(id), 60, TimeUnit.SECONDS);
    }

    public void updateClientLatency(ClientConnection client, int id) {
        Long sentTime = this.sentTimes.get(id);
        if (sentTime != null) {
            long latencyNano = System.nanoTime() - sentTime;
            client.setLatencyNano(latencyNano);
            this.serverPlayerLatencyPacket.create(client.getUUID(), latencyNano);
            this.server.announce(this.serverPlayerLatencyPacket.serialize());
        }
    }
}
