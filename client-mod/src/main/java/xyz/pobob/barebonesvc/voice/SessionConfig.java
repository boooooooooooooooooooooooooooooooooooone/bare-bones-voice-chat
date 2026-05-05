package xyz.pobob.barebonesvc.voice;

import de.maxhenkel.voicechat.config.ServerConfig;

public final class SessionConfig {
    private final boolean mojangAuth;
    private final ServerConfig.Codec codec;
    private final boolean groupsEnabled;

    private float voiceDistance;

    public SessionConfig(boolean mojangAuth, float voiceDistance, ServerConfig.Codec codec, boolean groupsEnabled) {
        this.mojangAuth = mojangAuth;
        this.voiceDistance = voiceDistance;
        this.codec = codec;
        this.groupsEnabled = groupsEnabled;
    }

    public boolean mojangAuth() {
        return mojangAuth;
    }

    public float voiceDistance() {
        return voiceDistance;
    }

    public ServerConfig.Codec codec() {
        return codec;
    }

    public boolean groupsEnabled() {
        return groupsEnabled;
    }

    public void setVoiceDistance(float val) {
        this.voiceDistance = val;
    }
}