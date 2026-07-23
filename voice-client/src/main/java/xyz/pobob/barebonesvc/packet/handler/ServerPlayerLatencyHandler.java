package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.client.BareBonesVCClient;
import xyz.pobob.barebonesvc.packet.ServerPlayerLatencyPacket;

public class ServerPlayerLatencyHandler implements ServerPacketHandler {

    private final ServerPlayerLatencyPacket serverPlayerLatencyPacket = new ServerPlayerLatencyPacket();

    @Override
    public void handle(byte[] data) {
        this.serverPlayerLatencyPacket.deserialize(data);
        BareBonesVCClient.INSTANCE.latencies.put(
                this.serverPlayerLatencyPacket.getUUID(),
                this.serverPlayerLatencyPacket.getLatencyNano() * 1e-6
        );
    }
}
