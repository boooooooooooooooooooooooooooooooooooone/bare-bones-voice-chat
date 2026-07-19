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
import net.minecraft.util.Formatting;
import xyz.pobob.barebonesvc.voiceclient.BareBonesVCClient;

import java.awt.*;
import java.util.List;

public class ClientEntry extends ElementListWidget.Entry<ClientEntry> {
    private final MinecraftClient minecraft = MinecraftClient.getInstance();

    private final PlayerState state;

    private static final int SKIN_SIZE = 16;

    public ClientEntry(PlayerState state) {
        this.state = state;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
        if (this.minecraft.textRenderer != null) {
            if (mouseX > this.getX() && mouseY > this.getY() && mouseX < this.getX() + this.getWidth() - 3 && mouseY < this.getY() + this.getHeight()) {
                context.drawTooltip(this.minecraft.textRenderer, Text.of(this.state.getName()), mouseX, mouseY);
                context.fill(this.getX(), this.getY(), this.getX() + this.getWidth() - 3, this.getY() + this.getHeight(), 0x20FFFFFF);
            }

            Double latency = BareBonesVCClient.INSTANCE.latencies.get(this.state.getUuid());
            if (latency != null) {
                this.renderLatency(
                        context,
                        Text.literal(String.format("%.1f", latency) + "ms").setStyle(Style.EMPTY.withColor(getLatencyColor(latency)))
                );
            } else {
                this.renderLatency(
                        context,
                        Text.literal("N/A").setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
                );
            }
        }

        SkinTextures skin = GameProfileUtils.getSkin(this.state.getUuid());
        int skinX = this.getX() + 3;
        int skinY = this.getY() + 2 + (this.getContentHeight() - SKIN_SIZE) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, skin.body().texturePath(), skinX, skinY, 8.0F, 8.0F, SKIN_SIZE, SKIN_SIZE, 8, 8, 64, 64);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, skin.body().texturePath(), skinX, skinY, 40.0F, 8.0F, SKIN_SIZE, SKIN_SIZE, 8, 8, 64, 64);
    }

    private void renderLatency(DrawContext guiGraphics, Text text) {
        guiGraphics.drawText(
                this.minecraft.textRenderer,
                text,
                this.getX() + 3 + SKIN_SIZE + 4,
                this.getY() + 2 + (this.getContentHeight() - this.minecraft.textRenderer.fontHeight) / 2 + 2,
                0xFFFFFFFF,
                false
        );
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
