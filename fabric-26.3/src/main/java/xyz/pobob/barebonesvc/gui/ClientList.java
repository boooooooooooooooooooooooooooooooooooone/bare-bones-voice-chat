package xyz.pobob.barebonesvc.gui;

import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class ClientList extends AbstractScrollArea {
    private static final int CELL_SIZE = 22;

    protected final List<ClientEntry> entries;

    public ClientList(int width, int height, int x, int y) {
        super(x, y, width, height, CommonComponents.EMPTY, AbstractScrollArea.defaultSettings(CELL_SIZE / 2));
        this.entries = new ArrayList<>();
        this.updateEntryList();
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        this.updateEntryList();

        context.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
        for (ClientEntry entry : this.entries) {
            if (entry.getY() + entry.getHeight() >= this.getY() && entry.getY() <= this.getBottom()) {
                entry.extractContent(context, mouseX, mouseY, false, deltaTicks);
            }
        }
        context.disableScissor();

        this.extractScrollbar(context, mouseX, mouseY);
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
        entry.setX(this.getX());
        entry.setWidth(this.getWidth());
        entry.setY(this.getYOfNextEntry());
        entry.setHeight(CELL_SIZE);
        this.entries.add(entry);
    }

    public int getYOfNextEntry() {
        int i = this.getY() - (int) this.scrollAmount();

        for (ClientEntry entry : this.entries) {
            i += entry.getHeight();
        }

        return i;
    }

    @Override
    protected int contentHeight() {
        int i = 0;

        for (ClientEntry entry : this.entries) {
            i += entry.getHeight();
        }

        return i + 4;
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
