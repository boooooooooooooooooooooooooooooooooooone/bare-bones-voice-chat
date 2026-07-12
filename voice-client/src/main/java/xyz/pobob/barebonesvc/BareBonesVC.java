package xyz.pobob.barebonesvc;

import xyz.pobob.barebonesvc.packet.PacketType;
import xyz.pobob.barebonesvc.packet.handler.*;
import xyz.pobob.barebonesvc.packet.registry.PacketRegistry;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public abstract class BareBonesVC {

    public static final String MOD_ID = "barebonesvc";
    public static Logger LOGGER;

    static {
        PacketRegistry.registerHandler(
                PacketType.SERVER_HELLO,
                new ServerHelloHandler()
        );
        PacketRegistry.registerHandler(
                PacketType.SERVER_ACK,
                new ServerAckHandler()
        );
        PacketRegistry.registerHandler(
                PacketType.SERVER_KEEP_ALIVE,
                new ServerKeepAliveHandler()
        );
        PacketRegistry.registerHandler(
                PacketType.SERVER_AUDIO,
                new ServerAudioHandler()
        );
        PacketRegistry.registerHandler(
                PacketType.SERVER_UPDATE_PLAYER,
                new ServerUpdatePlayerHandler()
        );
        PacketRegistry.registerHandler(
                PacketType.SERVER_CLOSE,
                new ServerCloseHandler()
        );
        PacketRegistry.registerHandler(
                PacketType.SERVER_KICK,
                new ServerKickHandler()
        );
        PacketRegistry.registerHandler(
                PacketType.SERVER_PLAYER_LATENCY,
                new ServerPlayerLatencyHandler()
        );
        PacketRegistry.registerHandler(
                PacketType.SERVER_UPDATE_VOICE_DISTANCE,
                new ServerUpdateVoiceDistanceHandler()
        );
        PacketRegistry.registerHandler(
                PacketType.SERVER_AUTHENTICATED,
                new ServerAuthenticatedHandler()
        );
    }

    // ensure that the entrypoint method for each implementation consists only of a call to this method
    public void onStartup() {
        this.registerClientQuit(() -> {
            if (BareBonesVCClient.INSTANCE.isRunning()) {
                BareBonesVCClient.INSTANCE.onDisconnect();
            }
        });
    }

    public abstract void registerClientQuit(Runnable action);
}