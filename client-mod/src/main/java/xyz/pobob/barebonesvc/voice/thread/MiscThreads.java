package xyz.pobob.barebonesvc.voice.thread;

import de.maxhenkel.voicechat.voice.client.AudioChannel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

public class MiscThreads {
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

                if (BareBonesVCSession.instance().client != null) {
                    BareBonesVCSession.instance().getAudioChannels().values().stream().filter(AudioChannel::canKill).forEach(AudioChannel::closeAndKill);
                    BareBonesVCSession.instance().getAudioChannels().entrySet().removeIf(entry -> entry.getValue().isClosed());
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
