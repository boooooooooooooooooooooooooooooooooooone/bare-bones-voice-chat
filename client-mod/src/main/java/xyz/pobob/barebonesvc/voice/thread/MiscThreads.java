package xyz.pobob.barebonesvc.voice.thread;

import de.maxhenkel.voicechat.voice.client.AudioChannel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.voice.BareBonesVCClient;

public class MiscThreads {
    private static Thread keepAliveCheckThread;

    public static void startCheckingConnectionHealth() {
        if (keepAliveCheckThread != null) {
            keepAliveCheckThread.interrupt();
        }
        keepAliveCheckThread = new Thread(() -> {
            while (BareBonesVCClient.INSTANCE.isRunning()) {
                if (System.currentTimeMillis() - BareBonesVCClient.INSTANCE.lastKeepAlive > 30000) {
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.sendMessage(Text.of("Bare Bones VC connection timed out"), true);
                    }

                    BareBonesVCClient.INSTANCE.disconnect();
                }

                if (BareBonesVCClient.INSTANCE.client != null) {
                    BareBonesVCClient.INSTANCE.getAudioChannels().values().stream().filter(AudioChannel::canKill).forEach(AudioChannel::closeAndKill);
                    BareBonesVCClient.INSTANCE.getAudioChannels().entrySet().removeIf(entry -> entry.getValue().isClosed());
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
