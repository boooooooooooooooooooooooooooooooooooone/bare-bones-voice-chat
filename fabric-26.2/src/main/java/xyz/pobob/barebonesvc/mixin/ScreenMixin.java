package xyz.pobob.barebonesvc.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.pobob.barebonesvc.client.BareBonesVCClient;
import xyz.pobob.barebonesvc.gui.ConnectionScreen;
import xyz.pobob.barebonesvc.gui.ManagementScreen;

import static xyz.pobob.barebonesvc.FabricBareBonesVCInitializer.OPEN_MENU;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(
            method = "keyPressed",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {

        if (OPEN_MENU.matches(event) && Minecraft.getInstance().gui.screen() instanceof TitleScreen) {

            Minecraft.getInstance().setScreenAndShow(BareBonesVCClient.INSTANCE.isRunning() ? new ManagementScreen() : new ConnectionScreen());
            cir.setReturnValue(true);
        }
    }
}
