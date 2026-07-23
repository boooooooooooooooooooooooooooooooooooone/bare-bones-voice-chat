package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.client.BareBonesVCClient;
import xyz.pobob.barebonesvc.packet.ServerUpdatePlayerPacket;

public class ServerUpdatePlayerHandler implements ServerPacketHandler {

    private final ServerUpdatePlayerPacket serverUpdatePlayerPacket = new ServerUpdatePlayerPacket();

    @Override
    public void handle(byte[] data) {
        this.serverUpdatePlayerPacket.deserialize(data);

        if (this.serverUpdatePlayerPacket.shouldLog()) {
            BareBonesVCClient.INSTANCE.sendFeed(this.serverUpdatePlayerPacket.getUsername() +
                    (this.serverUpdatePlayerPacket.isDisconnected() ? " disconnected" : " joined"));
        }

        if (this.serverUpdatePlayerPacket.isDisconnected()) {
            BareBonesVCClient.INSTANCE.latencies.remove(this.serverUpdatePlayerPacket.getUUID());
        }

        BareBonesVCClient.INSTANCE.updatePlayerState(
                this.serverUpdatePlayerPacket.getUUID(),
                this.serverUpdatePlayerPacket.getUsername(),
                this.serverUpdatePlayerPacket.isDisabled(),
                this.serverUpdatePlayerPacket.isDisconnected()
        );
    }
}
