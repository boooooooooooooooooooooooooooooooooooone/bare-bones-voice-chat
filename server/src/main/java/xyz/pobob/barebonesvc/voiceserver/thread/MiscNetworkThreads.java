package xyz.pobob.barebonesvc.voiceserver.thread;

import xyz.pobob.barebonesvc.BareBonesVCServer;
import xyz.pobob.barebonesvc.net.ServerKeepAlivePacket;
import xyz.pobob.barebonesvc.voiceserver.ClientConnection;
import xyz.pobob.barebonesvc.voiceserver.VoiceServer;

import java.net.SocketAddress;
import java.util.Map;

public class MiscNetworkThreads {

    public static void startSending(VoiceServer server) {
        Thread keepAliveSendThread = new Thread(() -> {
            ServerKeepAlivePacket keepAlive = new ServerKeepAlivePacket();

            while (server.isRunning()) {

                for (SocketAddress address : server.connected.keySet()) {
                    keepAlive.create();
                    server.send(keepAlive.serialize(), address);
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
        keepAliveSendThread.setName("KeepAliveSendThread");
        keepAliveSendThread.start();
    }

    public static void startCheckingConnectionHealth(VoiceServer server) {
        Thread keepAliveCheckThread = new Thread(() -> {
            while (server.isRunning()) {
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
                }
            }
        });

        keepAliveCheckThread.setDaemon(true);
        keepAliveCheckThread.setName("ClientConnectionHealthThread");
        keepAliveCheckThread.start();
    }

}
