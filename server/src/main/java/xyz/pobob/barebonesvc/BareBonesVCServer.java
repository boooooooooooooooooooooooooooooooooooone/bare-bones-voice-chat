package xyz.pobob.barebonesvc;

import xyz.pobob.barebonesvc.cli.logger.PrefixFormatter;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class BareBonesVCServer {

    public static final Logger LOGGER = Logger.getAnonymousLogger();
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new PrefixFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
    }

    public static void main(String[] args) {
        String configPath = null;

        for (String arg : args) {
            if (arg.startsWith("--config=")) {
                configPath = arg.substring(9);
            }
        }

        VoiceServer voiceServer = new VoiceServer(Config.load(configPath));
        voiceServer.start();
    }
}
