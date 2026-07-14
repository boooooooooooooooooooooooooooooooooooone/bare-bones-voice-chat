package xyz.pobob.barebonesvc.mixin.voicechat;

import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import de.maxhenkel.voicechat.voice.client.MicThread;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;
import xyz.pobob.barebonesvc.voiceclient.FabricBareBonesVCClient;

@Mixin(ClientVoicechat.class)
public class ClientVoicechatMixin {
    @Shadow private MicThread micThread;

    @Inject(
            method = "startMicThread",
            at = @At(
                    value = "FIELD",
                    target = "Lde/maxhenkel/voicechat/voice/client/ClientVoicechat;micThread:Lde/maxhenkel/voicechat/voice/client/MicThread;",
                    opcode = Opcodes.PUTFIELD
            ),
            cancellable = true
    )
    private void injectStartMicThread(ClientVoicechatConnection connection, CallbackInfo ci) {
        if (connection != null) return;

        ClientVoicechat client = ((FabricBareBonesVCClient) BareBonesVCClient.INSTANCE).client;
        if (client != null) {
            this.micThread = new MicThread(
                    client,
                    null,
                    e -> BareBonesVCClient.INSTANCE.logError("Failed to start microphone thread", e)
            );
            this.micThread.start();
        }

        ci.cancel();
    }
}