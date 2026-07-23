package xyz.pobob.barebonesvc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import xyz.pobob.barebonesvc.client.BareBonesVCClient;
import xyz.pobob.barebonesvc.client.FabricBareBonesVCClient;
import xyz.pobob.barebonesvc.gui.ConnectionScreen;
import xyz.pobob.barebonesvc.gui.ManagementScreen;

public class FabricBareBonesVCInitializer implements ClientModInitializer {

    static {
        BareBonesVCClient.INSTANCE = new FabricBareBonesVCClient();
    }

    @Override
    public void onInitializeClient() {
        BareBonesVC.onStartup();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_MENU.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(BareBonesVCClient.INSTANCE.isRunning() ? new ManagementScreen() : new ConnectionScreen());
                }
            }
        });
    }

    public static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of(BareBonesVC.MOD_ID, "bare_bones_voice_chat"));

    public static final KeyBinding OPEN_MENU = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.open_menu",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_B,
                    CATEGORY
            )
    );
}