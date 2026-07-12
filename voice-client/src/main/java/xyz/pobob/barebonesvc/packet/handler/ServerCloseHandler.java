package xyz.pobob.barebonesvc.packet.handler;

import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public class ServerCloseHandler implements ServerPacketHandler {
    @Override
    public void handle(byte[] data) {
        BareBonesVCClient.sendMessageSafe(Text.of("Bare Bones VC server was stopped"), true);
        BareBonesVCClient.INSTANCE.onDisconnect();
    }
}
