package xyz.pobob.barebonesvc;

import net.fabricmc.api.ClientModInitializer;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;
import xyz.pobob.barebonesvc.voiceclient.FabricBareBonesVCClient;

public class FabricBareBonesVCInitializer implements ClientModInitializer {

    static {
        BareBonesVCClient.INSTANCE = new FabricBareBonesVCClient();
    }

    @Override
    public void onInitializeClient() {
        BareBonesVC.onStartup();
    }
}
