package xyz.pobob.barebonesvc.voiceserver;

import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.voice.Codec;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;

public final class ConfigParser {

    public static Config loadFromPath(String path) {
        Config config = new Config();

        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(path)) {
            properties.load(in);


            String listenAddressString = properties.getProperty("listen-address");
            if (listenAddressString != null) {
                try {
                    config.listenAddress = Inet4Address.getByName(listenAddressString);
                } catch (UnknownHostException e) {
                    BareBonesVC.LOGGER.warning("Failed config parsing because listen address is invalid");
                    return null;
                }
            } else {
                BareBonesVC.LOGGER.warning("Failed config parsing because listen-address is not specified");
                return null;
            }


            String portString = properties.getProperty("port");
            if (portString != null) {
                try {
                    int val = Integer.parseInt(portString);
                    if (val < 0 || val > 65535) {
                        BareBonesVC.LOGGER.warning("Failed config parsing because port is not in 0-65535");
                        return null;
                    } else {
                        config.port = val;
                    }
                } catch (NumberFormatException e) {
                    BareBonesVC.LOGGER.warning("Failed config parsing because port is invalid");
                    return null;
                }
            } else {
                BareBonesVC.LOGGER.warning("Failed config parsing because port is not specified");
                return null;
            }


            String mojangAuthString = properties.getProperty("mojang-auth");
            if (mojangAuthString != null) {
                config.mojangAuth = Boolean.parseBoolean(mojangAuthString);
            } else {
                BareBonesVC.LOGGER.warning("Failed config parsing because mojang-auth is not specified");
                return null;
            }


            String voiceDistanceString = properties.getProperty("voice-distance");
            if (voiceDistanceString != null) {
                try {
                    double val = Double.parseDouble(voiceDistanceString);
                    if (val < 1d || val > 1_000_000d) {
                        BareBonesVC.LOGGER.warning("Failed config parsing because voice distance is not in 1-1000000");
                        return null;
                    } else {
                        config.voiceDistance = val;
                    }
                } catch (NumberFormatException e) {
                    BareBonesVC.LOGGER.warning("Failed config parsing because voice distance is invalid");
                    return null;
                }
            } else {
                BareBonesVC.LOGGER.warning("Failed config parsing because voice-distance is not specified");
                return null;
            }


            String codecString = properties.getProperty("codec");
            if (codecString != null) {
                try {
                    config.codec = Codec.valueOf(codecString.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    BareBonesVC.LOGGER.warning("Failed config parsing because codec is invalid");
                    return null;
                }
            } else {
                BareBonesVC.LOGGER.warning("Failed config parsing because codec is not specified");
                return null;
            }


        } catch (IOException e) {
            BareBonesVC.LOGGER.log(Level.SEVERE, "An error occurred while loading config", e);
            return null;
        }

        return config;
    }

    public static Config createQuestionnaire() {
        Config config = new Config();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter the listen address (blank = 0.0.0.0) (leave blank if unsure):");

            String line;
            try {
                line = scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Input interrupted. Exiting setup.");
                return null;
            }

            line = line.trim();

            try {
                if (line.isEmpty()) {
                    config.listenAddress = Inet4Address.getByName("0.0.0.0");
                } else {
                    config.listenAddress = Inet4Address.getByName(line);
                }
                System.out.println("Set listen address to " + config.listenAddress.getHostAddress() + "\n");
                break;
            } catch (UnknownHostException e) {
                System.out.println("Invalid IP address");
            }
        }

        while (true) {
            System.out.println("Enter the port:");

            String line;
            try {
                line = scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Input interrupted. Exiting setup.");
                return null;
            }

            line = line.trim();

            if (!line.isEmpty()) {
                try {
                    int val = Integer.parseInt(line);
                    if (val < 0 || val > 65535) {
                        System.out.println("Port must be between 0-65535");
                    } else {
                        config.port = val;
                        System.out.println("Set port to " + config.port + "\n");
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid port");
                }
            }
        }

        while (true) {
            System.out.println("Use Mojang authentication? (T/F) (This will prove that each client is currently logged into a valid Minecraft session. It won't prevent clients connecting from different Minecraft servers):");

            String line;
            try {
                line = scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Input interrupted. Exiting setup.");
                return null;
            }

            line = line.trim().toLowerCase(Locale.ROOT);

            if (!line.isEmpty()) {
                if (line.charAt(0) == 't') {
                    config.mojangAuth = true;
                    System.out.println("Using Mojang auth\n");
                    break;
                } else if (line.charAt(0) == 'f') {
                    config.mojangAuth = false;
                    System.out.println("Not using Mojang auth\n");
                    break;
                }
            }
        }

        while (true) {
            System.out.println("Enter the voice distance (blank = 64) (note that this is only an instruction for clients and sound packets will always be sent to every user connected to the server):");

            String line;
            try {
                line = scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Input interrupted. Exiting setup.");
                return null;
            }

            line = line.trim();

            if (!line.isEmpty()) {
                try {
                    double val = Double.parseDouble(line);
                    if (val < 1d || val > 1_000_000d) {
                        System.out.println("Voice distance must be between 1-1000000");
                    } else {
                        config.voiceDistance = val;
                        System.out.println("Set voice distance to " + config.voiceDistance + "\n");
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid voice distance");
                }
            } else {
                config.voiceDistance = 64;
                System.out.println("Set voice distance to " + config.voiceDistance + "\n");
                break;
            }
        }

        while (true) {
            System.out.println("Select a codec, allowed values are VOIP, AUDIO, RESTRICTED_LOWDELAY. (blank = AUDIO):");

            String line;
            try {
                line = scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Input interrupted. Exiting setup.");
                return null;
            }

            line = line.trim();

            if (!line.isEmpty()) {
                try {
                    config.codec = Codec.valueOf(line.toUpperCase(Locale.ROOT));
                    System.out.println("Set codec to " + config.codec + "\n");
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid codec");
                }
            } else {
                config.codec = Codec.AUDIO;
                System.out.println("Set codec to " + config.codec + "\n");
                break;
            }
        }

        return config;
    }
}