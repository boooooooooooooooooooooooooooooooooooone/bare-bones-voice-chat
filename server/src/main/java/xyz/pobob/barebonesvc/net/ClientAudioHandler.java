package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.voiceserver.ClientConnection;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Objects;

public class ClientAudioHandler extends ClientPacketHandler {

    public ClientAudioHandler(VoiceServer server) {
        super(server);
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
