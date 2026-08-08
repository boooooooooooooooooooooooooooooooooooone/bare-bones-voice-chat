package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.Util;
import xyz.pobob.barebonesvc.voiceserver.BanEntry;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class UnbanCommand implements Command {
    @Override
    public void execute(String[] args, BareBonesVCServer server) {
        if (args.length == 0) {
            BareBonesVC.LOGGER.warning("No client specified");
            return;
        }

        List<BanEntry> toSave = new ArrayList<>();
        for (BanEntry banEntry : getTargets(args[0], server)) {
            server.banlist.remove(banEntry);
            toSave.add(banEntry);

            BareBonesVC.LOGGER.info("Unbanned client: " + banEntry.username() + " (" + banEntry.uuid() + ")");
        }
        this.saveUnbans(toSave);
    }

    private void saveUnbans(List<BanEntry> unbannedEntries) {
        Path serverDir = Util.getServerDirectory();
        if (serverDir == null) return;

        Path banlist = serverDir.resolve("banlist.txt");

        if (banlist.toFile().exists()) {
            try {
                List<String> lines = Files.readAllLines(banlist);
                for (final BanEntry banEntry : unbannedEntries) {
                    lines.removeIf(line -> {
                       try {
                           return banEntry.uuid().toString().equalsIgnoreCase(line.substring(0, 36))
                                   || banEntry.username().equalsIgnoreCase(line.substring(37));
                       } catch (IndexOutOfBoundsException ignored) {
                           return false;
                       }
                    });
                }
                Files.write(banlist, lines);
            } catch (IOException e) {
                BareBonesVC.LOGGER.log(Level.SEVERE, "An error occurred while writing to banlist.txt", e);
            }
        }
    }

    private static List<BanEntry> getTargets(String target, BareBonesVCServer server) {
        List<BanEntry> unbanned = new ArrayList<>();

        for (BanEntry entry : server.banlist) {
            if (entry.username().equalsIgnoreCase(target) || entry.uuid().toString().equalsIgnoreCase(target)) {
                unbanned.add(entry);
            }
        }

        if (unbanned.isEmpty()) {
            BareBonesVC.LOGGER.info("Couldn't find any ban entries matching target");
        }

        return unbanned;
    }
}
