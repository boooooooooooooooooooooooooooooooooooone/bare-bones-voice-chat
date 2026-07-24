package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.client.BareBonesVCClient;
import xyz.pobob.barebonesvc.packet.ClientKeepAlivePacket;
import xyz.pobob.barebonesvc.packet.ServerKeepAlivePacket;

public class ServerKeepAliveHandler implements ServerPacketHandler {

    private final ServerKeepAlivePacket serverKeepAlivePacket = new ServerKeepAlivePacket();
    private final ClientKeepAlivePacket clientKeepAlivePacket = new ClientKeepAlivePacket();

    @Override
    public void handle(byte[] data) {
        if (BareBonesVCClient.instance.isConnected()) {
            BareBonesVCClient.instance.lastKeepAlive = System.currentTimeMillis();

            this.serverKeepAlivePacket.deserialize(data);
            this.clientKeepAlivePacket.create(this.serverKeepAlivePacket.getId());
            BareBonesVCClient.instance.send(this.clientKeepAlivePacket);
        }
    }
}
