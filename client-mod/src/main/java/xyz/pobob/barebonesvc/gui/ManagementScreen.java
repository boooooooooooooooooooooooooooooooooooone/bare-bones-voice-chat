package xyz.pobob.barebonesvc.gui;

import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import xyz.pobob.barebonesvc.BareBonesVCClient;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

public class ManagementScreen extends VoiceChatScreenBase {
    private static final Identifier TEXTURE = Identifier.of(BareBonesVCClient.MOD_ID, "textures/gui/gui_barebonesvc.png");
    private static final Text TITLE = Text.of("Bare Bones VC");
    private static final Text DISCONNECT = Text.of("Disconnect");

    protected ClientList clientList;
    protected int units;

    protected static final int HEADER_SIZE = 18;
    protected static final int FOOTER_SIZE = 32;
    protected static final int UNIT_SIZE = 20;
    protected static final int CELL_HEIGHT = 20;

    public ManagementScreen() {
        super(TITLE, 256, 256);
    }

    @Override
    protected void init() {
        super.init();
        this.hoverAreas.clear();
        this.clearChildren();

        ButtonWidget disconnect = ButtonWidget.builder(DISCONNECT, button -> {
            BareBonesVCSession.instance().disconnect();
            MinecraftClient.getInstance().setScreen(null);
        }).dimensions(this.guiLeft + 68, this.guiTop + this.ySize - 27, this.xSize - 136, 20).build();
        this.addDrawableChild(disconnect);

        int minUnits = MathHelper.ceil((float) (CELL_HEIGHT + 4) / (float) UNIT_SIZE);
        this.units = Math.max(minUnits, (height - HEADER_SIZE - FOOTER_SIZE - guiTop * 2) / UNIT_SIZE);

        if (this.clientList != null) {
            this.clientList.updateSize(width - 10, units * UNIT_SIZE + 6, 0, guiTop + HEADER_SIZE);
        } else {
            this.clientList = new ClientList(width - 10, units * UNIT_SIZE + 6, guiTop + HEADER_SIZE, CELL_HEIGHT, this);
        }

        this.addSelectableChild(this.clientList);
    }

    @Override
    public void renderBackground(DrawContext guiGraphics, int mouseX, int mouseY, float deltaTicks) {
        guiGraphics.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.guiLeft, this.guiTop, 0.0F, 0.0F, this.xSize, this.ySize, 256, 256);
    }

    @Override
    public void renderForeground(DrawContext guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawText(this.textRenderer, TITLE, this.guiLeft + this.xSize / 2 - this.textRenderer.getWidth(TITLE) / 2, this.guiTop + 7, -12566464, false);

        if (!this.clientList.children().isEmpty()) {
            this.clientList.render(guiGraphics, mouseX, mouseY, delta);
        }
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
