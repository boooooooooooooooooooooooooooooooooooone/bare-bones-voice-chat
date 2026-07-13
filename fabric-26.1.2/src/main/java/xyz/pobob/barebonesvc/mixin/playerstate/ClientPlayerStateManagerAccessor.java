package xyz.pobob.barebonesvc.mixin.playerstate;

import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPlayerStateManager.class)
public interface ClientPlayerStateManagerAccessor {
    @Invoker("lambda$new$0")
    void invokeUpdatePlayerState(LocalPlayer player, PlayerStatePacket packet);
}