package xyz.pobob.barebonesvc.gui;

import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.client.BareBonesVCClient;

public class ConnectionScreen extends VoiceChatScreenBase {
    private static final Identifier TEXTURE = Identifier.of(BareBonesVC.MOD_ID, "textures/gui/gui_barebonesvc_connect.png");
    private static final Text TITLE = Text.of("Bare Bones VC - Connect");
    private static final Text HOST = Text.of("Host");
    private static final Text PORT = Text.of("Port");
    private static final Text CONNECT = Text.of("Connect");

    private TextFieldWidget host;
    private TextFieldWidget port;

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
        this.host.setMaxLength(64);
        this.addDrawableChild(this.host);
        if (BareBonesVCClient.hostCache != null) {
            this.host.setText(BareBonesVCClient.hostCache);
        }

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
        if (BareBonesVCClient.portCache != null) {
            this.port.setText(BareBonesVCClient.portCache);
        }

        ButtonWidget connect = ButtonWidget.builder(CONNECT, button ->
                        BareBonesVCClient.instance.start(
                                this.host.getText().trim(),
                                this.port.getText().trim()
                        )
                ).dimensions(this.guiLeft + 6, this.guiTop + this.ySize - 27, this.xSize - 12, 20)
                .build();
        this.addDrawableChild(connect);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.guiLeft, this.guiTop, 0.0F, 0.0F, this.xSize, this.ySize, 256, 256);
    }

    @Override
    public void renderForeground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawText(this.textRenderer, TITLE, this.guiLeft + this.xSize / 2 - this.textRenderer.getWidth(TITLE) / 2, this.guiTop + 7, FONT_COLOR, false);

        context.drawText(this.textRenderer, HOST, this.guiLeft + 8, this.guiTop + 7 + (9 + 5), FONT_COLOR, false);
        context.drawText(this.textRenderer, PORT, this.guiLeft + 8, this.guiTop + 7 + (9 + 5) * 2 + 10 + 2, FONT_COLOR, false);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void resize(int width, int height) {
        String hostText = this.host.getText();
        String portText = this.port.getText();
        this.init(width, height);
        this.host.setText(hostText);
        this.port.setText(portText);
    }
}
