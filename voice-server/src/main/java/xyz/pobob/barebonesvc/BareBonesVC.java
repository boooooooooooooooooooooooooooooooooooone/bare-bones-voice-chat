package xyz.pobob.barebonesvc;

import xyz.pobob.barebonesvc.cli.PrefixFormatter;
import xyz.pobob.barebonesvc.packet.PacketType;
import xyz.pobob.barebonesvc.packet.handler.*;
import xyz.pobob.barebonesvc.packet.registry.PacketRegistry;
import xyz.pobob.barebonesvc.voiceserver.BanEntry;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;
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

            handleBanList(server);

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

    private static void handleBanList(BareBonesVCServer server) {
        try {
            Path serverDir = Util.getServerDirectory();
            if (serverDir == null) return;

            Path banlist = serverDir.resolve("banlist.txt");

            if (!banlist.toFile().exists()) {
                Scanner scanner = new Scanner(System.in);

                while (true) {
                    System.out.println("This folder doesn't have a banlist.txt, should a new one be created here? (Y/N):");

                    String line;
                    try {
                        line = scanner.nextLine();
                    } catch (Exception e) {
                        System.out.println("Input interrupted. Exiting setup.");
                        return;
                    }

                    line = line.trim().toLowerCase(Locale.ROOT);

                    if (!line.isEmpty()) {
                        if (line.charAt(0) == 'y') {
                            Files.createFile(banlist);
                            System.out.println("Created banlist.txt");
                            break;
                        } else if (line.charAt(0) == 'n') {
                            System.out.println("Skipping banlist.txt. Note that any players banned during this session will be pardoned when server restarts");
                            break;
                        }
                    }
                }
            } else {
                List<String> lines = Files.readAllLines(banlist);
                for (String line : lines) {
                    try {
                        server.banlist.add(new BanEntry(
                                UUID.fromString(line.substring(0, 36)),
                                line.substring(37)
                        ));
                    } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
                        LOGGER.warning("Unable to resolve line \"" + line + "\" in banlist.txt");
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error occurred while creating banlist.txt", e);
        }
    }
}
