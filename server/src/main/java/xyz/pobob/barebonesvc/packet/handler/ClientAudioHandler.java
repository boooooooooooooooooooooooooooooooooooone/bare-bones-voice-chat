package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.packet.ClientAudioPacket;
import xyz.pobob.barebonesvc.packet.ServerAudioPacket;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Objects;

public class ClientAudioHandler implements ClientPacketHandler {

    private final BareBonesVCServer server;

    public ClientAudioHandler(BareBonesVCServer server) {
        this.server = server;
    }

    private final ThreadLocal<ClientAudioPacket> localClientAudioPacket = ThreadLocal.withInitial(ClientAudioPacket::new);
    private final ThreadLocal<ServerAudioPacket> localServerAudioPacket = ThreadLocal.withInitial(ServerAudioPacket::new);

    @Override
    public void handle(byte[] data, SocketAddress clientAddress) {
        this.localClientAudioPacket.get().deserialize(data);
        this.localServerAudioPacket.get().create(this.localClientAudioPacket.get(), this.server.connected.get(clientAddress).getUUID());

        for (Map.Entry<SocketAddress, ClientConnection> entry : this.server.connected.entrySet()) {
            if (!Objects.equals(entry.getKey(), clientAddress) && !entry.getValue().isDisabled()) {
                this.server.send(this.localServerAudioPacket.get(), entry.getKey());
            }
        }
    }
}
