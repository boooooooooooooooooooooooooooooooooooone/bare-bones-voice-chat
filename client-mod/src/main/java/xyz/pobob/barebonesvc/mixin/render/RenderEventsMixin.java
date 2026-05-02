package xyz.pobob.barebonesvc.mixin.render;

import de.maxhenkel.voicechat.voice.client.RenderEvents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

@Mixin(RenderEvents.class)
public class RenderEventsMixin {
    @Shadow private void renderIcon(DrawContext guiGraphics, Identifier texture) {}
    @Final @Shadow private static Identifier MICROPHONE_ICON;
    @Final @Shadow private static Identifier WHISPER_MICROPHONE_ICON;

    @Inject(
            method = "onRenderHUD",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lde/maxhenkel/voicechat/voice/client/ClientVoicechat;getMicThread()Lde/maxhenkel/voicechat/voice/client/MicThread;",
                    ordinal = 0
            )
    )
    private void injectIsTalking(DrawContext guiGraphics, float tickDelta, CallbackInfo ci) {
        if (BareBonesVCSession.instance().micThread != null) {
            if (BareBonesVCSession.instance().micThread.isWhispering()) {
                renderIcon(guiGraphics, WHISPER_MICROPHONE_ICON);
            } else if (BareBonesVCSession.instance().micThread.isTalking()) {
                renderIcon(guiGraphics, MICROPHONE_ICON);
            }
        }
    }

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