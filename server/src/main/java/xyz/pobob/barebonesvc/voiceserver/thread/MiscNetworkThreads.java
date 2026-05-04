package xyz.pobob.barebonesvc.voiceserver.thread;

import xyz.pobob.barebonesvc.BareBonesVCServer;
import xyz.pobob.barebonesvc.net.ServerKeepAlivePacket;
import xyz.pobob.barebonesvc.net.ServerUpdatePlayerPacket;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MiscNetworkThreads {

    public static void startKeepAliveThread(final VoiceServer server) {
        Thread keepAliveSendThread = new Thread(() -> {
            ServerKeepAlivePacket keepAlive = new ServerKeepAlivePacket();

            while (server.isRunning()) {

                for (SocketAddress address : server.connected.keySet()) {
                    keepAlive.create();
                    server.send(keepAlive.serialize(), address);
                }

                for (Map.Entry<SocketAddress, ClientConnection> client : server.connected.entrySet()) {
                    if (System.currentTimeMillis() - client.getValue().getLastKeepAliveSynced() > 30000) {
                        if (server.connected.containsKey(client.getKey())) {
                            BareBonesVCServer.LOGGER.info(server.connected.remove(client.getKey()).getUsername()
                                    + " timed out");
                        }
                    }
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

            }
        });

        keepAliveSendThread.setDaemon(true);
        keepAliveSendThread.setName("KeepAliveThread");
        keepAliveSendThread.start();
    }

    private static final ExecutorService SINGLE_THREAD_POOL = Executors.newSingleThreadExecutor();

    public static void sendPlayerListWithDelay(final VoiceServer server, final SocketAddress clientAddress) {
        SINGLE_THREAD_POOL.submit(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            ServerUpdatePlayerPacket serverUpdatePlayerPacket = new ServerUpdatePlayerPacket();

            for (ClientConnection client : server.connected.values()) {
                serverUpdatePlayerPacket.create(client.getUsername(), client.getUUID(), client.isDisabled(), false);
                server.send(serverUpdatePlayerPacket.serialize(), clientAddress);
            }
        });
    }
}
