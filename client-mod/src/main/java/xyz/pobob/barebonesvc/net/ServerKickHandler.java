package xyz.pobob.barebonesvc.net;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.voice.BareBonesVCClient;

public class ServerKickHandler implements ServerPacketHandler {
    @Override
    public void handle(byte[] data) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.of("Kicked from Bare Bones VC server"), true);
        }
        BareBonesVCClient.INSTANCE.disconnect();
    }
}
