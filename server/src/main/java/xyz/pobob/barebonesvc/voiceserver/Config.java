package xyz.pobob.barebonesvc.voiceserver;

import xyz.pobob.barebonesvc.voice.Codec;

import java.net.InetAddress;

public class Config {

    public InetAddress listenAddress;
    public int port;
    public boolean mojangAuth;
    public double voiceDistance;
    public double whisperDistance;
    public Codec codec;

    public static Config load(String path) {
        Config config = null;

        if (path != null) {
            config = ConfigParser.loadFromPath(path);
        }

        if (config == null) {
            config = ConfigParser.createQuestionnaire();
        }

        return config;
    }
}