package xyz.pobob.barebonesvc.gui;

import com.google.common.collect.Lists;
import de.maxhenkel.voicechat.gui.widgets.ListScreenListBase;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.MinecraftClient;

import java.util.Collection;
import java.util.List;


public class ClientList extends ListScreenListBase<ClientEntry> {
    protected ManagementScreen screen;
    protected final List<ClientEntry> entries;

    public ClientList(int width, int height, int top, int itemSize, ManagementScreen screen) {
        super(width, height, top, itemSize);
        this.screen = screen;
        this.entries = Lists.newArrayList();
        this.updateEntryList();
    }

    public static void update() {
        if (MinecraftClient.getInstance().currentScreen instanceof ManagementScreen managementScreen) {
            managementScreen.clientList.updateEntryList();
        }
    }

    public void updateEntryList() {
        Collection<PlayerState> onlinePlayers = ClientManager.getPlayerStateManager().getPlayerStates(true);
        this.entries.clear();

        for (PlayerState state : onlinePlayers) {
            if (!state.isDisconnected()) {
                this.entries.add(new ClientEntry(state));
            }
        }

        this.replaceEntries(this.entries);
    }
}
