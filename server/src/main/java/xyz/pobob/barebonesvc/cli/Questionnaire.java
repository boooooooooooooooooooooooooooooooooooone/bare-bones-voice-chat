package xyz.pobob.barebonesvc.cli;

import xyz.pobob.barebonesvc.voice.Codec;
import xyz.pobob.barebonesvc.voiceserver.Config;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Scanner;

public final class Questionnaire {
    public static void load(Config config) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter the listen address (blank = 0.0.0.0) (leave blank if unsure):");

            String line;
            try {
                line = scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Input interrupted. Exiting setup.");
                return;
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
                return;
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
                return;
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
                return;
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
                return;
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

        while (true) {
            System.out.println("Allow groups? (T/F):");

            String line;
            try {
                line = scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Input interrupted. Exiting setup.");
                return;
            }

            line = line.trim().toLowerCase(Locale.ROOT);

            if (!line.isEmpty()) {
                if (line.charAt(0) == 't') {
                    config.groupsEnabled = true;
                    System.out.println("Allowed groups\n");
                    break;
                } else if (line.charAt(0) == 'f') {
                    config.groupsEnabled = false;
                    System.out.println("Disallowed groups\n");
                    break;
                }
            }
        }
    }
}