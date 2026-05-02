package xyz.pobob.barebonesvc.mixin.render;

import de.maxhenkel.voicechat.gui.VoiceChatScreen;
import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pobob.barebonesvc.gui.ConnectionScreen;
import xyz.pobob.barebonesvc.gui.ManagementScreen;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

@Mixin(VoiceChatScreen.class)
public abstract class VoiceChatScreenMixin extends VoiceChatScreenBase {
    @Unique private static final Text BARE_BONES_VC = Text.of("Bare Bones VC");
    @Unique private static final Identifier TEXTURE_REPLACEMENT = Identifier.of(xyz.pobob.barebonesvc.BareBonesVCClient.MOD_ID, "textures/gui/gui_voicechat.png");

    protected VoiceChatScreenMixin(Text title, int xSize, int ySize) {
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
        MinecraftClient minecraft = MinecraftClient.getInstance();

        ButtonWidget bareBonesVC = ButtonWidget.builder(BARE_BONES_VC,
                button -> minecraft.setScreen(BareBonesVCSession.instance().isRunning() ? new ManagementScreen() : new ConnectionScreen())).dimensions(this.guiLeft + 6, this.guiTop + 6 + 38, 183, 20).build();
        this.addDrawableChild(bareBonesVC);

    }

    @Redirect(
            method = "renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            at = @At(
                    value = "FIELD",
                    target = "Lde/maxhenkel/voicechat/gui/VoiceChatScreen;TEXTURE:Lnet/minecraft/util/Identifier;",
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
        for (Element element : this.children()) {
            if (element instanceof ButtonWidget widget && widget.getX() == this.guiLeft + this.xSize - 6 - 75 + 1) {
                widget.setX(widget.getX() - 1);
            }
        }
    }
}