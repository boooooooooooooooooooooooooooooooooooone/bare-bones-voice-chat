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

        int textX = this.getContentX();
        int textY = this.getContentY();
        for (int i = 0; i < messageSplit.size(); i++) {
            context.drawText(this.textRenderer, messageSplit.get(i), textX, textY + i * this.textRenderer.fontHeight, 0xFFFFFFFF, false);
        }

        if (mouseX > this.getX() && mouseX < this.getX() + this.getWidth() && mouseY > this.getY() && mouseY < this.getY() + this.getHeight()) {
            context.drawTooltip(this.textRenderer, Text.of("Sent at " + FORMATTER.format(this.timestamp)), mouseX, mouseY);
        }
    }

    public List<OrderedText> getWrapped() {
        return this.textRenderer.wrapLines(this.message, this.getWidth() - 10);
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
