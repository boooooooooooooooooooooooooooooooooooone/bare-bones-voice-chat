package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.packet.ServerUpdatePlayerPacket;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public class ServerUpdatePlayerHandler implements ServerPacketHandler {

    private final ServerUpdatePlayerPacket serverUpdatePlayerPacket = new ServerUpdatePlayerPacket();

    @Override
    public void handle(byte[] data) {
        this.serverUpdatePlayerPacket.deserialize(data);

        if (this.serverUpdatePlayerPacket.shouldLog()) {
            BareBonesVCClient.INSTANCE.sendFeed(this.serverUpdatePlayerPacket.getUsername() +
                    (this.serverUpdatePlayerPacket.isDisconnected() ? " disconnected" : " joined"));
        }

        BareBonesVCClient.INSTANCE.updatePlayerState(
                this.serverUpdatePlayerPacket.getUUID(),
                this.serverUpdatePlayerPacket.getUsername(),
                this.serverUpdatePlayerPacket.isDisabled(),
                this.serverUpdatePlayerPacket.isDisconnected()
        );
    }
}
