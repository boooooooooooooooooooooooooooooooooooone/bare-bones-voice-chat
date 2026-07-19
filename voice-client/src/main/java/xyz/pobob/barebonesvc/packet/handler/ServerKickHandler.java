package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public class ServerKickHandler implements ServerPacketHandler {
    @Override
    public void handle(byte[] data) {
        BareBonesVCClient.INSTANCE.sendMessage("Kicked from voice server", true);
        BareBonesVCClient.INSTANCE.onDisconnect(true);
    }
}
