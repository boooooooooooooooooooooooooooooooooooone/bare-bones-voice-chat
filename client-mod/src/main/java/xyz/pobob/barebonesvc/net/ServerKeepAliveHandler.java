package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public class ServerKeepAliveHandler implements ServerPacketHandler {

    private final ServerKeepAlivePacket serverKeepAlivePacket = new ServerKeepAlivePacket();
    private final ClientKeepAlivePacket clientKeepAlivePacket = new ClientKeepAlivePacket();

    @Override
    public void handle(byte[] data) {
        if (BareBonesVCClient.INSTANCE.isConnected()) {
            BareBonesVCClient.INSTANCE.lastKeepAlive = System.currentTimeMillis();

            this.serverKeepAlivePacket.deserialize(data);
            this.clientKeepAlivePacket.create(this.serverKeepAlivePacket.getId());
            BareBonesVCClient.INSTANCE.send(this.clientKeepAlivePacket);
        }
    }
}
