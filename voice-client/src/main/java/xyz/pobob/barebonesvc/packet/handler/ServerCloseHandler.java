package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.client.BareBonesVCClient;

public class ServerCloseHandler implements ServerPacketHandler {
    @Override
    public void handle(byte[] data) {
        BareBonesVCClient.instance.onDisconnect("Voice server was stopped", true);
    }
}
