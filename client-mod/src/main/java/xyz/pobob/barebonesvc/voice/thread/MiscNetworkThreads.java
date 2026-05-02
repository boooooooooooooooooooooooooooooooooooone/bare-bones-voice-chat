package xyz.pobob.barebonesvc.voice.thread;

import de.maxhenkel.voicechat.voice.client.ClientManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.net.ClientKeepAlivePacket;
import xyz.pobob.barebonesvc.net.ClientUpdatePlayerPacket;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

public class MiscNetworkThreads {

    public static void startSendingKeepAlives() {
        ClientKeepAlivePacket keepAlive = new ClientKeepAlivePacket();

        Thread keepAliveSendThread = new Thread(() -> {

            while (BareBonesVCSession.instance().isRunning()) {

                keepAlive.create();
                BareBonesVCSession.instance().send(keepAlive.serialize());

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

            }

        });

        keepAliveSendThread.setDaemon(true);
        keepAliveSendThread.setName("BareBonesVCKeepAliveSendThread");
        keepAliveSendThread.start();
    }

    public static void startCheckingConnectionHealth() {
        Thread keepAliveCheckThread = new Thread(() -> {
            while (BareBonesVCSession.instance().isRunning()) {
                if (System.currentTimeMillis() - BareBonesVCSession.instance().lastKeepAlive > 30000) {
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.sendMessage(Text.of("Bare Bones VC connection timed out"), true);
                    }

                    BareBonesVCSession.instance().disconnect();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        keepAliveCheckThread.setDaemon(true);
        keepAliveCheckThread.setName("BareBonesVCCheckConnectionThread");
        keepAliveCheckThread.start();
    }

    public static void startUpdatingPlayerState() {
        final ClientUpdatePlayerPacket clientUpdatePlayerPacket = new ClientUpdatePlayerPacket();

        Thread updatePlayerState = new Thread(() -> {
            boolean last = isDisabled();

            clientUpdatePlayerPacket.create(last, false);
            BareBonesVCSession.instance().send(clientUpdatePlayerPacket.serialize());

            while (BareBonesVCSession.instance().isRunning()) {
                boolean current = isDisabled();
                if (current != last) {

                    clientUpdatePlayerPacket.create(current, false);
                    BareBonesVCSession.instance().send(clientUpdatePlayerPacket.serialize());
                    last = current;

                }

                try {
                    Thread.sleep(1250);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        updatePlayerState.setDaemon(true);
        updatePlayerState.setName("BareBonesVCUpdateStateThread");
        updatePlayerState.start();
    }

    private static synchronized boolean isDisabled() {
        return ClientManager.getPlayerStateManager().isDisabled();
    }

}
