package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public class ServerKickHandler implements ServerPacketHandler {
    @Override
    public void handle(byte[] data) {
        BareBonesVCClient.INSTANCE.sendMessage("Kicked from Bare Bones VC server", true);
        BareBonesVCClient.INSTANCE.onDisconnect();
    }
}
