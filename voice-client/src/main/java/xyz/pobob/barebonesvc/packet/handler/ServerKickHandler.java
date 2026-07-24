package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.client.BareBonesVCClient;

public class ServerKickHandler implements ServerPacketHandler {
    @Override
    public void handle(byte[] data) {
        BareBonesVCClient.instance.onDisconnect("Kicked from voice server", true);
    }
}
