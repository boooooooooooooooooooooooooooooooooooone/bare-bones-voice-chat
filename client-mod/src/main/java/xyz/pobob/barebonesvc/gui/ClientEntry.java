package xyz.pobob.barebonesvc.gui;

import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.gui.widgets.ListScreenEntryBase;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import xyz.pobob.barebonesvc.BareBonesVCClient;

import java.awt.*;
import java.util.Objects;

public class ClientEntry extends ListScreenEntryBase<ClientEntry> {
    private final MinecraftClient minecraft = MinecraftClient.getInstance();

    private final PlayerState state;

    private static final int SKIN_SIZE = 16;
    private static final int X_OFFSET = 154;

    public ClientEntry(PlayerState state) {
        this.state = state;
    }

    public void render(DrawContext guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
        int left = this.getContentX() + X_OFFSET;
        int top = this.getContentY();
        int skinX = left + 2;
        int skinY = top + (this.getContentHeight() - SKIN_SIZE) / 2;
        Objects.requireNonNull(this.minecraft.textRenderer);
        this.renderElement(guiGraphics, skinX, skinY, mouseX, mouseY, hovered);
    }

    public void renderElement(DrawContext guiGraphics, int skinX, int skinY, int mouseX, int mouseY, boolean hovered) {
        SkinTextures skin = GameProfileUtils.getSkin(this.state.getUuid());
        guiGraphics.drawTexture(RenderPipelines.GUI_TEXTURED, skin.body().texturePath(), skinX, skinY, 8.0F, 8.0F, SKIN_SIZE, SKIN_SIZE, 8, 8, 64, 64);
        guiGraphics.drawTexture(RenderPipelines.GUI_TEXTURED, skin.body().texturePath(), skinX, skinY, 40.0F, 8.0F, SKIN_SIZE, SKIN_SIZE, 8, 8, 64, 64);

        Double latency = BareBonesVCClient.LATENCIES.get(this.state.getUuid());
        if (latency != null) {
            this.renderLatency(
                    guiGraphics,
                    Text.literal(String.format("%.1f", latency) + "ms")
                            .setStyle(Style.EMPTY.withColor(getLatencyColor(latency)))
            );
        }
        if (hovered && mouseX > this.getContentX() + X_OFFSET - 2) {
            guiGraphics.drawTooltip(this.minecraft.textRenderer, Text.of(this.state.getName()), mouseX, mouseY);
        }
    }

    private void renderLatency(DrawContext guiGraphics, Text text) {
        int textX = this.getContentX() + X_OFFSET + SKIN_SIZE + 4 + 2;
        int textY = this.getContentY() + (getContentHeight() - this.minecraft.textRenderer.fontHeight) / 2 + 2;
        guiGraphics.drawText(this.minecraft.textRenderer, text, textX, textY, 0xFFFFFFFF, false);
    }

    private static int getLatencyColor(double ping) {
        float hue = (float) (11.1*Math.exp(2.525*Math.atan((311-ping)/83)-1)-5) / 360;
        Color color = Color.getHSBColor((hue < 0.0F ? hue + 1 : hue), 0.92F, 1.0F);
        return color.getRGB();
    }
}
