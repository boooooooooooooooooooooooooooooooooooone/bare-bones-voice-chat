package xyz.pobob.barebonesvc;

import xyz.pobob.barebonesvc.cli.PrefixFormatter;
import xyz.pobob.barebonesvc.packet.PacketType;
import xyz.pobob.barebonesvc.packet.handler.*;
import xyz.pobob.barebonesvc.packet.registry.PacketRegistry;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.Config;

import java.security.NoSuchAlgorithmException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BareBonesVC {

    public static final Logger LOGGER = Logger.getAnonymousLogger();

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new PrefixFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
    }

    public static void registerPackets(BareBonesVCServer server) {
        PacketRegistry.registerHandler(
                PacketType.CLIENT_HELLO,
                new ClientHelloHandler(server)
        );
        PacketRegistry.registerHandler(
                PacketType.CLIENT_ACK,
                new ClientAckHandler(server)
        );
        PacketRegistry.registerHandler(
                PacketType.CLIENT_KEEP_ALIVE,
                new ClientKeepAliveHandler(server)
        );
        PacketRegistry.registerHandler(
                PacketType.CLIENT_AUDIO,
                new ClientAudioHandler(server)
        );
        PacketRegistry.registerHandler(
                PacketType.CLIENT_UPDATE_PLAYER,
                new ClientUpdatePlayerHandler(server)
        );
        PacketRegistry.registerHandler(
                PacketType.CLIENT_HASH,
                new ClientHashHandler(server)
        );
    }

    public static void main(String[] args) {
        String configPath = null;

        for (String arg : args) {
            if (arg.startsWith("--config=")) {
                configPath = arg.substring(9);
            }
        }

        Config config = Config.load(configPath);
        if (config != null) {
            BareBonesVCServer server = new BareBonesVCServer(config);
            if (config.mojangAuth) {
                try {
                    server.generateKeyPair();
                } catch (NoSuchAlgorithmException e) {
                    BareBonesVC.LOGGER.log(Level.SEVERE,
                            "An error occurred while generating keypair! Disable mojang auth if this cannot be resolved", e);
                    return;
                }
            }

            registerPackets(server);
            server.start();
        }
    }
}
