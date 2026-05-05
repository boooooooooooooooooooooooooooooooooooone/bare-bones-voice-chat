package xyz.pobob.barebonesvc.voiceserver;

import xyz.pobob.barebonesvc.cli.Questionnaire;
import xyz.pobob.barebonesvc.voice.Codec;

import java.net.InetAddress;

public class Config {
    public InetAddress listenAddress;
    public int port;
    public boolean mojangAuth;
    public double voiceDistance;
    public Codec codec;
    public boolean groupsEnabled;

    public static Config load(String path) {
        Config config = new Config();

        if (path == null) {
            Questionnaire.load(config);
        } else {
            // TODO add config file parsing
            Questionnaire.load(config);
        }

        return config;
    }
}
