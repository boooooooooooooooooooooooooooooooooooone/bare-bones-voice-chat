package xyz.pobob.barebonesvc.mixin.voicechat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

@Mixin(targets = "de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection$AuthThread")
public class AuthThreadMixin {
    @Inject(
            method = "run",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectAuthThreadRun(CallbackInfo ci) {
        if (BareBonesVCClient.INSTANCE.isConnected()) {
            ci.cancel();
        }
    }
}