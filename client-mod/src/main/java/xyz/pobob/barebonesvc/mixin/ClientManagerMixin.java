package xyz.pobob.barebonesvc.mixin;

import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

@Mixin(ClientManager.class)
public class ClientManagerMixin {
    @Inject(
            method = "getClient",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void injectGetClient(CallbackInfoReturnable<ClientVoicechat> cir) {
        if (BareBonesVCSession.instance().client != null) cir.setReturnValue(BareBonesVCSession.instance().client);
    }

    @Inject(
            method = "onJoinWorld",
            at = @At(
                    value = "FIELD",
                    target = "Lde/maxhenkel/voicechat/voice/client/ClientManager;hasShownPermissionsMessage:Z",
                    opcode = Opcodes.PUTFIELD
            ),
            cancellable = true
    )
    private void injectOnJoinWorld(CallbackInfo ci) {
        if (BareBonesVCSession.instance().isRunning()) ci.cancel();
    }
}
