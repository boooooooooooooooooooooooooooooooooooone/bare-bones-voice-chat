package xyz.pobob.barebonesvc.voice;

import xyz.pobob.barebonesvc.client.BareBonesVCClient;
import xyz.pobob.barebonesvc.packet.ClientHelloPacket;

public class MiscTasks {
    private static final int MAX_SENDS = 20;

    public static void startHandshake() {
        final ClientHelloPacket clientHelloPacket = new ClientHelloPacket();
        clientHelloPacket.create(
                BareBonesVCClient.instance.getOwnUsername(),
                BareBonesVCClient.instance.getOwnUUID(),
                BareBonesVCClient.instance.getOwnDisabled()
        );

        Thread thread = new Thread(null, () -> {
            int count = 0;
            while (BareBonesVCClient.instance.isRunning() && BareBonesVCClient.instance.config == null && count < MAX_SENDS) {
                BareBonesVCClient.instance.send(clientHelloPacket);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {}

                count++;
            }
            if (count >= MAX_SENDS) {
                BareBonesVCClient.instance.onDisconnect("Failed to connect to voice server", true);
            }
        }, "BareBonesVCHandshakeThread");
        thread.setDaemon(true);
        thread.start();
    }
}
