package xyz.pobob.barebonesvc;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

public final class Util {
    private Util() {}

    public static Path getServerDirectory() {
        try {
            Path location = Paths.get(
                    BareBonesVC.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );

            return Files.isDirectory(location) ? location : location.getParent();
        } catch (URISyntaxException e) {
            BareBonesVC.LOGGER.log(Level.SEVERE, "An error occurred while trying to find server directory", e);
            return null;
        }
    }
}
