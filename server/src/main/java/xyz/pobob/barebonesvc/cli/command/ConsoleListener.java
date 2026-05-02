package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

import java.util.Scanner;

public class ConsoleListener implements Runnable {

    private final VoiceServer server;
    private final CommandDispatcher dispatcher;

    public ConsoleListener(VoiceServer server, CommandDispatcher dispatcher) {
        this.server = server;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (this.server.isRunning()) {
            String line;
            try {
                line = scanner.nextLine().trim();
            } catch (Exception e) {
                BareBonesVCServer.LOGGER.info("Input interrupted. Stopping server.");
                this.server.stopNow();
                return;
            }

            if (!line.isEmpty()) {
                this.dispatcher.dispatch(line);
            }
        }
    }
}