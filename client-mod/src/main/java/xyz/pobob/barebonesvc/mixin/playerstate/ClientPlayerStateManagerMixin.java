package xyz.pobob.barebonesvc.mixin.playerstate;

import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.pobob.barebonesvc.voice.BareBonesVCClient;

@Mixin(ClientPlayerStateManager.class)
public class ClientPlayerStateManagerMixin {
    @Inject(
            method = "isDisconnected",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectIsDisconnected(CallbackInfoReturnable<Boolean> cir) {
        if (BareBonesVCClient.INSTANCE.isConnected()) cir.setReturnValue(false);
    }

    @Inject(
            method = "isPlayerDisconnected",
            at = @At( // the injection point will only be found if states.get(uuid) == null
                    value = "FIELD",
                    target = "Lde/maxhenkel/voicechat/VoicechatClient;CLIENT_CONFIG:Lde/maxhenkel/voicechat/config/ClientConfig;",
                    opcode = Opcodes.GETSTATIC
            ),
            cancellable = true
    )
    private void injectIsPlayerDisconnected(CallbackInfoReturnable<Boolean> cir) {
        if (BareBonesVCClient.INSTANCE.isConnected()) cir.setReturnValue(true);
    }

    @Inject(
            method = "onDisconnect",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectOnDisconnect(CallbackInfo ci) {
        if (BareBonesVCClient.INSTANCE.isRunning()) ci.cancel();
    }

    @Inject(
            method = "setDisabled",
            at = @At("HEAD")
    )
    private void injectSetDisabled(boolean disabled, CallbackInfo ci) {
        if (BareBonesVCClient.INSTANCE.isConnected()) BareBonesVCClient.INSTANCE.declareOwnState(disabled);
    }
}
