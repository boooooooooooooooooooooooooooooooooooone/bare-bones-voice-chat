package xyz.pobob.barebonesvc.voice;


public final class SessionConfig {
    private final boolean mojangAuth;
    private float voiceDistance;
    private float whisperDistance;
    private final Codec codec;

    public SessionConfig(boolean mojangAuth, float voiceDistance, float whisperDistance, Codec codec) {
        this.mojangAuth = mojangAuth;
        this.voiceDistance = voiceDistance;
        this.whisperDistance = whisperDistance;
        this.codec = codec;
    }

    public boolean isMojangAuth() {
        return this.mojangAuth;
    }

    public float getVoiceDistance() {
        return this.voiceDistance;
    }

    public float getWhisperDistance() {
        return this.whisperDistance;
    }

    public Codec getCodec() {
        return this.codec;
    }

    public void setVoiceDistance(float val) {
        this.voiceDistance = val;
    }

    public void setWhisperDistance(float val) {
        this.whisperDistance = val;
    }
}