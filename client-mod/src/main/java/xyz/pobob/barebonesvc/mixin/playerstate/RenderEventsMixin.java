package xyz.pobob.barebonesvc.mixin.playerstate;

import de.maxhenkel.voicechat.voice.client.RenderEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

@Mixin(RenderEvents.class)
public class RenderEventsMixin {

    @Inject(
            method = "shouldShowIcons",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/voice/client/ClientManager;getClient()Lde/maxhenkel/voicechat/voice/client/ClientVoicechat;",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void injectShouldShowIcons(CallbackInfoReturnable<Boolean> cir) {
        if (BareBonesVCSession.instance().isConnected()) cir.setReturnValue(true);
    }
}