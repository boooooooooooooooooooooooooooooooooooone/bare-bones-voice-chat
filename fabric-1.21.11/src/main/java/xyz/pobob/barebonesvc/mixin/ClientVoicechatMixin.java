package xyz.pobob.barebonesvc.mixin;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.AudioChannel;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import de.maxhenkel.voicechat.voice.client.MicThread;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pobob.barebonesvc.BareBonesVC;
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

        this.micThread = new MicThread(((FabricBareBonesVCClient) BareBonesVCClient.INSTANCE).client, null,
                e -> BareBonesVC.LOGGER.error("Failed to start microphone thread", e));
        this.micThread.start();
        ci.cancel();
    }

    @Inject(
            method = "processSoundPacket",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectProcessSoundPacket(SoundPacket<?> packet, CallbackInfo ci) {
        if (BareBonesVCClient.INSTANCE.isSimpleVoiceChatRunning() && !VoicechatClient.CLIENT_CONFIG.disabled.get()) {
            AudioChannel channel = ((FabricBareBonesVCClient) BareBonesVCClient.INSTANCE).getAudioChannels().get(packet.getChannelId());
            if (channel == null) {
                channel = new AudioChannel(
                        ((FabricBareBonesVCClient) BareBonesVCClient.INSTANCE).client,
                        null,
                        packet.getChannelId()
                );
                channel.start();
                ((FabricBareBonesVCClient) BareBonesVCClient.INSTANCE).getAudioChannels().put(packet.getChannelId(), channel);
            }
            channel.addToQueue(packet);

            ci.cancel();
        }
    }
}
