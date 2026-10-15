package xyz.pobob.barebonesvc.mixin.voicechat;

import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import de.maxhenkel.voicechat.voice.client.InitializationData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pobob.barebonesvc.client.BareBonesVCClient;

@Mixin(ClientVoicechat.class)
public class ClientVoicechatMixin {
    @Shadow private ClientVoicechatConnection connection;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void injectClientVoicechatConstructor(CallbackInfo ci) {
        if (BareBonesVCClient.instance.isConnected()) {
            try {
                this.connection = new ClientVoicechatConnection(null, new InitializationData(
                        BareBonesVCClient.instance.host,
                        null
                ));
            } catch (Exception e) {
                BareBonesVCClient.instance.logError("An error occurred while starting voice chat!", e);
                BareBonesVCClient.instance.shutdownVoiceChat();
            }
        }
    }
}