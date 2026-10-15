package xyz.pobob.barebonesvc.gui;

import com.google.common.collect.EvictingQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class SessionEventFeed extends AbstractScrollArea {
    private static final Queue<FeedEntry> FEED_HISTORY = EvictingQueue.create(50);

    public static void send(String message) {
        FEED_HISTORY.add(new FeedEntry(message));
    }

    public static void clear() {
        FEED_HISTORY.clear();
    }

    protected final List<FeedEntry> entries;
    protected int totalHeight;
    protected boolean firstRender = true;

    public SessionEventFeed(int width, int height, int x, int y) {
        super(x, y, width, height, CommonComponents.EMPTY, AbstractScrollArea.defaultSettings(15));
        this.entries = new ArrayList<>();
        this.updateEntryList();
    }

    @Override
    protected void extractWidgetRenderState(@NotNull GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        boolean wasAtBottom = Math.abs(this.scrollAmount() - this.maxScrollAmount()) < 1d;

        this.updateEntryList();

        if (wasAtBottom || this.firstRender) {
            this.firstRender = false;
            this.setScrollAmount(this.maxScrollAmount());
        }

        context.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
        for (FeedEntry entry : this.entries) {
            if (entry.getY() + entry.getHeight() >= this.getY() && entry.getY() <= this.getBottom()) {
                entry.extractContent(context, mouseX, mouseY, false, deltaTicks);
            }
        }
        context.disableScissor();

        this.extractScrollbar(context, mouseX, mouseY);
    }

    public void updateEntryList() {
        this.entries.clear();

        List<FeedEntry> list = new ArrayList<>(FEED_HISTORY);
        this.totalHeight = this.getTotalHeight();
        for (int i = list.size() - 1; i >= 0; i--) {
            this.addEntry(list.get(i));
        }
    }

    public void addEntry(FeedEntry entry) {
        this.entries.add(entry);
        entry.setX(this.getX());
        entry.setWidth(this.getWidth());
        entry.setY(this.getYOfThisEntry());
        entry.setHeight(Minecraft.getInstance().font.lineHeight * entry.getWrapped().size());
    }

    public int getTotalHeight() {
        int i = 0;

        for (FeedEntry entry : FEED_HISTORY) {
            i += entry.getHeight() + 4;
        }

        return i;
    }

    public int getYOfThisEntry() {
        int i = Math.max(this.totalHeight, this.getHeight()) + this.getY() - (int) this.scrollAmount();

        for (FeedEntry entry : this.entries) {
            i -= entry.getHeight() + 4;
        }

        return i - 1;
    }

    @Override
    protected int contentHeight() {
        return this.totalHeight + 3;
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent click, boolean doubled) {
        if (this.updateScrolling(click)) {
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
    }
}
