package xyz.pobob.barebonesvc.packet.handler;

import de.maxhenkel.voicechat.config.ServerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.mixin.ClientLoginNetworkHandlerAccessor;
import xyz.pobob.barebonesvc.packet.ClientHashPacket;
import xyz.pobob.barebonesvc.packet.ServerHelloPacket;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;
import xyz.pobob.barebonesvc.voiceclient.SessionConfig;

import java.math.BigInteger;
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

            ServerConfig.Codec codec = switch (this.serverHelloPacket.getCodec()) {
                case VOIP -> ServerConfig.Codec.VOIP;
                case AUDIO -> ServerConfig.Codec.AUDIO;
                case RESTRICTED_LOWDELAY -> ServerConfig.Codec.RESTRICTED_LOWDELAY;
            };

            BareBonesVCClient.INSTANCE.config = new SessionConfig(
                    this.serverHelloPacket.getMojangAuth(),
                    (float) this.serverHelloPacket.getVoiceDistance(),
                    (float) this.serverHelloPacket.getWhisperDistance(),
                    codec
            );

            BareBonesVC.LOGGER.info(
                    "Server config packet received! mojang auth={}, voice distance={}, whisper distance={}, codec={}",
                    this.serverHelloPacket.getMojangAuth(),
                    this.serverHelloPacket.getVoiceDistance(),
                    this.serverHelloPacket.getWhisperDistance(),
                    codec
            );

            if (this.serverHelloPacket.getMojangAuth()) {
                try {
                    final String digest = new BigInteger(NetworkEncryptionUtils.computeServerId(
                            "",
                            NetworkEncryptionUtils.decodeEncodedRsaPublicKey(this.serverHelloPacket.getPublicKey()),
                            NetworkEncryptionUtils.generateSecretKey()
                    )).toString(16);

                    BareBonesVCClient.sendMessageSafe(Text.of("Verifying with Minecraft session server..."), true);

                    final ClientLoginNetworkHandler login = new ClientLoginNetworkHandler(null, MinecraftClient.getInstance(), null, null, false, null, component -> {}, null, null);
                    Util.getIoWorkerExecutor().execute(() -> {
                        Text text = ((ClientLoginNetworkHandlerAccessor) login).invokeJoinServerSession(digest);

                        if (text != null) {
                            BareBonesVCClient.sendMessageSafe(text, true);
                            BareBonesVCClient.INSTANCE.onDisconnect();
                            return;
                        }

                        this.clientHashPacket.create(digest);
                        final ScheduledFuture<?> task = BareBonesVCClient.INSTANCE.scheduler.scheduleAtFixedRate(() -> {
                            if (BareBonesVCClient.INSTANCE.waitingForAuth) {
                                BareBonesVCClient.INSTANCE.send(this.clientHashPacket);
                            }
                        }, 0L, 995L, TimeUnit.MILLISECONDS);
                        BareBonesVCClient.INSTANCE.scheduler.schedule(() -> task.cancel(false), 10L, TimeUnit.SECONDS);

                    });

                } catch (Exception e) {
                    BareBonesVCClient.sendMessageSafe(Text.of("An error occurred. Check logs!"), true);
                    throw new IllegalStateException("Protocol error", e);
                }
            } else {
                BareBonesVCClient.INSTANCE.onAuthenticated();
            }
        }
    }
}