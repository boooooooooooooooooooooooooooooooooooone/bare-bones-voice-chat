package xyz.pobob.barebonesvc.mixin.voicechat;

import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.voice.client.MicThread;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pobob.barebonesvc.client.BareBonesVCClient;
import xyz.pobob.barebonesvc.packet.ClientAudioPacket;

import java.util.concurrent.atomic.AtomicLong;

@Mixin(MicThread.class)
public class MicThreadMixin {
    @Shadow @Final private OpusEncoder encoder;
    @Shadow @Final private AtomicLong sequenceNumber;

    @Unique private final ClientAudioPacket clientAudioPacket = new ClientAudioPacket();

    @Inject(
            method = "sendAudioPacket",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectSendAudioPacket(short[] audio, boolean whispering, CallbackInfo ci) {
        if (BareBonesVCClient.INSTANCE.isConnected()) {
            this.clientAudioPacket.create(this.encoder.encode(audio), this.sequenceNumber.getAndIncrement(), whispering);
            BareBonesVCClient.INSTANCE.send(this.clientAudioPacket);
            ci.cancel();
        }
    }
}