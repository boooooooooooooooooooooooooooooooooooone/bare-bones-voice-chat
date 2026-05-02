package xyz.pobob.barebonesvc.mixin.clientplayerstatemanager;

import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import org.objectweb.asm.Opcodes;
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



    @Inject(
            method = "isPlayerDisconnected",
            at = @At(
                    value = "FIELD",
                    target = "Lde/maxhenkel/voicechat/VoicechatClient;CLIENT_CONFIG:Lde/maxhenkel/voicechat/config/ClientConfig;",
                    opcode = Opcodes.GETSTATIC
            ),
            cancellable = true
    )
    private void injectIsPlayerDisconnected(CallbackInfoReturnable<Boolean> cir) {
        if (BareBonesVCSession.instance().isConnected()) cir.setReturnValue(true);
    }
}
