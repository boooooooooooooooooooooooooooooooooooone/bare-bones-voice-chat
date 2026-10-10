package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.client.BareBonesVCClient;
import xyz.pobob.barebonesvc.packet.ServerKickPacket;

public class ServerKickHandler implements ServerPacketHandler {
    private final ServerKickPacket serverKickPacket = new ServerKickPacket();

    @Override
    public void handle(byte[] data) {
        this.serverKickPacket.deserialize(data);

        BareBonesVCClient.instance.onDisconnect(
                this.serverKickPacket.isBanned() ? "Banned from voice server" : "Kicked from voice server",
                true
        );
    }
}
