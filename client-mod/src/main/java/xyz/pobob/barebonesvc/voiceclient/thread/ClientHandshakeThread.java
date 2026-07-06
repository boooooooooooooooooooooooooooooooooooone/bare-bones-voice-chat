package xyz.pobob.barebonesvc.voiceclient.thread;

import de.maxhenkel.voicechat.VoicechatClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.net.ClientHelloPacket;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public class ClientHandshakeThread extends Thread {

    private static final int MAX_SENDS = 20;

    private final ClientHelloPacket clientHelloPacket = new ClientHelloPacket();

    public ClientHandshakeThread() {
        this.clientHelloPacket.create(
                MinecraftClient.getInstance().getGameProfile().name(),
                MinecraftClient.getInstance().getGameProfile().id(),
                VoicechatClient.CLIENT_CONFIG.disabled.get()
        );

        this.setName("BareBonesVCHandshakeSendThread");
        this.setDaemon(true);
    }

    @Override
    public void run() {
        int count = 0;
        while (BareBonesVCClient.INSTANCE.isRunning() && BareBonesVCClient.INSTANCE.config == null && count < MAX_SENDS) {
            BareBonesVCClient.INSTANCE.send(this.clientHelloPacket);
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
    }
}
