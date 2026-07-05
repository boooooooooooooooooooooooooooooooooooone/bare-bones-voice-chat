package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.BareBonesVC;

public class ServerPlayerLatencyHandler implements ServerPacketHandler {

    private final ServerPlayerLatencyPacket serverPlayerLatencyPacket = new ServerPlayerLatencyPacket();

    @Override
    public void handle(byte[] data) {
        this.serverPlayerLatencyPacket.deserialize(data);
        BareBonesVC.LATENCIES.put(
                this.serverPlayerLatencyPacket.getUUID(),
                this.serverPlayerLatencyPacket.getLatencyNano() * 1e-6
        );
    }
}
