package xyz.pobob.barebonesvc.mixin;

import de.maxhenkel.voicechat.voice.client.RenderEvents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
}