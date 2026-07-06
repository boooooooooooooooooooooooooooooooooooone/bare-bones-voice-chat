package xyz.pobob.barebonesvc.voiceclient.thread;

import de.maxhenkel.voicechat.voice.client.AudioChannel;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public class MiscThreads {
    private static Thread keepAliveCheckThread;

    public static void startCheckingConnectionHealth() {
        if (keepAliveCheckThread != null) {
            keepAliveCheckThread.interrupt();
        }
        keepAliveCheckThread = new Thread(() -> {
            while (BareBonesVCClient.INSTANCE.isRunning()) {
                if (System.currentTimeMillis() - BareBonesVCClient.INSTANCE.lastKeepAlive > BareBonesVCClient.TIMEOUT_MILLIS) {
                    BareBonesVCClient.INSTANCE.onTimeout();
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
