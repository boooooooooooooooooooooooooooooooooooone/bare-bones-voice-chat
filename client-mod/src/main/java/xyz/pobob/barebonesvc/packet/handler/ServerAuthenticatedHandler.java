package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public class ServerAuthenticatedHandler implements ServerPacketHandler {
    @Override
    public void handle(byte[] data) {
        if (BareBonesVCClient.INSTANCE.waitingForAuth) {
            BareBonesVCClient.INSTANCE.waitingForAuth = false;
            BareBonesVCClient.INSTANCE.onAuthenticated();
        }
    }
}
