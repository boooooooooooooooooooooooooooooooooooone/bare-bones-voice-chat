package xyz.pobob.barebonesvc.gui;

import com.google.common.collect.Lists;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.screen.ScreenTexts;

import java.util.List;


public class ClientList extends ContainerWidget {
    protected final List<ClientEntry> entries;

    public ClientList(int width, int height, int x, int y) {
        super(x, y, width, height, ScreenTexts.EMPTY);
        this.entries = Lists.newArrayList();
        this.updateEntryList();
    }

    public static void update() {
        if (MinecraftClient.getInstance().currentScreen instanceof ManagementScreen managementScreen) {
            managementScreen.clientList.updateEntryList();
        }
    }

    public void updateEntryList() {
        List<PlayerState> onlinePlayers = ClientManager.getPlayerStateManager().getPlayerStates(true);
        this.entries.clear();

        for (PlayerState state : onlinePlayers) {
            if (!state.isDisconnected()) {
                this.addEntry(new ClientEntry(state));
            }
        }
    }

    public void addEntry(ClientEntry entry) {
        entry.setX(this.getRowLeft());
        entry.setWidth(this.getRowWidth());
        entry.setY(this.getYOfNextEntry());
        entry.setHeight(ManagementScreen.CELL_SIZE);
        this.entries.add(entry);
    }

    public int getRowLeft() {
        return this.getX() + this.width / 2 - this.getRowWidth() / 2;
    }

    public int getRowWidth() {
        return this.width;
    }

    public int getYOfNextEntry() {
        int i = 1 + this.getY() - (int) this.getScrollY();

        for (ClientEntry entry : this.entries) {
            i += entry.getHeight();
        }

        return i;
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

    @Override
    public List<? extends Element> children() {
        return List.of();
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
        return ManagementScreen.CELL_SIZE * 0.5;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
