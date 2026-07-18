package xyz.pobob.barebonesvc.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.pobob.barebonesvc.gui.ConnectionScreen;
import xyz.pobob.barebonesvc.gui.ManagementScreen;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

import static xyz.pobob.barebonesvc.FabricBareBonesVCInitializer.OPEN_MENU;

@Mixin(Screen.class)
public class ScreenMixin {
    @Shadow @Final protected MinecraftClient client;

    @Inject(
            method = "keyPressed",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onKeyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {

        if (OPEN_MENU.matchesKey(input) && this.client.currentScreen instanceof TitleScreen) {

            this.client.setScreen(BareBonesVCClient.INSTANCE.isRunning() ? new ManagementScreen() : new ConnectionScreen());
            cir.setReturnValue(true);
        }
    }
}