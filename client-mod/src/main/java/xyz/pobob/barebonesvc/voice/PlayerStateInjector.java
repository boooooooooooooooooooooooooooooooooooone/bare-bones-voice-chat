package xyz.pobob.barebonesvc.voice;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.group.GroupList;
import de.maxhenkel.voicechat.gui.group.JoinGroupList;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumeList;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import xyz.pobob.barebonesvc.mixin.PlayerStatesAccessor;

import java.util.UUID;

public class PlayerStateInjector {

    // copied from SVC's ClientPlayerStateManager
    public static void updatePlayerState(UUID uuid, PlayerState state) {
        ((PlayerStatesAccessor) ClientManager.getPlayerStateManager()).getPlayerStates().put(uuid, state);
        Voicechat.LOGGER.debug("Got state for {}: {}", state.getName(), state);
        VoicechatClient.USERNAME_CACHE.updateUsernameAndSave(state.getUuid(), state.getName());
        if (state.isDisconnected()) {
            ClientVoicechat c = ClientManager.getClient();
            if (c != null) {
                c.closeAudioChannel(state.getUuid());
            }
        }
        AdjustVolumeList.update();
        JoinGroupList.update();
        GroupList.update();
    }
}
