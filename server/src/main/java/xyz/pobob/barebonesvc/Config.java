package xyz.pobob.barebonesvc;

import xyz.pobob.barebonesvc.voice.Codec;

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public class Config {
    public InetAddress listenAddress;
    public int port;
    public boolean mojangAuth;
    public int mtuSize;
    public int keepAliveInterval;
    public double voiceDistance;
    public Codec codec;
    public boolean groupsEnabled;

    public static Config load(String path) {
        Config config = new Config();

        if (path == null) {
            Questionnaire.load(config);
        } else {
            try {
                String content = Files.readString(Path.of(path));
            } catch (Exception e) {
                BareBonesVCServer.LOGGER.log(Level.WARNING, "Failed to load config");
                Questionnaire.load(config);
            }
        }

        return config;
    }
}
