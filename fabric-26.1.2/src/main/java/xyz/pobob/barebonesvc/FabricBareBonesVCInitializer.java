package xyz.pobob.barebonesvc;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import xyz.pobob.barebonesvc.gui.ConnectionScreen;
import xyz.pobob.barebonesvc.gui.ManagementScreen;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;
import xyz.pobob.barebonesvc.voiceclient.FabricBareBonesVCClient;

public class FabricBareBonesVCInitializer implements ClientModInitializer {

    static {
        BareBonesVCClient.INSTANCE = new FabricBareBonesVCClient();
    }

    @Override
    public void onInitializeClient() {
        BareBonesVC.onStartup();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_MENU.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(BareBonesVCClient.INSTANCE.isRunning() ? new ManagementScreen() : new ConnectionScreen());
                }
            }
        });
    }

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(BareBonesVC.MOD_ID, "bare_bones_voice_chat"));

    public static final KeyMapping OPEN_MENU = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.open_menu",
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_B,
                    CATEGORY
            )
    );
}
