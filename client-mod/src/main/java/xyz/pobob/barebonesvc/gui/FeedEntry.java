package xyz.pobob.barebonesvc.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class FeedEntry extends ElementListWidget.Entry<FeedEntry> {
    public final TextRenderer textRenderer = Objects.requireNonNull(MinecraftClient.getInstance().textRenderer);

    public final Text message;
    public final ZonedDateTime timestamp;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public FeedEntry(String message) {
        this.message = Text.of("> " + message);
        this.timestamp = Instant.now().atZone(ZoneId.systemDefault());
    }

    public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
        List<OrderedText> messageSplit = this.getWrapped();

        int textX = this.getContentX() + 2;
        int textY = this.getContentY();

        if (mouseX > this.getX() && mouseY > this.getY() - 1 && mouseX < this.getX() + this.getWidth() - 6 && mouseY < this.getY() + this.getHeight() + 3) {
            context.drawTooltip(this.textRenderer, Text.of("Sent at " + FORMATTER.format(this.timestamp)), mouseX, mouseY);
            context.fill(this.getX(), this.getY() - 1, this.getX() + this.getWidth() - 6, this.getY() + this.getHeight() + 3, 0x40FFFFFF);
        }

        for (int i = 0; i < messageSplit.size(); i++) {
            context.drawText(this.textRenderer, messageSplit.get(i), textX, textY + i * this.textRenderer.fontHeight, 0xFFFFFFFF, false);
        }
    }

    public List<OrderedText> getWrapped() {
        return this.textRenderer.wrapLines(this.message, this.getWidth() - 12);
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
