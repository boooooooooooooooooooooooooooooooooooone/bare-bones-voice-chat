package xyz.pobob.barebonesvc.cli.command;

import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;

import java.util.Scanner;

public class ConsoleListener implements Runnable {

    private final BareBonesVCServer server;
    private final CommandDispatcher dispatcher;

    public ConsoleListener(BareBonesVCServer server, CommandDispatcher dispatcher) {
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
                BareBonesVC.LOGGER.info("Input interrupted. Stopping server.");
                this.server.close();
                return;
            }

            if (!line.isEmpty()) {
                this.dispatcher.dispatch(line);
            }
        }
    }
}