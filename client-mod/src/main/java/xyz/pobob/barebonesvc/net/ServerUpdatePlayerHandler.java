package xyz.pobob.barebonesvc.net;

import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.MinecraftClient;
import xyz.pobob.barebonesvc.gui.ClientList;
import xyz.pobob.barebonesvc.mixin.playerstate.ClientPlayerStateManagerInvoker;

public class ServerUpdatePlayerHandler implements ServerPacketHandler {

    private final ServerUpdatePlayerPacket serverUpdatePlayerPacket = new ServerUpdatePlayerPacket();

    @Override
    public void handle(byte[] data) {
        this.serverUpdatePlayerPacket.deserialize(data);
        MinecraftClient.getInstance().execute(() -> {
            ((ClientPlayerStateManagerInvoker) ClientManager.getPlayerStateManager()).invokeUpdatePlayerState(
                    null,
                    new PlayerStatePacket(new PlayerState(
                            this.serverUpdatePlayerPacket.getUUID(),
                            this.serverUpdatePlayerPacket.getUsername(),
                            this.serverUpdatePlayerPacket.getDisabled(),
                            this.serverUpdatePlayerPacket.getDisconnected()
                    ))
            );
            ClientList.update();
        });
    }
}
