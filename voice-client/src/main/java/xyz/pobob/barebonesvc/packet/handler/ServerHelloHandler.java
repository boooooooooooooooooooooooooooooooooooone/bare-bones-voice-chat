package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.client.BareBonesVCClient;
import xyz.pobob.barebonesvc.packet.ClientHashPacket;
import xyz.pobob.barebonesvc.packet.ServerHelloPacket;
import xyz.pobob.barebonesvc.voice.SessionConfig;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ServerHelloHandler implements ServerPacketHandler {

    private final ServerHelloPacket serverHelloPacket = new ServerHelloPacket();
    private final ClientHashPacket clientHashPacket = new ClientHashPacket();
    private ScheduledFuture<?> task;

    public static volatile boolean waitingForServerHello = true;

    @Override
    public void handle(byte[] data) {
        if (waitingForServerHello) {
            waitingForServerHello = false;

            this.serverHelloPacket.deserialize(data);

            BareBonesVCClient.instance.config = new SessionConfig(
                    this.serverHelloPacket.getMojangAuth(),
                    (float) this.serverHelloPacket.getVoiceDistance(),
                    (float) this.serverHelloPacket.getWhisperDistance(),
                    this.serverHelloPacket.getCodec()
            );

            if (this.serverHelloPacket.getMojangAuth()) {
                BareBonesVCClient.instance.sendFeed("Verifying with Minecraft session server...");

                try {
                    final String digest = BareBonesVCClient.instance.getDigest(this.serverHelloPacket.getPublicKey());

                    BareBonesVCClient.instance.submitToIoThread(() -> {

                        if (BareBonesVCClient.instance.requestSessionServerJoin(digest)) {

                            this.clientHashPacket.create(digest);

                            int[] count = {0};
                            try {
                                this.task = BareBonesVCClient.instance.scheduler.scheduleAtFixedRate(() -> {
                                    if (!BareBonesVCClient.instance.isConnected() && count[0] < 10) {
                                        BareBonesVCClient.instance.send(this.clientHashPacket);
                                    } else {
                                        this.task.cancel(false);
                                        return;
                                    }
                                    count[0]++;
                                }, 0L, 995L, TimeUnit.MILLISECONDS);
                            } catch (RejectedExecutionException ignored) {
                            }
                        } else {
                            BareBonesVCClient.instance.onDisconnect("Failed to verify with voice server!", true);
                        }
                    });

                } catch (Exception e) {
                    BareBonesVCClient.instance.sendMessage("An error occurred. Check logs!", true);
                    throw new IllegalStateException("Protocol error", e);
                }
            } else {
                BareBonesVCClient.instance.onAuthenticated();
            }
        }
    }
}