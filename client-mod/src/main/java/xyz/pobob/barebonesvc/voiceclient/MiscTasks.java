package xyz.pobob.barebonesvc.voiceclient;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.AudioChannel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.packet.ClientHelloPacket;

import java.util.concurrent.TimeUnit;

public class MiscTasks {
    public static void startKeepAliveTask() {
        BareBonesVCClient.INSTANCE.scheduler.scheduleAtFixedRate(() -> {
            if (System.currentTimeMillis() - BareBonesVCClient.INSTANCE.lastKeepAlive > BareBonesVCClient.TIMEOUT_MILLIS) {
                BareBonesVCClient.INSTANCE.onTimeout();
            }

            if (BareBonesVCClient.INSTANCE.client != null) {
                BareBonesVCClient.INSTANCE.getAudioChannels().values().stream().filter(AudioChannel::canKill).forEach(AudioChannel::closeAndKill);
                BareBonesVCClient.INSTANCE.getAudioChannels().entrySet().removeIf(entry -> entry.getValue().isClosed());
            }
        }, 0L, 2000L, TimeUnit.MILLISECONDS);
    }



    private static final int MAX_SENDS = 20;

    public static void startHandshake() {
        final ClientHelloPacket clientHelloPacket = new ClientHelloPacket();
        clientHelloPacket.create(
                MinecraftClient.getInstance().getGameProfile().name(),
                MinecraftClient.getInstance().getGameProfile().id(),
                VoicechatClient.CLIENT_CONFIG.disabled.get()
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
                BareBonesVCClient.INSTANCE.onDisconnect();
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(Text.of("Failed to connect to Bare Bones VC server"), true);
                }
            }
        }, "BareBonesVCHandshakeThread");
        thread.setDaemon(true);
        thread.start();
    }
}
