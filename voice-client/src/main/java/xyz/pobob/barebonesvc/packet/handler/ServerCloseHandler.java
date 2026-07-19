package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public class ServerCloseHandler implements ServerPacketHandler {
    @Override
    public void handle(byte[] data) {
        BareBonesVCClient.INSTANCE.sendMessage("Voice server was stopped", true);
        BareBonesVCClient.INSTANCE.onDisconnect(true);
    }
}
