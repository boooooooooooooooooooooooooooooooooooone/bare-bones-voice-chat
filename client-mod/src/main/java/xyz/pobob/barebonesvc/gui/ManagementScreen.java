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

    protected ClientList clientList;
    protected SessionEventFeed feed;

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

        if (this.feed != null) {
            this.feed.setDimensionsAndPosition(146, 202, this.guiLeft + 11, this.guiTop + 20);
            this.feed.refreshScroll();
        } else {
            this.feed = new SessionEventFeed(146, 202, this.guiLeft + 11, this.guiTop + 20);
        }
        this.addDrawableChild(this.feed);

        if (this.clientList != null) {
            this.clientList.setDimensionsAndPosition(78, 202, this.guiLeft + 167, this.guiTop + 20);
            this.clientList.refreshScroll();
        } else {
            this.clientList = new ClientList(78, 202, this.guiLeft + 167, this.guiTop + 20);
        }
        this.addDrawableChild(this.clientList);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.guiLeft, this.guiTop, 0.0F, 0.0F, this.xSize, this.ySize, 256, 256);
    }

    @Override
    public void renderForeground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawText(this.textRenderer, TITLE, this.guiLeft + this.xSize / 2 - this.textRenderer.getWidth(TITLE) / 2, this.guiTop + 7, FONT_COLOR, false);

        this.clientList.render(context, mouseX, mouseY, delta);
        this.feed.render(context, mouseX, mouseY, delta);
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
