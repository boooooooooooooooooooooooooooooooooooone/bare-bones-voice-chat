package xyz.pobob.barebonesvc.gui;

import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.screen.ScreenTexts;

import java.util.ArrayList;
import java.util.List;


public class ClientList extends ScrollableWidget {
    private static final int CELL_SIZE = 20;

    protected final List<ClientEntry> entries;

    public ClientList(int width, int height, int x, int y) {
        super(x, y, width, height, ScreenTexts.EMPTY);
        this.entries = new ArrayList<>();
        this.updateEntryList();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        this.updateEntryList();

        context.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
        for (ClientEntry entry : this.entries) {
            if (entry.getY() + entry.getHeight() >= this.getY() && entry.getY() <= this.getBottom()) {
                entry.render(context, mouseX, mouseY, false, deltaTicks);
            }
        }
        context.disableScissor();

        this.drawScrollbar(context, mouseX, mouseY);
    }

    public void updateEntryList() {
        this.entries.clear();

        for (PlayerState state : ClientManager.getPlayerStateManager().getPlayerStates(true)) {
            if (!state.isDisconnected()) {
                this.addEntry(new ClientEntry(state));
            }
        }
    }

    public void addEntry(ClientEntry entry) {
        entry.setX(this.getRowLeft() + 1);
        entry.setWidth(this.getRowWidth());
        entry.setY(this.getYOfNextEntry() + 1);
        entry.setHeight(CELL_SIZE);
        this.entries.add(entry);
    }

    public int getRowLeft() {
        return this.getX();
    }

    public int getRowWidth() {
        return this.getWidth();
    }

    public int getYOfNextEntry() {
        int i = this.getY() - (int) this.getScrollY();

        for (ClientEntry entry : this.entries) {
            i += entry.getHeight();
        }

        return i;
    }

    @Override
    protected int getContentsHeightWithPadding() {
        int i = 0;

        for (ClientEntry entry : this.entries) {
            i += entry.getHeight();
        }

        return i + 4;
    }

    @Override
    protected double getDeltaYPerScroll() {
        return CELL_SIZE * 0.5;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
