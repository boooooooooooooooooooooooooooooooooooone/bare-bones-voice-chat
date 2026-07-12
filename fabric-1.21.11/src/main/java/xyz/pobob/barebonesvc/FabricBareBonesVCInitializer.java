package xyz.pobob.barebonesvc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.LoggerFactory;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;
import xyz.pobob.barebonesvc.voiceclient.FabricBareBonesVCClient;

public class FabricBareBonesVCInitializer extends BareBonesVC implements ClientModInitializer {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BareBonesVC.MOD_ID);
    static {
        BareBonesVCClient.INSTANCE = new FabricBareBonesVCClient();

        BareBonesVC.LOGGER = new Logger() {
            @Override
            public void info(String msg) {
                FabricBareBonesVCInitializer.LOGGER.info(msg);
            }

            @Override
            public void warn(String msg) {
                FabricBareBonesVCInitializer.LOGGER.warn(msg);
            }

            @Override
            public void error(String msg, Throwable t) {
                FabricBareBonesVCInitializer.LOGGER.error(msg, t);
            }
        };
    }

    @Override
    public void registerClientQuit(Runnable action) {
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> action.run());
    }

    @Override
    public void onInitializeClient() {
        this.onStartup();
    }
}
