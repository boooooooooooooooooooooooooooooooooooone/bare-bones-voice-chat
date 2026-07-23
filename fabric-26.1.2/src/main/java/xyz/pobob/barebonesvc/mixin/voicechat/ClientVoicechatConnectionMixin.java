package xyz.pobob.barebonesvc.mixin.voicechat;

import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pobob.barebonesvc.client.BareBonesVCClient;

@Mixin(ClientVoicechatConnection.class)
public class ClientVoicechatConnectionMixin {
    @Shadow private boolean running;

    @Inject(
            method = "<init>",
            at = @At(
                    value = "FIELD",
                    target = "Lde/maxhenkel/voicechat/voice/client/ClientVoicechatConnection;authThread:Lde/maxhenkel/voicechat/voice/client/ClientVoicechatConnection$AuthThread;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void injectClientVoicechatConnectionInit(CallbackInfo ci) {
        this.running = !BareBonesVCClient.INSTANCE.isConnected();
    }

    @Inject(
            method = "checkTimeout",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectCheckTimeout(CallbackInfo ci) {
        if (BareBonesVCClient.INSTANCE.isRunning() && BareBonesVCClient.INSTANCE.lastKeepAlive > 0) {
            if (!BareBonesVCClient.INSTANCE.isConnected()) {
                BareBonesVCClient.INSTANCE.onTimeout();
            }
            ci.cancel();
        }
    }
}
