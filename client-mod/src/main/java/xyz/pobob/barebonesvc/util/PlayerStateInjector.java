package xyz.pobob.barebonesvc.util;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.group.GroupList;
import de.maxhenkel.voicechat.gui.group.JoinGroupList;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumeList;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import xyz.pobob.barebonesvc.gui.ClientList;
import xyz.pobob.barebonesvc.mixin.playerstate.PlayerStatesAccessor;

import java.util.UUID;

public class PlayerStateInjector {

    public static synchronized void updatePlayerState(UUID uuid, PlayerState state) {
        ((PlayerStatesAccessor) ClientManager.getPlayerStateManager()).getStates().put(uuid, state);
        VoicechatClient.USERNAME_CACHE.updateUsernameAndSave(state.getUuid(), state.getName());
        if (state.isDisconnected() && ClientManager.getClient() != null) ClientManager.getClient().closeAudioChannel(state.getUuid());
        AdjustVolumeList.update();
        ClientList.update();
        JoinGroupList.update();
        GroupList.update();
    }

    public static synchronized void clearStates() {
        ClientManager.getPlayerStateManager().clearStates();
    }

}
