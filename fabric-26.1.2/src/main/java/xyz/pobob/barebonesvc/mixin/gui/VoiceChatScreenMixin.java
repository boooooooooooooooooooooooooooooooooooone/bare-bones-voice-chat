package xyz.pobob.barebonesvc.mixin.gui;

import de.maxhenkel.voicechat.gui.VoiceChatScreen;
import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.gui.ConnectionScreen;
import xyz.pobob.barebonesvc.gui.ManagementScreen;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

@Mixin(VoiceChatScreen.class)
public abstract class VoiceChatScreenMixin extends VoiceChatScreenBase {
    @Unique private static final Component BARE_BONES_VC = Component.literal("Bare Bones VC");
    @Unique private static final Identifier TEXTURE_REPLACEMENT = Identifier.fromNamespaceAndPath(BareBonesVC.MOD_ID, "textures/gui/gui_voicechat.png");

    protected VoiceChatScreenMixin(Component title, int xSize, int ySize) {
        super(title, xSize, ySize);
    }

    @Inject(
            method = "init()V",
            at = @At("HEAD")
    )
    private void modifySize(CallbackInfo ci) {
        this.xSize = 195;
        this.ySize = 93;
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }

    @Inject(
            method = "init()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/gui/VoiceChatScreen;checkButtons()V"
            )
    )
    private void onInit(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();

        Button bareBonesVC = Button.builder(BARE_BONES_VC,
                _ -> minecraft.setScreen(BareBonesVCClient.INSTANCE.isRunning() ? new ManagementScreen() : new ConnectionScreen())).bounds(this.guiLeft + 6, this.guiTop + 6 + 38, 183, 20).build();
        this.addRenderableWidget(bareBonesVC);

    }

    @Redirect(
            method = "extractBackgroundRenderState",
            at = @At(
                    value = "FIELD",
                    target = "Lde/maxhenkel/voicechat/gui/VoiceChatScreen;TEXTURE:Lnet/minecraft/resources/Identifier;",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private Identifier identifier() {
        return TEXTURE_REPLACEMENT;
    }

    @Inject(
            method = "init()V",
            at = @At("TAIL")
    )
    private void moveHideButton(CallbackInfo ci) {
        for (GuiEventListener child : this.children()) {
            if (child instanceof Button button && button.getX() == this.guiLeft + this.xSize - 6 - 75 + 1) {
                button.setX(button.getX() - 1);
            }
        }
    }
}