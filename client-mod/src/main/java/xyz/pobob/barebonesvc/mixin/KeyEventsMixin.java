package xyz.pobob.barebonesvc.mixin;

import de.maxhenkel.voicechat.voice.client.KeyEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

@Mixin(KeyEvents.class)
public class KeyEventsMixin {
    @Inject(
            method = "checkConnected",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectCheckConnected(CallbackInfoReturnable<Boolean> cir) {
        if (BareBonesVCSession.instance().isConnected()) cir.setReturnValue(true);
    }
}
