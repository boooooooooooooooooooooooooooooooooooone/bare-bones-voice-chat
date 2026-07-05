package xyz.pobob.barebonesvc.net;

import xyz.pobob.barebonesvc.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;
import xyz.pobob.barebonesvc.voiceserver.Config;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;
import xyz.pobob.barebonesvc.voiceserver.thread.MiscNetworkThreads;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class ClientHelloHandler implements ClientPacketHandler {

    private final VoiceServer server;

    public ClientHelloHandler(VoiceServer server) {
        this.server = server;
    }

    private final ThreadLocal<ClientHelloPacket> localClientHelloPacket = ThreadLocal.withInitial(ClientHelloPacket::new);
    private final ThreadLocal<ServerHelloPacket> localServerHelloPacket = ThreadLocal.withInitial(ServerHelloPacket::new);
    private final ThreadLocal<ServerUpdatePlayerPacket> localServerUpdatePlayerPacket = ThreadLocal.withInitial(ServerUpdatePlayerPacket::new);

    @Override
    public void handle(byte[] data, SocketAddress clientAddress) {
        ClientHelloPacket clientHelloPacket = this.localClientHelloPacket.get();
        clientHelloPacket.deserialize(data);

        if (this.server.connected.containsKey(clientAddress)
                || this.server.connected.values()
                .stream().map(ClientConnection::getUUID)
                .toList()
                .contains(clientHelloPacket.getUUID())) return;
        BareBonesVCServer.LOGGER.info("Client connected: " + clientHelloPacket.getUsername() + " (" + clientHelloPacket.getUUID() + ")");

        Config config = this.server.config;
        this.localServerHelloPacket.get().create(
                config.mojangAuth,
                config.voiceDistance,
                config.codec
        );

        final byte[] serverHelloData = this.localServerHelloPacket.get().serialize();
        if (!this.server.send(serverHelloData, clientAddress)) {
            BareBonesVCServer.LOGGER.severe("An error occurred while sending handshake to " + clientAddress);
            return;
        }

        this.server.scheduler.schedule(() -> this.server.send(serverHelloData, clientAddress), 500, TimeUnit.MILLISECONDS);
        this.server.scheduler.schedule(() -> this.server.send(serverHelloData, clientAddress), 1000, TimeUnit.MILLISECONDS);
        this.server.scheduler.schedule(() -> this.server.send(serverHelloData, clientAddress), 1500, TimeUnit.MILLISECONDS);

        this.server.scheduler.schedule(() -> MiscNetworkThreads.sendPlayerList(this.server, clientAddress), 2000, TimeUnit.MILLISECONDS);
        this.server.scheduler.schedule(() -> MiscNetworkThreads.sendPlayerList(this.server, clientAddress), 3000, TimeUnit.MILLISECONDS);
        this.server.scheduler.schedule(() -> MiscNetworkThreads.sendPlayerList(this.server, clientAddress), 4000, TimeUnit.MILLISECONDS);

        this.server.connected.put(clientAddress, new ClientConnection(
                clientHelloPacket.getUsername(),
                clientHelloPacket.getUUID(),
                clientHelloPacket.isDisabled()
        ));

        this.localServerUpdatePlayerPacket.get().create(clientHelloPacket.getUsername(), clientHelloPacket.getUUID(), clientHelloPacket.isDisabled(), false);
        this.server.announceExcluding(this.localServerUpdatePlayerPacket.get().serialize(), clientAddress);
    }
}
