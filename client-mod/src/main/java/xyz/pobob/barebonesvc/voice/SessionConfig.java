package xyz.pobob.barebonesvc.voice;

import de.maxhenkel.voicechat.config.ServerConfig;

public record SessionConfig(boolean mojangAuth, double voiceDistance, ServerConfig.Codec codec, boolean groupsEnabled) {}