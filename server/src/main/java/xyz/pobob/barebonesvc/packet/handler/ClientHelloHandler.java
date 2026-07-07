package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.packet.ClientHelloPacket;
import xyz.pobob.barebonesvc.packet.ServerHelloPacket;
import xyz.pobob.barebonesvc.packet.ServerUpdatePlayerPacket;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;
import xyz.pobob.barebonesvc.voiceserver.Config;

import java.net.SocketAddress;

public class ClientHelloHandler implements ClientPacketHandler {

    private final BareBonesVCServer server;

    public ClientHelloHandler(BareBonesVCServer server) {
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

        BareBonesVC.LOGGER.info("Client connected: " + clientHelloPacket.getUsername() + " (" + clientHelloPacket.getUUID() + ")");

        this.server.connected.put(clientAddress, new ClientConnection(
                clientHelloPacket.getUsername(),
                clientHelloPacket.getUUID(),
                clientHelloPacket.isDisabled()
        ));


        Config config = this.server.config;
        this.localServerHelloPacket.get().create(
                config.mojangAuth,
                config.voiceDistance,
                config.codec
        );

        this.server.send(this.localServerHelloPacket.get(), clientAddress);

        ServerUpdatePlayerPacket serverUpdatePlayerPacket = new ServerUpdatePlayerPacket();

        for (ClientConnection client : this.server.connected.values()) {
            serverUpdatePlayerPacket.create(client.getUsername(), client.getUUID(), client.isDisabled(), false);
            this.server.send(serverUpdatePlayerPacket, clientAddress);
        }

        this.localServerUpdatePlayerPacket.get().create(
                clientHelloPacket.getUsername(),
                clientHelloPacket.getUUID(),
                clientHelloPacket.isDisabled(),
                false
        );
        this.server.announceExcluding(this.localServerUpdatePlayerPacket.get(), clientAddress);
    }
}