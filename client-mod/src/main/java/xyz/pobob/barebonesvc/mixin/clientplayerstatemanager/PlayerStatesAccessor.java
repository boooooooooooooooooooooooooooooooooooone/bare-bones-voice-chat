package xyz.pobob.barebonesvc.mixin.clientplayerstatemanager;

import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@Mixin(ClientPlayerStateManager.class)
public interface PlayerStatesAccessor {
    @Accessor("states")
    Map<UUID, PlayerState> getPlayerStates();
}
