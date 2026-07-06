package xyz.pobob.barebonesvc.voiceserver.thread;

import xyz.pobob.barebonesvc.net.ServerKeepAlivePacket;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

import java.net.SocketAddress;
import java.util.Map;

public class MiscNetworkThreads {
    private static final int TIMEOUT_MILLIS = 20000;

    public static void startKeepAliveThread(final VoiceServer server) {
        Thread keepAliveSendThread = new Thread(() -> {
            ServerKeepAlivePacket keepAlive = new ServerKeepAlivePacket();

            while (server.isRunning()) {

                for (SocketAddress address : server.connected.keySet()) {
                    keepAlive.create();
                    server.send(keepAlive, address);

                    server.latencyManager.registerSentTime(keepAlive.getId());
                }

                for (Map.Entry<SocketAddress, ClientConnection> client : server.connected.entrySet()) {
                    if (System.currentTimeMillis() - client.getValue().getLastKeepAliveSynced() > TIMEOUT_MILLIS) {
                        server.onTimeout(client.getKey());
                    }
                }

                try {
                    Thread.sleep(750);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

            }
        });

        keepAliveSendThread.setDaemon(true);
        keepAliveSendThread.setName("ConnectionHealthThread");
        keepAliveSendThread.start();
    }
}
