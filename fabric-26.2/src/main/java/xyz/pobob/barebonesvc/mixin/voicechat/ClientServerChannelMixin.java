package xyz.pobob.barebonesvc.mixin.voicechat;

import de.maxhenkel.voicechat.net.ClientServerChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pobob.barebonesvc.client.BareBonesVCClient;

@Mixin(ClientServerChannel.class)
public class ClientServerChannelMixin {
    @Inject(
            method = "onClientPacket",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectOnClientPacket(CallbackInfo ci) {
        if (BareBonesVCClient.INSTANCE.isRunning()) {
            ci.cancel();
        }
    }
}
