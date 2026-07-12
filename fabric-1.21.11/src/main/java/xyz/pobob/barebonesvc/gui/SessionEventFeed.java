package xyz.pobob.barebonesvc.gui;

import com.google.common.collect.EvictingQueue;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.screen.ScreenTexts;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class SessionEventFeed extends ScrollableWidget {
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
        super(x, y, width, height, ScreenTexts.EMPTY);
        this.entries = new ArrayList<>();
        this.updateEntryList();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        boolean wasAtBottom = Math.abs(this.getScrollY() - this.getMaxScrollY()) < 1d;

        this.updateEntryList();

        if (wasAtBottom || this.firstRender) {
            this.firstRender = false;
            this.setScrollY(this.getMaxScrollY());
        }

        context.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
        for (FeedEntry entry : this.entries) {
            if (entry.getY() + entry.getHeight() >= this.getY() && entry.getY() <= this.getBottom()) {
                entry.render(context, mouseX, mouseY, false, deltaTicks);
            }
        }
        context.disableScissor();

        this.drawScrollbar(context, mouseX, mouseY);
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
        entry.setHeight(entry.textRenderer.fontHeight * entry.getWrapped().size());
    }

    public int getTotalHeight() {
        int i = 0;

        for (FeedEntry entry : FEED_HISTORY) {
            i += entry.getHeight() + 4;
        }

        return i;
    }

    public int getYOfThisEntry() {
        int i = Math.max(this.totalHeight, this.getHeight()) + this.getY() - (int) this.getScrollY();

        for (FeedEntry entry : this.entries) {
            i -= entry.getHeight() + 4;
        }

        return i - 1;
    }

    @Override
    protected int getContentsHeightWithPadding() {
        return this.totalHeight + 3;
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 20d;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (this.checkScrollbarDragged(click)) {
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
