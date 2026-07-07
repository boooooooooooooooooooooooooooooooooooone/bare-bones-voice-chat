package xyz.pobob.barebonesvc.packet.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public class ServerKickHandler implements ServerPacketHandler {
    @Override
    public void handle(byte[] data) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.of("Kicked from Bare Bones VC server"), true);
        }
        BareBonesVCClient.INSTANCE.onDisconnect();
    }
}
