package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.packet.ClientHashPacket;
import xyz.pobob.barebonesvc.packet.ServerAuthenticatedPacket;
import xyz.pobob.barebonesvc.packet.ServerKickPacket;
import xyz.pobob.barebonesvc.util.Util;
import xyz.pobob.barebonesvc.voiceserver.BareBonesVCServer;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;

import java.net.SocketAddress;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ClientHashHandler implements ClientPacketHandler {

    private final BareBonesVCServer server;

    public ClientHashHandler(BareBonesVCServer server) {
        this.server = server;
    }

    private final ThreadLocal<ClientHashPacket> localClientHashPacket = ThreadLocal.withInitial(ClientHashPacket::new);
    private final ServerKickPacket serverKickPacket = new ServerKickPacket();
    private final ServerAuthenticatedPacket serverAuthenticatedPacket = new ServerAuthenticatedPacket();

    @Override
    public void handle(byte[] data, final SocketAddress clientAddress) {
        if (this.server.config.mojangAuth) {
            final ClientConnection conn = this.server.getClient(clientAddress);
            if (conn != null && !conn.isAuthenticated()) {
                this.localClientHashPacket.get().deserialize(data);
                String hash = this.localClientHashPacket.get().getHash();

                CompletableFuture<HttpResponse<String>> response = Util.httpRequestAsync(
                        "GET",
                        String.format(
                                "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s",
                                URLEncoder.encode(conn.getUsername(), StandardCharsets.UTF_8),
                                URLEncoder.encode(hash, StandardCharsets.UTF_8)
                        ),
                        null,
                        Map.of("Accept", "application/json")
                );

                response.thenAcceptAsync(httpResponse -> {
                    if (httpResponse.statusCode() == 200) {
                        this.server.send(this.serverAuthenticatedPacket, clientAddress);
                        this.server.onAuthenticated(clientAddress, conn);
                    } else {
                        this.server.send(this.serverKickPacket, clientAddress);
                        this.server.onDisconnect(clientAddress);
                    }
                }, this.server.ioThread);
            }
        }
    }
}