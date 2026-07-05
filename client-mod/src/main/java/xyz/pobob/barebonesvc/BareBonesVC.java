package xyz.pobob.barebonesvc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.pobob.barebonesvc.voice.BareBonesVCClient;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BareBonesVC implements ClientModInitializer {

    public static final String MOD_ID = "barebonesvc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Map<UUID, Double> LATENCIES = new ConcurrentHashMap<>();

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STOPPING.register(minecraftClient -> {
            if (BareBonesVCClient.INSTANCE.isRunning()) BareBonesVCClient.INSTANCE.disconnect();
        });
    }
}