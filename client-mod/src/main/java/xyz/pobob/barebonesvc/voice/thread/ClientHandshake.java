package xyz.pobob.barebonesvc.voice.thread;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

public class ClientHandshake extends Thread {

    private static final int MAX_SENDS = 12;

    private final byte[] rawPacket;

    public ClientHandshake(byte[] rawPacket) {
        this.rawPacket = rawPacket;

        this.setName("BareBonesVCHandshakeSendThread");
        this.setDaemon(true);
    }

    @Override
    public void run() {
        if (!BareBonesVCSession.instance().send(this.rawPacket)) {
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.of("A network error occurred. Check logs!"), true);
            }
            BareBonesVCSession.instance().stopNow();
            return;
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        int count = 0;
        while (BareBonesVCSession.instance().isRunning() && BareBonesVCSession.instance().config == null && count < MAX_SENDS) {
            BareBonesVCSession.instance().send(this.rawPacket);
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {}

            count++;
        }
        if (count >= MAX_SENDS) {
            BareBonesVCSession.instance().disconnect();
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.of("Failed to connect to Bare Bones VC server"), true);
            }
        }
    }
}
