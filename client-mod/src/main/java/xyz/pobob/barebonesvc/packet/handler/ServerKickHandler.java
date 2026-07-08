package xyz.pobob.barebonesvc.packet.handler;

import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public class ServerKickHandler implements ServerPacketHandler {
    @Override
    public void handle(byte[] data) {
        BareBonesVCClient.sendMessageSafe(Text.of("Kicked from Bare Bones VC server"), true);
        BareBonesVCClient.INSTANCE.onDisconnect();
    }
}
