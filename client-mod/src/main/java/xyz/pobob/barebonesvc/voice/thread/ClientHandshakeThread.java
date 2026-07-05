package xyz.pobob.barebonesvc.voice.thread;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.voice.BareBonesVCClient;

public class ClientHandshakeThread extends Thread {

    private static final int MAX_SENDS = 20;

    private final byte[] rawPacket;

    public ClientHandshakeThread(byte[] rawPacket) {
        this.rawPacket = rawPacket;

        this.setName("BareBonesVCHandshakeSendThread");
        this.setDaemon(true);
    }

    @Override
    public void run() {
        if (!BareBonesVCClient.INSTANCE.send(this.rawPacket)) {
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.of("A network error occurred. Check logs!"), true);
            }
            BareBonesVCClient.INSTANCE.stopNow();
            return;
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        int count = 0;
        while (BareBonesVCClient.INSTANCE.isRunning() && BareBonesVCClient.INSTANCE.config == null && count < MAX_SENDS) {
            BareBonesVCClient.INSTANCE.send(this.rawPacket);
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {}

            count++;
        }
        if (count >= MAX_SENDS) {
            BareBonesVCClient.INSTANCE.disconnect();
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.of("Failed to connect to Bare Bones VC server"), true);
            }
        }
    }
}
