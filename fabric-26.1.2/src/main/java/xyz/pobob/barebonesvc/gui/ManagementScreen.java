package xyz.pobob.barebonesvc.gui;

import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

public class ManagementScreen extends VoiceChatScreenBase {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(BareBonesVC.MOD_ID, "textures/gui/gui_barebonesvc.png");
    private static final Component TITLE = Component.literal("Bare Bones VC");
    private static final Component DISCONNECT = Component.literal("Disconnect");

    protected ClientList clientList;
    protected SessionEventFeed feed;

    public ManagementScreen() {
        super(TITLE, 256, 256);
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        Button disconnect = Button.builder(DISCONNECT, _ -> {
            BareBonesVCClient.INSTANCE.onDisconnect(true);
            Minecraft.getInstance().setScreen(null);
        }).bounds(this.guiLeft + 68, this.guiTop + this.ySize - 27, this.xSize - 136, 20).build();
        this.addRenderableWidget(disconnect);

        if (this.feed != null) {
            this.feed.setRectangle(146, 202, this.guiLeft + 11, this.guiTop + 20);
            this.feed.refreshScrollAmount();
        } else {
            this.feed = new SessionEventFeed(146, 202, this.guiLeft + 11, this.guiTop + 20);
        }
        this.addRenderableWidget(this.feed);

        if (this.clientList != null) {
            this.clientList.setRectangle(78, 202, this.guiLeft + 167, this.guiTop + 20);
            this.clientList.refreshScrollAmount();
        } else {
            this.clientList = new ClientList(78, 202, this.guiLeft + 167, this.guiTop + 20);
        }
        this.addRenderableWidget(this.clientList);
    }

    @Override
    public void extractBackgroundRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.guiLeft, this.guiTop, 0.0F, 0.0F, this.xSize, this.ySize, 256, 256);
    }

    @Override
    public void extractForegroundRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.text(this.font, TITLE, this.guiLeft + this.xSize / 2 - this.font.width(TITLE) / 2, this.guiTop + 7, FONT_COLOR, false);

        this.clientList.extractWidgetRenderState(context, mouseX, mouseY, delta);
        this.feed.extractWidgetRenderState(context, mouseX, mouseY, delta);
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
