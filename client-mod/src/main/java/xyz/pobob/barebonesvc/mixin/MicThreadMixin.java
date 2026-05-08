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
import xyz.pobob.barebonesvc.net.ClientAudioPacket;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

import java.util.concurrent.atomic.AtomicLong;

@Mixin(MicThread.class)
public class MicThreadMixin {
    @Shadow @Final private OpusEncoder encoder;
    @Shadow @Final private AtomicLong sequenceNumber;
    @Shadow private boolean stopPacketSent;

    @Unique private final ClientAudioPacket clientAudioPacket = new ClientAudioPacket();

    @Inject(
            method = "sendAudioPacket",
            at = @At("HEAD")
    )
    private void injectSendAudioPacket(short[] audio, boolean whispering, CallbackInfo ci) {
        if (BareBonesVCSession.instance().isConnected()) {
            byte[] encoded = this.encoder.encode(audio);
            this.clientAudioPacket.create(encoded, this.sequenceNumber.getAndIncrement());
            BareBonesVCSession.instance().send(this.clientAudioPacket.serialize());
            this.stopPacketSent = false;
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
        return OpusManager.createEncoder(BareBonesVCSession.instance().config == null ? ServerConfig.Codec.AUDIO.getMode() : BareBonesVCSession.instance().config.codec().getMode());
    }
}
