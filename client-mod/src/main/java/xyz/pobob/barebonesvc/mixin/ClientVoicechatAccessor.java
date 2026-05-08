package xyz.pobob.barebonesvc.mixin;

import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientVoicechat.class)
public interface ClientVoicechatAccessor {
    @Invoker("startMicThread") public void callStartMicThread(ClientVoicechatConnection connection);
}
