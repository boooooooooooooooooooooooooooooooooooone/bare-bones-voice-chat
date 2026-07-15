package xyz.pobob.barebonesvc.mixin.voicechat;

import de.maxhenkel.voicechat.voice.client.ClientManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

@Mixin(ClientManager.class)
public class ClientManagerMixin {

    @Inject(
            method = "authenticate",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectAuthenticate(CallbackInfo ci) {
        if (BareBonesVCClient.INSTANCE.isRunning()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "onJoinWorld",
            at = @At(
                    value = "FIELD",
                    target = "Lde/maxhenkel/voicechat/voice/client/ClientManager;client:Lde/maxhenkel/voicechat/voice/client/ClientVoicechat;",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 0
            ),
            cancellable = true
    )
    private void injectOnJoinWorld(CallbackInfo ci) {
        if (BareBonesVCClient.INSTANCE.isRunning()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "onDisconnect",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectOnDisconnect(CallbackInfo ci) {
        if (BareBonesVCClient.INSTANCE.isConnected()) {
            ci.cancel();
        }
    }
}
