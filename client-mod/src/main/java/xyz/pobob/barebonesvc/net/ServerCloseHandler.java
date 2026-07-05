package xyz.pobob.barebonesvc.net;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.voice.BareBonesVCClient;

public class ServerCloseHandler implements ServerPacketHandler {
    @Override
    public void handle(byte[] data) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.of("Bare Bones VC server was stopped"), true);
        }
        BareBonesVCClient.INSTANCE.disconnect();
    }
}
