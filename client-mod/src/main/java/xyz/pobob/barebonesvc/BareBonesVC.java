package xyz.pobob.barebonesvc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.pobob.barebonesvc.packet.PacketType;
import xyz.pobob.barebonesvc.packet.handler.*;
import xyz.pobob.barebonesvc.packet.registry.PacketRegistry;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BareBonesVC implements ClientModInitializer {

    public static final String MOD_ID = "barebonesvc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Map<UUID, Double> LATENCIES = new ConcurrentHashMap<>();

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
    }

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STOPPING.register(minecraftClient -> {
            if (BareBonesVCClient.INSTANCE.isRunning()) BareBonesVCClient.INSTANCE.onDisconnect();
        });
    }
}