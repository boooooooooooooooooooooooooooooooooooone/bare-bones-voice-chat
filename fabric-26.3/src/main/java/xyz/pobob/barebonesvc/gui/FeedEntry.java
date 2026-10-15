package xyz.pobob.barebonesvc.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FeedEntry extends ContainerObjectSelectionList.Entry<@NotNull FeedEntry> {

    public final Component message;
    public final ZonedDateTime timestamp;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public FeedEntry(String message) {
        this.message = Component.literal("> " + message);
        this.timestamp = Instant.now().atZone(ZoneId.systemDefault());
    }

    @Override
    public void extractContent(@NotNull GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
        List<FormattedCharSequence> messageSplit = this.getWrapped();

        int textX = this.getContentX() + 2;
        int textY = this.getContentY();

        for (int i = 0; i < messageSplit.size(); i++) {
            context.text(Minecraft.getInstance().font, messageSplit.get(i), textX, textY + i * Minecraft.getInstance().font.lineHeight, 0xFFFFFFFF, false);
        }

        if (mouseX > this.getX() && mouseY > this.getY() - 1 && mouseX < this.getX() + this.getWidth() - 3 && mouseY < this.getY() + this.getHeight() + 3) {
            context.fill(this.getX(), this.getY() - 1, this.getX() + this.getWidth() - 3, this.getY() + this.getHeight() + 3, 0x20FFFFFF);
            context.setTooltipForNextFrame(Component.literal("Sent at " + FORMATTER.format(this.timestamp)), mouseX, mouseY);
        }
    }

    public List<FormattedCharSequence> getWrapped() {
        return Minecraft.getInstance().font.split(this.message, this.getWidth() - 12);
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
