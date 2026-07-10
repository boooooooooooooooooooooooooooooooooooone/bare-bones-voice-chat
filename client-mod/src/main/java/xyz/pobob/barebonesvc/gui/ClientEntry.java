package xyz.pobob.barebonesvc.gui;

import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.BareBonesVC;

import java.awt.*;
import java.util.List;

public class ClientEntry extends ElementListWidget.Entry<ClientEntry> {
    private final MinecraftClient minecraft = MinecraftClient.getInstance();

    private final PlayerState state;

    private static final int SKIN_SIZE = 16;

    public ClientEntry(PlayerState state) {
        this.state = state;
    }

    public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
        int left = this.getContentX();
        int top = this.getContentY();
        int skinX = left + 4;
        int skinY = top + (this.getContentHeight() - SKIN_SIZE) / 2;

        SkinTextures skin = GameProfileUtils.getSkin(this.state.getUuid());
        context.drawTexture(RenderPipelines.GUI_TEXTURED, skin.body().texturePath(), skinX, skinY, 8.0F, 8.0F, SKIN_SIZE, SKIN_SIZE, 8, 8, 64, 64);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, skin.body().texturePath(), skinX, skinY, 40.0F, 8.0F, SKIN_SIZE, SKIN_SIZE, 8, 8, 64, 64);

        if (this.minecraft.textRenderer != null) {
            Double latency = BareBonesVC.LATENCIES.get(this.state.getUuid());
            if (latency != null) {
                this.renderLatency(
                        context,
                        Text.literal(String.format("%.1f", latency) + "ms")
                                .setStyle(Style.EMPTY.withColor(getLatencyColor(latency)))
                );
            }

            if (mouseX > this.getX() && mouseX < this.getX() + this.getWidth() && mouseY > this.getY() && mouseY < this.getY() + this.getHeight()) {
                context.drawTooltip(this.minecraft.textRenderer, Text.of(this.state.getName()), mouseX, mouseY);
            }
        }
    }

    private void renderLatency(DrawContext guiGraphics, Text text) {
        int textX = this.getContentX() + SKIN_SIZE + 4 + 4;
        int textY = this.getContentY() + (this.getContentHeight() - this.minecraft.textRenderer.fontHeight) / 2 + 2;
        guiGraphics.drawText(this.minecraft.textRenderer, text, textX, textY, 0xFFFFFFFF, false);
    }

    private static int getLatencyColor(double ping) {
        return Color.getHSBColor(
                (float) (11.1 * Math.exp(2.525 * Math.atan((311 - ping) / 83) - 1) - 5) / 360,
                0.92F,
                1.0F
        ).getRGB();
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return List.of();
    }

    @Override
    public List<? extends Element> children() {
        return List.of();
    }
}
