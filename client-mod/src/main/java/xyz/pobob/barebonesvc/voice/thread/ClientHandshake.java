package xyz.pobob.barebonesvc.voice.thread;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.BareBonesVCClient;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

public class ClientHandshake {

    private static final int MAX_SENDS = 20;

    public void start(final byte[] rawPacket) {

        BareBonesVCClient.LOGGER.info("Started connecting to voice server {}", BareBonesVCSession.instance().getReadableAddress());

        Thread sendHellos = new Thread(() -> {

            if (!BareBonesVCSession.instance().send(rawPacket)) {
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(Text.of("A network error occurred. Check logs!"), true);
                }
                BareBonesVCSession.instance().stopNow();
                return;
            }

            int count = 0;
            while (BareBonesVCSession.instance().isRunning() && BareBonesVCSession.instance().config != null && count < MAX_SENDS) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}

                BareBonesVCSession.instance().send(rawPacket);
                count++;
            }
            if (count >= MAX_SENDS) {
                BareBonesVCSession.instance().disconnect();
            }

        });

        sendHellos.setName("BareBonesVCHandshakeSendThread");
        sendHellos.setDaemon(true);
        sendHellos.start();

    }
}
