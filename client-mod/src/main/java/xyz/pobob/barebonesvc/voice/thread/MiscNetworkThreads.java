package xyz.pobob.barebonesvc.voice.thread;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

public class MiscNetworkThreads {
    private static Thread keepAliveCheckThread;

    public static void startCheckingConnectionHealth() {
        if (keepAliveCheckThread != null) {
            keepAliveCheckThread.interrupt();
        }
        keepAliveCheckThread = new Thread(() -> {
            while (BareBonesVCSession.instance().isRunning()) {
                if (System.currentTimeMillis() - BareBonesVCSession.instance().lastKeepAlive > 30000) {
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.sendMessage(Text.of("Bare Bones VC connection timed out"), true);
                    }

                    BareBonesVCSession.instance().disconnect();
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        keepAliveCheckThread.setDaemon(true);
        keepAliveCheckThread.setName("BareBonesVCCheckConnectionThread");
        keepAliveCheckThread.start();
    }

}
