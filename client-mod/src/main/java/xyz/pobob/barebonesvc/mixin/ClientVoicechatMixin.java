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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pobob.barebonesvc.BareBonesVCClient;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

import java.util.UUID;

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

        this.micThread = new MicThread(BareBonesVCSession.instance().client, null,
                e -> BareBonesVCClient.LOGGER.error("Failed to start microphone thread", e));
        this.micThread.start();
        ci.cancel();
    }

    @Inject(
            method = "processSoundPacket",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectProcessSoundPacket(SoundPacket<?> packet, CallbackInfo ci) {
        if (BareBonesVCSession.instance().client != null && !VoicechatClient.CLIENT_CONFIG.disabled.get()) {
            if (BareBonesVCSession.instance().getAudioChannels().containsKey(packet.getChannelId())) {
                BareBonesVCSession.instance().getAudioChannels().get(packet.getChannelId()).addToQueue(packet);
            } else {
                AudioChannel channel = new AudioChannel(BareBonesVCSession.instance().client, null, packet.getChannelId());
                channel.start();
                channel.addToQueue(packet);
                this.addNewAudioChannel(packet.getChannelId(), channel);
            }
            ci.cancel();
        }
    }

    @Unique
    private synchronized void addNewAudioChannel(UUID uuid, AudioChannel channel) {
        BareBonesVCSession.instance().client.getAudioChannels().put(uuid, channel);
    }
}
