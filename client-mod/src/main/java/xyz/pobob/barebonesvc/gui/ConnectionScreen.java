package xyz.pobob.barebonesvc.gui;

import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.pobob.barebonesvc.voice.BareBonesVCSession;

import java.net.URI;
import java.net.URISyntaxException;

public class ConnectionScreen extends VoiceChatScreenBase {
    private static final Identifier TEXTURE = Identifier.of(xyz.pobob.barebonesvc.BareBonesVCClient.MOD_ID, "textures/gui/gui_voicechat_connect.png");
    private static final Text TITLE = Text.of("Bare Bones VC - Connect");
    private static final Text HOST = Text.of("Host");
    private static final Text PORT = Text.of("Port");
    private static final Text CONNECT = Text.of("Connect");

    private TextFieldWidget host;
    private TextFieldWidget port;
    private ButtonWidget connect;

    public ConnectionScreen() {
        super(TITLE, 195, 101);
    }

    @Override
    protected void init() {
        super.init();
        this.hoverAreas.clear();
        this.clearChildren();

        this.host = new TextFieldWidget(
                this.textRenderer,
                this.guiLeft + 7,
                this.guiTop + 30,
                this.xSize - 14,
                14,
                Text.empty()
        );
        this.host.setMaxLength(48);
        this.addDrawableChild(this.host);

        this.port = new TextFieldWidget(
                this.textRenderer,
                this.guiLeft + 7,
                this.guiTop + 56,
                this.xSize - 14,
                14,
                Text.empty()
        );
        this.port.setMaxLength(5);
        this.addDrawableChild(this.port);

        this.connect = ButtonWidget.builder(CONNECT, button -> {
            this.connect();
            MinecraftClient.getInstance().setScreen(null);
        }).dimensions(this.guiLeft + 6, this.guiTop + this.ySize - 27, this.xSize - 12, 20).build();
        this.addDrawableChild(this.connect);
    }

    private void connect() {
        String host = this.host.getText().trim();
        String portText = this.port.getText().trim();

        int port;

        try {
            port = Integer.parseInt(portText);

            URI uri = new URI("http", null, host, port, null, null, null);

            BareBonesVCSession.instance().start(uri.getHost(), uri.getPort());
        } catch (URISyntaxException | NumberFormatException e) {
            xyz.pobob.barebonesvc.BareBonesVCClient.LOGGER.error("Invalid socket address \"{}:{}\"", host, portText);

            BareBonesVCSession.invalidAddress();
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.guiLeft, this.guiTop, 0.0F, 0.0F, this.xSize, this.ySize, 256, 256);
    }

    @Override
    public void renderForeground(DrawContext guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawText(this.textRenderer, TITLE, this.guiLeft + this.xSize / 2 - this.textRenderer.getWidth(TITLE) / 2, this.guiTop + 7, -12566464, false);

        guiGraphics.drawText(this.textRenderer, HOST, this.guiLeft + 8, this.guiTop + 7 + 9 + 5, -12566464, false);
        guiGraphics.drawText(this.textRenderer, PORT, this.guiLeft + 8, this.guiTop + 7 + (9 + 5) * 2 + 10 + 2, -12566464, false);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void resize(int width, int height) {
        String groupNameText = this.host.getText();
        String passwordText = this.port.getText();
        this.init(width, height);
        this.host.setText(groupNameText);
        this.port.setText(passwordText);
    }
}
