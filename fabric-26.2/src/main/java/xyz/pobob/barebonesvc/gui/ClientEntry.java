package xyz.pobob.barebonesvc.gui;

import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.NotNull;
import xyz.pobob.barebonesvc.client.BareBonesVCClient;

import java.awt.*;
import java.util.List;

public class ClientEntry extends ContainerObjectSelectionList.Entry<@NotNull ClientEntry> {

    private final PlayerState state;

    private static final int SKIN_SIZE = 16;

    public ClientEntry(PlayerState state) {
        this.state = state;
    }

    @Override
    public void extractContent(@NotNull GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {

        PlayerSkin skin = GameProfileUtils.getSkin(this.state.getUuid());
        int skinX = this.getX() + 3;
        int skinY = this.getY() + 2 + (this.getContentHeight() - SKIN_SIZE) / 2;
        context.blit(RenderPipelines.GUI_TEXTURED, skin.body().texturePath(), skinX, skinY, 8.0F, 8.0F, SKIN_SIZE, SKIN_SIZE, 8, 8, 64, 64);
        context.blit(RenderPipelines.GUI_TEXTURED, skin.body().texturePath(), skinX, skinY, 40.0F, 8.0F, SKIN_SIZE, SKIN_SIZE, 8, 8, 64, 64);

        Double latency = BareBonesVCClient.INSTANCE.latencies.get(this.state.getUuid());
        if (latency != null) {
            this.renderLatency(
                    context,
                    Component.literal(String.format("%.1f", latency) + "ms").setStyle(Style.EMPTY.withColor(getLatencyColor(latency)))
            );
        } else {
            this.renderLatency(
                    context,
                    Component.literal("N/A").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))
            );
        }

        if (mouseX > this.getX() && mouseY > this.getY() && mouseX < this.getX() + this.getWidth() - 3 && mouseY < this.getY() + this.getHeight()) {
            context.fill(this.getX(), this.getY(), this.getX() + this.getWidth() - 3, this.getY() + this.getHeight(), 0x20FFFFFF);
            context.setTooltipForNextFrame(Component.literal(this.state.getName()), mouseX, mouseY);
        }
    }

    private void renderLatency(GuiGraphicsExtractor context, Component text) {
        context.text(
                Minecraft.getInstance().font,
                text,
                this.getX() + 3 + SKIN_SIZE + 4,
                this.getY() + 2 + (this.getContentHeight() - Minecraft.getInstance().font.lineHeight) / 2 + 2,
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
    public @NotNull List<? extends NarratableEntry> narratables() {
        return List.of();
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return List.of();
    }
}
