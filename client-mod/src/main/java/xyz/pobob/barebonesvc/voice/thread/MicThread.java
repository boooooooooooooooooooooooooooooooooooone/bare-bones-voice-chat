package xyz.pobob.barebonesvc.voice.thread;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.config.ServerConfig.Codec;
import de.maxhenkel.voicechat.debug.VoicechatUncaughtExceptionHandler;
import de.maxhenkel.voicechat.natives.OpusManager;
import de.maxhenkel.voicechat.plugins.ClientPluginManager;
import de.maxhenkel.voicechat.voice.client.*;
import de.maxhenkel.voicechat.voice.client.microphone.Microphone;
import de.maxhenkel.voicechat.voice.client.microphone.MicrophoneManager;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.MinecraftClient;
import org.jspecify.annotations.Nullable;
import xyz.pobob.barebonesvc.net.ClientAudioPacket;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class MicThread extends Thread {
    @Nullable
    private final ClientVoicechat client;
    @Nullable
    private Microphone mic;
    @Nullable
    private MicrophoneException microphoneError;
    private boolean running;
    private boolean microphoneLocked;
    private final OpusEncoder encoder;
    private MicrophoneProcessor microphoneProcessor;
    private final Consumer<MicrophoneException> onError;
    private boolean hasSentAudio;
    private final AtomicLong sequenceNumber = new AtomicLong();
    private volatile boolean stopPacketSent = true;

    public MicThread(@Nullable ClientVoicechat client, Consumer<MicrophoneException> onError) {
        this.client = client;
        this.onError = onError;
        this.running = true;
        this.encoder = OpusManager.createEncoder(BareBonesVCSession.instance().config == null ? Codec.VOIP.getMode() : BareBonesVCSession.instance().config.codec().getMode());
        this.microphoneProcessor = this.createMicrophoneProcessor();
        this.setDaemon(true);
        this.setName("BareBonesVCMicThread");
        this.setUncaughtExceptionHandler(new VoicechatUncaughtExceptionHandler());
    }

    private MicrophoneProcessor createMicrophoneProcessor() {
        MicrophoneActivationType type = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get();
        return (MicrophoneActivationType.VOICE.equals(type) ? new VoiceMicrophoneProcessor() : new PTTMicrophoneProcessor());
    }

    public void getError(Consumer<MicrophoneException> onError) {
        if (this.microphoneError != null) {
            onError.accept(this.microphoneError);
        }
    }

    public void run() {
        Microphone mic = this.getMic();
        if (mic != null) {
            while(this.running) {
                MicrophoneActivationType type = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get();
                if (!type.equals(this.microphoneProcessor.getActivationType())) {
                    this.microphoneProcessor.close();
                    this.microphoneProcessor = this.createMicrophoneProcessor();
                }

                if (!this.microphoneLocked && !ClientManager.getPlayerStateManager().isDisabled()) {
                    short[] processed = this.pollProcessedAudio(false);
                    if (processed != null) {
                        if (!this.microphoneProcessor.shouldTransmitAudio()) {
                            processed = null;
                        }

                        this.sendAudio(processed, this.microphoneProcessor.isWhispering());
                    }
                } else {
                    this.flushIfNeeded();
                    if (!this.microphoneLocked && ClientManager.getPlayerStateManager().isDisabled()) {
                        this.microphoneProcessor.reset();
                        if (mic.isStarted()) {
                            mic.stop();
                        }
                    }

                    Utils.sleep(10);
                }
            }

        }
    }

    public short[] pollMic() {
        Microphone mic = this.getMic();
        if (mic == null) {
            throw new IllegalStateException("No microphone available");
        } else {
            if (!mic.isStarted()) {
                mic.start();
            }

            if (mic.available() < 960) {
                Utils.sleep(5);
                return null;
            } else {
                return mic.read();
            }
        }
    }

    public short[] pollProcessedAudio(boolean testing) {
        short[] audio = this.pollMic();
        if (audio == null) {
            return null;
        } else {
            this.microphoneProcessor.process(audio, testing);
            return audio;
        }
    }

    @Nullable
    private Microphone getMic() {
        if (!this.running) {
            return null;
        } else {
            if (this.mic == null) {
                try {
                    this.mic = MicrophoneManager.createMicrophone();
                    MinecraftClient var10000 = MinecraftClient.getInstance();
                    ClientManager var10001 = ClientManager.instance();
                    Objects.requireNonNull(var10001);
                    var10000.execute(var10001::checkMicrophonePermissions);
                } catch (MicrophoneException e) {
                    this.onError.accept(e);
                    this.microphoneError = e;
                    this.running = false;
                    return null;
                }
            }

            return this.mic;
        }
    }

    private void flush() {
        this.sendStopPacket();
        if (!this.encoder.isClosed()) {
            this.encoder.resetState();
        }

        if (this.client != null) {
            AudioRecorder recorder = this.client.getRecorder();
            if (recorder != null) {
                recorder.flushChunkThreaded(MinecraftClient.getInstance().getSession().getUuidOrNull());
            }
        }
    }

    private void sendAudio(short[] rawAudio, boolean whispering) {
        short[] mergedAudio = ClientPluginManager.instance().onMergeClientSound(rawAudio);
        if (mergedAudio == null) {
            this.flushIfNeeded();
        } else {
            short[] finalAudio = ClientPluginManager.instance().onClientSound(mergedAudio, whispering);
            if (finalAudio == null) {
                this.flushIfNeeded();
            } else {
                this.sendAudioPacket(finalAudio, whispering);
                this.hasSentAudio = true;
            }
        }
    }

    private void flushIfNeeded() {
        if (this.hasSentAudio) {
            this.flush();
            this.hasSentAudio = false;
        }
    }

    public boolean isTalking() {
        return !this.microphoneLocked && this.microphoneProcessor.shouldTransmitAudio();
    }

    public boolean isWhispering() {
        return this.microphoneProcessor.isWhispering();
    }

    public boolean shouldTransmitAudio() {
        return this.microphoneProcessor.shouldTransmitAudio();
    }

    public void setMicrophoneLocked(boolean microphoneLocked) {
        this.microphoneLocked = microphoneLocked;
        this.microphoneProcessor.reset();
    }

    public void close() {
        if (this.running) {
            this.running = false;
            if (Thread.currentThread() != this) {
                try {
                    this.join(100L);
                } catch (InterruptedException e) {
                    Voicechat.LOGGER.error("Interrupted while waiting for mic thread to close", e);
                }
            }

            if (this.mic != null) {
                this.mic.close();
            }

            this.encoder.close();
            this.microphoneProcessor.close();
            this.flush();
        }
    }

    public boolean isClosed() {
        return !this.running;
    }

    private void sendAudioPacket(short[] audio, boolean whispering) {
        if (BareBonesVCSession.instance().isConnected()) {
            byte[] encoded = this.encoder.encode(audio);
            ClientAudioPacket packet = new ClientAudioPacket();
            packet.create(encoded, this.sequenceNumber.getAndIncrement());
            BareBonesVCSession.instance().send(packet.serialize());
            this.stopPacketSent = false;
        }

        // try {
        //     if (this.client != null && this.client.getRecorder() != null) {
        //         this.client.getRecorder().appendChunk(MinecraftClient.getInstance().getSession().getUuidOrNull(), System.currentTimeMillis(), PositionalAudioUtils.convertToStereo(audio));
        //     }
        // } catch (IOException e) {
        //     Voicechat.LOGGER.error("Failed to record audio", new Object[]{e});
        //     this.client.setRecording(false);
        // }

        // just use OBS bruz

    }

    private void sendStopPacket() {
        // if (!this.stopPacketSent) {
        //     if (this.connection != null && this.connection.isInitialized()) {
        //         this.connection.sendToServer(new NetworkMessage(new MicPacket(new byte[0], false, this.sequenceNumber.getAndIncrement())));
        //         this.stopPacketSent = true;
        //     }
        // }
    }
}
