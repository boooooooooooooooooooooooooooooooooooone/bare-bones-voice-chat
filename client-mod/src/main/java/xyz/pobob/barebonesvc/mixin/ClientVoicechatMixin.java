package xyz.pobob.barebonesvc.mixin;

import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import de.maxhenkel.voicechat.voice.client.InitializationData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientVoicechat.class)
public class ClientVoicechatMixin {
    @Shadow private ClientVoicechatConnection connection;

    @Inject(
            method = "connect",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectConnect(InitializationData data, CallbackInfo ci) {
        if (data == null) ci.cancel();
        try {
            this.connection = new ClientVoicechatConnection(null, null);
        } catch (Exception ignored) {}

    }
}
