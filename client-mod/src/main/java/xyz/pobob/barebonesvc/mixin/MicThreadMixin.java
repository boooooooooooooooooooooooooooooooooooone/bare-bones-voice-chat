package xyz.pobob.barebonesvc.mixin;

import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.natives.OpusManager;
import de.maxhenkel.voicechat.voice.client.MicThread;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pobob.barebonesvc.packet.ClientAudioPacket;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

import java.util.concurrent.atomic.AtomicLong;

@Mixin(MicThread.class)
public class MicThreadMixin {
    @Shadow @Final private OpusEncoder encoder;
    @Shadow @Final private AtomicLong sequenceNumber;

    @Unique private final ClientAudioPacket clientAudioPacket = new ClientAudioPacket();

    @Inject(
            method = "sendAudioPacket",
            at = @At("HEAD")
    )
    private void injectSendAudioPacket(short[] audio, boolean whispering, CallbackInfo ci) {
        if (BareBonesVCClient.INSTANCE.isConnected()) {
            this.clientAudioPacket.create(this.encoder.encode(audio), this.sequenceNumber.getAndIncrement(), whispering);
            BareBonesVCClient.INSTANCE.send(this.clientAudioPacket);
        }
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/natives/OpusManager;createEncoder(Lde/maxhenkel/voicechat/api/opus/OpusEncoderMode;)Lde/maxhenkel/voicechat/api/opus/OpusEncoder;"
            )
    )
    private OpusEncoder redirectEncoder(OpusEncoderMode encoder) {
        return OpusManager.createEncoder(BareBonesVCClient.INSTANCE.config == null ? ServerConfig.Codec.AUDIO.getMode() : BareBonesVCClient.INSTANCE.config.getCodec().getMode());
    }
}
