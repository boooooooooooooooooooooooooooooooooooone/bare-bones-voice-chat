package xyz.pobob.barebonesvc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

public class BareBonesVCClient implements ClientModInitializer {

    public static final String MOD_ID = "barebonesvc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STOPPING.register(minecraftClient -> {
            if (BareBonesVCSession.instance().isRunning()) {
                BareBonesVCSession.instance().disconnect();
            }
        });
    }
}