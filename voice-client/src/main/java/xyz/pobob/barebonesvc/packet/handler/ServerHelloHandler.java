package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.packet.ClientHashPacket;
import xyz.pobob.barebonesvc.packet.ServerHelloPacket;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;
import xyz.pobob.barebonesvc.voiceclient.SessionConfig;

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

            BareBonesVCClient.INSTANCE.clearPlayerStates();

            this.serverHelloPacket.deserialize(data);

            BareBonesVCClient.INSTANCE.config = new SessionConfig(
                    this.serverHelloPacket.getMojangAuth(),
                    (float) this.serverHelloPacket.getVoiceDistance(),
                    (float) this.serverHelloPacket.getWhisperDistance(),
                    this.serverHelloPacket.getCodec()
            );

            if (this.serverHelloPacket.getMojangAuth()) {
                BareBonesVCClient.INSTANCE.sendMessage("Verifying with Minecraft session server...", true);

                try {
                    final String digest = BareBonesVCClient.INSTANCE.getDigest(this.serverHelloPacket.getPublicKey());

                    BareBonesVCClient.INSTANCE.getIoWorkerExecutor().execute(() -> {

                        if (BareBonesVCClient.INSTANCE.requestSessionServerJoin(digest)) {

                            this.clientHashPacket.create(digest);

                            int[] count = {0};
                            try {
                                this.task = BareBonesVCClient.INSTANCE.scheduler.scheduleAtFixedRate(() -> {
                                    if (!BareBonesVCClient.INSTANCE.isConnected() && count[0] < 10) {
                                        BareBonesVCClient.INSTANCE.send(this.clientHashPacket);
                                    } else {
                                        return;
                                    }
                                    count[0]++;
                                }, 0L, 995L, TimeUnit.MILLISECONDS);
                            } catch (RejectedExecutionException ignored) {
                            }
                        } else {
                            BareBonesVCClient.INSTANCE.onDisconnect(true);
                        }
                    });

                } catch (Exception e) {
                    BareBonesVCClient.INSTANCE.sendMessage("An error occurred. Check logs!", true);
                    throw new IllegalStateException("Protocol error", e);
                }
            } else {
                BareBonesVCClient.INSTANCE.onAuthenticated();
            }
        }
    }
}