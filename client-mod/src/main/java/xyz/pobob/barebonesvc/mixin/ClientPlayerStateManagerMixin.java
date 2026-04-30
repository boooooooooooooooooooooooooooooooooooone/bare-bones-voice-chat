package xyz.pobob.barebonesvc.mixin;

import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

@Mixin(ClientPlayerStateManager.class)
public class ClientPlayerStateManagerMixin {
    @Inject(
            method = "isDisconnected",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectIsDisconnected(CallbackInfoReturnable<Boolean> cir) {
        if (BareBonesVCSession.instance().isConnected()) cir.setReturnValue(false);
    }
}
