package xyz.pobob.barebonesvc.packet.handler;

import de.maxhenkel.voicechat.config.ServerConfig;
import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.packet.ServerHelloPacket;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;
import xyz.pobob.barebonesvc.voiceclient.MiscTasks;
import xyz.pobob.barebonesvc.voiceclient.SessionConfig;

public class ServerHelloHandler implements ServerPacketHandler {

    private final ServerHelloPacket serverHelloPacket = new ServerHelloPacket();

    @Override
    public void handle(byte[] data) {
        if (BareBonesVCClient.INSTANCE.config == null) {
            this.serverHelloPacket.deserialize(data);

            ServerConfig.Codec codec = switch (this.serverHelloPacket.getCodec()) {
                case VOIP -> ServerConfig.Codec.VOIP;
                case AUDIO -> ServerConfig.Codec.AUDIO;
                case RESTRICTED_LOWDELAY -> ServerConfig.Codec.RESTRICTED_LOWDELAY;
            };

            BareBonesVC.LOGGER.info(
                    "Server config packet received! mojang auth={}, voice distance={}, codec={}",
                    this.serverHelloPacket.getMojangAuth(),
                    this.serverHelloPacket.getVoiceDistance(),
                    codec
            );

            BareBonesVCClient.INSTANCE.config = new SessionConfig(
                    this.serverHelloPacket.getMojangAuth(),
                    (float) this.serverHelloPacket.getVoiceDistance(),
                    codec
            );

            BareBonesVCClient.INSTANCE.lastKeepAlive = System.currentTimeMillis();
            MiscTasks.startKeepAliveTask();

            if (this.serverHelloPacket.getMojangAuth()) {
                // TODO add mojang auth
                BareBonesVCClient.INSTANCE.startVoiceChat();
            } else {
                BareBonesVCClient.INSTANCE.startVoiceChat();
            }
        }
    }
}
