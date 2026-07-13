package xyz.pobob.barebonesvc.mixin.voicechat;

import de.maxhenkel.voicechat.voice.client.ClientManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientManager.class)
public interface ClientManagerAccessor {
    @Invoker("onJoinWorld")
    void invokeOnJoinWorld();
}
