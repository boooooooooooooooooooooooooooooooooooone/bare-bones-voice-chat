package xyz.pobob.barebonesvc.packet.handler;

import xyz.pobob.barebonesvc.packet.ClientHashPacket;
import xyz.pobob.barebonesvc.packet.ServerHelloPacket;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;
import xyz.pobob.barebonesvc.voiceclient.SessionConfig;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ServerHelloHandler implements ServerPacketHandler {

    private final ServerHelloPacket serverHelloPacket = new ServerHelloPacket();
    private final ClientHashPacket clientHashPacket = new ClientHashPacket();

    @Override
    public void handle(byte[] data) {
        if (BareBonesVCClient.INSTANCE.waitingForServerHello) {
            BareBonesVCClient.INSTANCE.waitingForServerHello = false;

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

                            final ScheduledFuture<?> task = BareBonesVCClient.INSTANCE.scheduler.scheduleAtFixedRate(() -> {
                                if (BareBonesVCClient.INSTANCE.waitingForAuth) {
                                    BareBonesVCClient.INSTANCE.send(this.clientHashPacket);
                                }
                            }, 0L, 995L, TimeUnit.MILLISECONDS);
                            BareBonesVCClient.INSTANCE.scheduler.schedule(() -> task.cancel(false), 10L, TimeUnit.SECONDS);

                        } else {
                            BareBonesVCClient.INSTANCE.onDisconnect();
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