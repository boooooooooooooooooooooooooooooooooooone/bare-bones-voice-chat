package xyz.pobob.barebonesvc.packet.handler;

import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import xyz.pobob.barebonesvc.gui.SessionEventFeed;
import xyz.pobob.barebonesvc.mixin.playerstate.ClientPlayerStateManagerAccessor;
import xyz.pobob.barebonesvc.packet.ServerUpdatePlayerPacket;

public class ServerUpdatePlayerHandler implements ServerPacketHandler {

    private final ServerUpdatePlayerPacket serverUpdatePlayerPacket = new ServerUpdatePlayerPacket();

    @Override
    public void handle(byte[] data) {
        this.serverUpdatePlayerPacket.deserialize(data);

        if (this.serverUpdatePlayerPacket.shouldLog()) {
            SessionEventFeed.send(this.serverUpdatePlayerPacket.getUsername() +
                    (this.serverUpdatePlayerPacket.isDisconnected() ? " disconnected" : " joined"));
        }

        ((ClientPlayerStateManagerAccessor) ClientManager.getPlayerStateManager()).invokeUpdatePlayerState(
                null,
                new PlayerStatePacket(new PlayerState(
                        this.serverUpdatePlayerPacket.getUUID(),
                        this.serverUpdatePlayerPacket.getUsername(),
                        this.serverUpdatePlayerPacket.isDisabled(),
                        this.serverUpdatePlayerPacket.isDisconnected()
                ))
        );
    }
}
