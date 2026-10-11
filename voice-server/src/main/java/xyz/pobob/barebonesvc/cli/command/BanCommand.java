package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.Util;
import xyz.pobob.barebonesvc.packet.ServerKickPacket;
import xyz.pobob.barebonesvc.voiceserver.BanEntry;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import static xyz.pobob.barebonesvc.util.Util.httpRequestAsync;

public class BanCommand implements Command {
    private final ServerKickPacket serverKickPacket = new ServerKickPacket();

    public BanCommand() {
        this.serverKickPacket.create(true);
    }

    @Override
    public void execute(String[] args, BareBonesVCServer server) {
        if (args.length == 0) {
            BareBonesVC.LOGGER.warning("No client specified");
            return;
        }

        var targets = KickCommand.getTargets(args[0], server);
        if (!targets.isEmpty()) {
            List<BanEntry> toSave = new ArrayList<>();
            for (Map.Entry<SocketAddress, ClientConnection> target : targets) {
                server.send(this.serverKickPacket, target.getKey());
                server.onDisconnect(target.getKey());

                BanEntry banEntry = new BanEntry(
                        target.getValue().getUUID(),
                        target.getValue().getUsername()
                );
                server.banlist.add(banEntry);
                toSave.add(banEntry);

                BareBonesVC.LOGGER.info("Banned client: " + target.getValue().getUsername() + " (" + target.getValue().getUUID() + ")");
            }
            this.saveBans(toSave);
        } else {
            try {
                this.banOfflineFromUUID(UUID.fromString(args[0]), server);
            } catch (IllegalArgumentException e) {
                this.banOfflineFromUsername(args[0], server);
            }
        }
    }

    private void saveBans(List<BanEntry> newBans) {
        Path serverDir = Util.getServerDirectory();
        if (serverDir == null) return;

        Path banlist = serverDir.resolve("banlist.txt");

        if (banlist.toFile().exists()) {
            try {
                List<String> lines = Files.readAllLines(banlist);
                for (BanEntry banEntry : newBans) {
                    String line = banEntry.uuid() + " " + banEntry.username();
                    if (!lines.contains(line)) {
                        lines.add(line);
                    }
                }
                Files.write(banlist, lines);
            } catch (IOException e) {
                BareBonesVC.LOGGER.log(Level.SEVERE, "An error occurred while writing to banlist.txt", e);
            }
        }
    }

    private void banOfflineFromUUID(UUID uuid, final BareBonesVCServer server) {
        var response = httpRequestAsync(
                "GET",
                "https://api.minecraftservices.com/minecraft/profile/lookup/" + uuid,
                null,
                Map.of("Accept", "application/json"),
                server.ioPool
        );

        response.thenAccept(httpResponse -> {
            if (httpResponse.statusCode() == 200) {
                BanEntry banEntry = fromJson(httpResponse.body());
                if (banEntry != null) {
                    server.banlist.add(banEntry);
                    this.saveBans(List.of(banEntry));

                    BareBonesVC.LOGGER.info("Banned offline client: " + banEntry.username() + " (" + banEntry.uuid() + ")");
                    return;
                }
            }

            BareBonesVC.LOGGER.warning("Unable to ban client");
        });
    }

    private void banOfflineFromUsername(String username, final BareBonesVCServer server) {
        var response = httpRequestAsync(
                "GET",
                "https://api.mojang.com/users/profiles/minecraft/" + username,
                null,
                Map.of("Accept", "application/json"),
                server.ioPool
        );

        response.thenAcceptAsync(httpResponse -> {
            if (httpResponse.statusCode() == 200) {
                BanEntry banEntry = fromJson(httpResponse.body());
                if (banEntry != null) {
                    server.banlist.add(banEntry);
                    this.saveBans(List.of(banEntry));

                    BareBonesVC.LOGGER.info("Banned offline client: " + banEntry.username() + " (" + banEntry.uuid() + ")");
                    return;
                }
            }

            BareBonesVC.LOGGER.warning("Unable to ban client");
        });
    }

    private static BanEntry fromJson(String json) {
        try {
            return new BanEntry(processUUIDFromMojang(json.substring(12, 44)), json.substring(59, json.length() - 3));
        } catch (IndexOutOfBoundsException e) {
            BareBonesVC.LOGGER.severe("An error occurred while parsing response from Mojang API, please report this!");
            return null;
        }
    }

    private static UUID processUUIDFromMojang(String mojangUUID) {
        return UUID.fromString(String.join("-",
                mojangUUID.substring(0,8),
                mojangUUID.substring(8,12),
                mojangUUID.substring(12,16),
                mojangUUID.substring(16,20),
                mojangUUID.substring(20,32)
        ));
    }
}