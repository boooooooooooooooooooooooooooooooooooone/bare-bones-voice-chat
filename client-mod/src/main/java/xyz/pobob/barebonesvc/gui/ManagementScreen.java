package xyz.pobob.barebonesvc.gui;

import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public class ManagementScreen extends VoiceChatScreenBase {
    private static final Identifier TEXTURE = Identifier.of(BareBonesVC.MOD_ID, "textures/gui/gui_barebonesvc.png");
    private static final Text TITLE = Text.of("Bare Bones VC");
    private static final Text DISCONNECT = Text.of("Disconnect");
    private static final int WHITE = 0xFFFFFFFF;

    protected ClientList clientList;
    protected int units;

    protected static final int HEADER_SIZE = 20;
    protected static final int FOOTER_SIZE = 36;
    protected static final int CELL_SIZE = 20;

    public ManagementScreen() {
        super(TITLE, 256, 256);
    }

    @Override
    protected void init() {
        super.init();
        this.clearChildren();

        ButtonWidget disconnect = ButtonWidget.builder(DISCONNECT, button -> {
            BareBonesVCClient.INSTANCE.onDisconnect();
            MinecraftClient.getInstance().setScreen(null);
        }).dimensions(this.guiLeft + 68, this.guiTop + this.ySize - 27, this.xSize - 136, 20).build();
        this.addDrawableChild(disconnect);

        this.units = Math.max(2, (height - HEADER_SIZE - FOOTER_SIZE - guiTop * 2) / CELL_SIZE);

        if (this.clientList != null) {
            this.clientList.setDimensionsAndPosition(80, this.units * CELL_SIZE + 2, this.guiLeft + 164, this.guiTop + HEADER_SIZE);
            this.clientList.refreshScroll();
        } else {
            this.clientList = new ClientList(80, this.units * CELL_SIZE + 2, this.guiLeft + 164, this.guiTop + HEADER_SIZE);
        }

        this.addSelectableChild(this.clientList);
    }

    @Override
    public void renderBackground(DrawContext guiGraphics, int mouseX, int mouseY, float deltaTicks) {
        guiGraphics.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.guiLeft, this.guiTop, 0.0F, 0.0F, this.xSize, this.ySize, 256, 256);
    }

    @Override
    public void renderForeground(DrawContext guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawText(this.textRenderer, TITLE, this.guiLeft + (this.xSize >> 1) - (this.textRenderer.getWidth(TITLE) >> 1), this.guiTop + 7, FONT_COLOR, false);
        if (BareBonesVCClient.INSTANCE.waitingForAuth) {
            guiGraphics.drawText(this.textRenderer, Text.of("Connection pending..."), this.guiLeft + 14, this.guiTop + 23, WHITE, false);
        }

        this.clientList.render(guiGraphics, mouseX, mouseY, delta);

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
