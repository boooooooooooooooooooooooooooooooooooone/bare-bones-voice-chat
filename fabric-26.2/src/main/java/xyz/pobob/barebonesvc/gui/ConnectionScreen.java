package xyz.pobob.barebonesvc.gui;

import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

import java.net.URI;
import java.net.URISyntaxException;

public class ConnectionScreen extends VoiceChatScreenBase {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(BareBonesVC.MOD_ID, "textures/gui/gui_barebonesvc_connect.png");
    private static final Component TITLE = Component.literal("Bare Bones VC - Connect");
    private static final Component HOST = Component.literal("Host");
    private static final Component PORT = Component.literal("Port");
    private static final Component CONNECT = Component.literal("Connect");

    private EditBox host;
    private EditBox port;

    public ConnectionScreen() {
        super(TITLE, 195, 101);
    }

    @Override
    protected void init() {
        super.init();
        this.hoverAreas.clear();
        this.clearWidgets();

        this.host = new EditBox(
                this.font,
                this.guiLeft + 7,
                this.guiTop + 30,
                this.xSize - 14,
                14,
                CommonComponents.EMPTY
        );
        this.host.setMaxLength(48);
        this.addRenderableWidget(this.host);

        this.port = new EditBox(
                this.font,
                this.guiLeft + 7,
                this.guiTop + 56,
                this.xSize - 14,
                14,
                CommonComponents.EMPTY
        );
        this.port.setMaxLength(5);
        this.addRenderableWidget(this.port);

        Button connect = Button.builder(CONNECT, _ ->
                Minecraft.getInstance().setScreenAndShow(this.connect() ? new ManagementScreen() : null)).bounds(this.guiLeft + 6, this.guiTop + this.ySize - 27, this.xSize - 12, 20).build();
        this.addRenderableWidget(connect);
    }

    private boolean connect() {
        String host = this.host.getValue().trim();
        String portText = this.port.getValue().trim();

        int port;

        try {
            port = Integer.parseInt(portText);

            URI uri = new URI("http", null, host, port, null, null, null);

            BareBonesVCClient.INSTANCE.start(uri.getHost(), uri.getPort());
            return true;
        } catch (URISyntaxException | NumberFormatException e) {
            BareBonesVCClient.INSTANCE.sendMessage("Failed to resolve address", true);
            return false;
        }
    }

    @Override
    public void extractBackgroundRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.guiLeft, this.guiTop, 0.0F, 0.0F, this.xSize, this.ySize, 256, 256);
    }

    @Override
    public void extractForegroundRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.text(this.font, TITLE, this.guiLeft + this.xSize / 2 - this.font.width(TITLE) / 2, this.guiTop + 7, FONT_COLOR, false);

        context.text(this.font, HOST, this.guiLeft + 8, this.guiTop + 7 + (9 + 5), FONT_COLOR, false);
        context.text(this.font, PORT, this.guiLeft + 8, this.guiTop + 7 + (9 + 5) * 2 + 10 + 2, FONT_COLOR, false);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void resize(int width, int height) {
        String hostText = this.host.getValue();
        String portText = this.port.getValue();
        this.init(width, height);
        this.host.setValue(hostText);
        this.port.setValue(portText);
    }
}
