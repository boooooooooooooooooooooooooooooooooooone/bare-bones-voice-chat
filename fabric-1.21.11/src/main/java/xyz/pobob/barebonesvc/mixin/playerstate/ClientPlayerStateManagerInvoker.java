package xyz.pobob.barebonesvc.mixin.playerstate;

import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPlayerStateManager.class)
public interface ClientPlayerStateManagerInvoker {
    @Invoker("lambda$new$0")
    void invokeUpdatePlayerState(ClientPlayerEntity player, PlayerStatePacket packet);
}