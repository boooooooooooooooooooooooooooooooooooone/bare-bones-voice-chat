package xyz.pobob.barebonesvc.voiceclient;

import xyz.pobob.barebonesvc.packet.ClientHelloPacket;

import java.util.concurrent.TimeUnit;

public class MiscTasks {
    public static void startTimeoutAndAudioChannelCheck() {
        BareBonesVCClient.INSTANCE.scheduler.scheduleAtFixedRate(() -> {
            if (System.currentTimeMillis() - BareBonesVCClient.INSTANCE.lastKeepAlive > BareBonesVCClient.TIMEOUT_MILLIS) {
                BareBonesVCClient.INSTANCE.onTimeout();
            }

            if (BareBonesVCClient.INSTANCE.isSimpleVoiceChatRunning()) {
                BareBonesVCClient.INSTANCE.pruneAudioChannels();
            }
        }, 50L, 2000L, TimeUnit.MILLISECONDS);
    }



    private static final int MAX_SENDS = 20;

    public static void startHandshake() {
        final ClientHelloPacket clientHelloPacket = new ClientHelloPacket();
        clientHelloPacket.create(
                BareBonesVCClient.INSTANCE.getOwnUsername(),
                BareBonesVCClient.INSTANCE.getOwnUUID(),
                BareBonesVCClient.INSTANCE.isSimpleVoiceChatDisabled()
        );

        Thread thread = new Thread(null, () -> {
            int count = 0;
            while (BareBonesVCClient.INSTANCE.isRunning() && BareBonesVCClient.INSTANCE.config == null && count < MAX_SENDS) {
                BareBonesVCClient.INSTANCE.send(clientHelloPacket);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {}

                count++;
            }
            if (count >= MAX_SENDS) {
                BareBonesVCClient.INSTANCE.sendMessage("Failed to connect to Bare Bones VC server", true);
                BareBonesVCClient.INSTANCE.onDisconnect();
            }
        }, "BareBonesVCHandshakeThread");
        thread.setDaemon(true);
        thread.start();
    }
}
