package xyz.pobob.barebonesvc.mixin;

import de.maxhenkel.voicechat.voice.client.TalkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

@Mixin(TalkCache.class)
public class TalkCacheMixin {
    @Inject(
            method = "isTalking(Ljava/util/UUID;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/voice/client/ClientVoicechat;getMicThread()Lde/maxhenkel/voicechat/voice/client/MicThread;",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void injectIsTalking(CallbackInfoReturnable<Boolean> cir) {
        if (BareBonesVCSession.instance().micThread != null && BareBonesVCSession.instance().micThread.isTalking()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "isWhispering(Ljava/util/UUID;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/voice/client/ClientVoicechat;getMicThread()Lde/maxhenkel/voicechat/voice/client/MicThread;"
            ),
            cancellable = true
    )
    private void injectIsWhispering(CallbackInfoReturnable<Boolean> cir) {
        if (BareBonesVCSession.instance().micThread != null && BareBonesVCSession.instance().micThread.isWhispering()) {
            cir.setReturnValue(true);
        }
    }
}
