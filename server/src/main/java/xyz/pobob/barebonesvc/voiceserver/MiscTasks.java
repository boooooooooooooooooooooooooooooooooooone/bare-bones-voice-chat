package xyz.pobob.barebonesvc.voiceserver;

import xyz.pobob.barebonesvc.packet.ServerKeepAlivePacket;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MiscTasks {
    private static final int TIMEOUT_MILLIS = 20000;

    public static void startKeepAliveTask(final BareBonesVCServer server) {
        final ServerKeepAlivePacket keepAlive = new ServerKeepAlivePacket();

        server.scheduler.scheduleAtFixedRate(() -> {
            for (SocketAddress address : server.connected.keySet()) {
                keepAlive.create();
                server.send(keepAlive, address);

                server.latencyManager.registerSentTime(keepAlive.getId());
            }

            for (Map.Entry<SocketAddress, ClientConnection> client : server.connected.entrySet()) {
                if (System.currentTimeMillis() - client.getValue().getLastKeepAlive() > TIMEOUT_MILLIS) {
                    server.onTimeout(client.getKey());
                }
            }

        }, 0L, 750L, TimeUnit.MILLISECONDS);
    }
}