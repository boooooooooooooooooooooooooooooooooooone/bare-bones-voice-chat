package xyz.pobob.barebonesvc.gui;

import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

public class ManagementScreen extends VoiceChatScreenBase {
    private static final Identifier TEXTURE = Identifier.of(xyz.pobob.barebonesvc.BareBonesVCClient.MOD_ID, "textures/gui/gui_voicechat_direct_connect.png");
    private static final Text TITLE = Text.of("Bare Bones VC - Management");
    private static final Text DISCONNECT = Text.of("Disconnect");

    private ButtonWidget disconnect;

    public ManagementScreen() {
        super(TITLE, 195, 49);
    }

    @Override
    protected void init() {
        super.init();
        this.hoverAreas.clear();
        this.clearChildren();

        this.disconnect = ButtonWidget.builder(DISCONNECT, button -> {
            this.disconnect();
            MinecraftClient.getInstance().setScreen(null);
        }).dimensions(this.guiLeft + 6, this.guiTop + this.ySize - 27, this.xSize - 12, 20).build();
        this.addDrawableChild(this.disconnect);
    }

    private void disconnect() {
        BareBonesVCSession.instance().disconnect();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.guiLeft, this.guiTop, 0.0F, 0.0F, this.xSize, this.ySize, 256, 256);
    }

    @Override
    public void renderForeground(DrawContext guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawText(this.textRenderer, TITLE, this.guiLeft + this.xSize / 2 - this.textRenderer.getWidth(TITLE) / 2, this.guiTop + 7, -12566464, false);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void resize(int width, int height) {
        this.init(width, height);
    }
}
