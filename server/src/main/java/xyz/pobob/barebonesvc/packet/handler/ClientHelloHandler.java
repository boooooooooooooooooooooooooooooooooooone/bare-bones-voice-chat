package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.packet.ClientHelloPacket;
import xyz.pobob.barebonesvc.packet.ServerHelloPacket;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;
import xyz.pobob.barebonesvc.voiceserver.Config;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class ClientHelloHandler implements ClientPacketHandler {

    private final BareBonesVCServer server;

    public ClientHelloHandler(BareBonesVCServer server) {
        this.server = server;
    }

    private final ThreadLocal<ClientHelloPacket> localClientHelloPacket = ThreadLocal.withInitial(ClientHelloPacket::new);
    private final ThreadLocal<ServerHelloPacket> localServerHelloPacket = ThreadLocal.withInitial(ServerHelloPacket::new);

    @Override
    public void handle(byte[] data, final SocketAddress clientAddress) {
        ClientHelloPacket clientHelloPacket = this.localClientHelloPacket.get();
        clientHelloPacket.deserialize(data);

        if (this.server.isSocketConnected(clientAddress)
                || this.server.getAuthenticatedClients()
                .stream().map(ClientConnection::getUUID)
                .toList()
                .contains(clientHelloPacket.getUUID())) return;

        Config config = this.server.config;

        final ClientConnection conn = new ClientConnection(
                clientHelloPacket.getUsername(),
                clientHelloPacket.getUUID(),
                clientHelloPacket.isDisabled(),
                !config.mojangAuth
        );

        this.server.addClient(clientAddress, conn);
        this.server.scheduler.schedule(() -> {
            if (!conn.isAuthenticated()) {
                this.server.onDisconnect(clientAddress);
            }
        }, 20, TimeUnit.SECONDS);

        byte[] publicKey = null;
        if (config.mojangAuth) {
            publicKey = this.server.keyPair.getPublic().getEncoded();
        }

        this.localServerHelloPacket.get().create(
                config.mojangAuth,
                config.voiceDistance,
                config.codec,
                publicKey
        );

        this.server.send(this.localServerHelloPacket.get(), clientAddress);

        if (!config.mojangAuth) {
            this.server.onAuthenticated(clientAddress, conn);
        }
    }
}