package xyz.pobob.barebonesvc.voiceclient;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.intercompatibility.FabricClientCompatibilityManager;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.PlayerSoundPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.pobob.barebonesvc.BareBonesVC;
import xyz.pobob.barebonesvc.gui.SessionEventFeed;
import xyz.pobob.barebonesvc.mixin.ClientLoginNetworkHandlerInvoker;
import xyz.pobob.barebonesvc.mixin.playerstate.ClientPlayerStateManagerInvoker;
import xyz.pobob.barebonesvc.mixin.voicechat.ClientManagerAccessor;

import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;

public class FabricBareBonesVCClient extends BareBonesVCClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BareBonesVC.MOD_ID);

    @Override
    public String getOwnUsername() {
        return Minecraft.getInstance().getGameProfile().name();
    }

    @Override
    public UUID getOwnUUID() {
        return Minecraft.getInstance().getGameProfile().id();
    }

    @Override
    public boolean getOwnDisabled() {
        return VoicechatClient.CLIENT_CONFIG.disabled.get();
    }

    @Override
    public void logInfo(String msg) {
        LOGGER.info(msg);
    }

    @Override
    public void logWarn(String msg) {
        LOGGER.warn(msg);
    }

    @Override
    public void logError(String msg, Throwable t) {
        LOGGER.error(msg, t);
    }

    @Override
    public void sendMessage(String message, boolean overlay) {
        if (Minecraft.getInstance().player != null) {
            if (overlay) {
                Minecraft.getInstance().player.sendOverlayMessage(Component.literal(message));
            } else {
                Minecraft.getInstance().player.sendSystemMessage(Component.literal(message));
            }
        }
    }

    @Override
    public void sendFeed(String message) {
        SessionEventFeed.send(message);
    }

    @Override
    public void clearFeed() {
        SessionEventFeed.clear();
    }

    @Override
    public void startVanillaSVC() {
        ((ClientManagerAccessor) ClientManager.instance()).invokeOnJoinWorld();
    }

    @Override
    public void startOurSVC() {
        if (VoicechatClient.CLIENT_CONFIG.muteOnJoin.get()) {
            ClientManager.getPlayerStateManager().setMuted(true);
        }

        ((ClientManagerAccessor) ClientManager.instance()).setClient(new ClientVoicechat());

        FabricClientCompatibilityManager.INSTANCE.emitVoiceChatConnectedEvent(ClientManager.getClient().getConnection());
    }

    @Override
    public void shutdownVoiceChat() {
        if (ClientManager.getClient() != null) {
            ClientManager.getClient().close();
            ((ClientManagerAccessor) ClientManager.instance()).setClient(null);
        }
    }

    @Override
    public void passSoundPacketToSVC(byte[] audio, long sequenceNumber, UUID uuid, boolean whispering) {
        Objects.requireNonNull(ClientManager.getClient()).processSoundPacket(new PlayerSoundPacket(
                uuid,
                uuid,
                audio,
                sequenceNumber,
                whispering,
                whispering ? this.config.getWhisperDistance() : this.config.getVoiceDistance(),
                null
        ));
    }

    @Override
    public synchronized void updatePlayerState(UUID uuid, String username, boolean disabled, boolean disconnected) {
        ((ClientPlayerStateManagerInvoker) ClientManager.getPlayerStateManager()).invokeUpdatePlayerState(
                null,
                new PlayerStatePacket(new PlayerState(uuid, username, disabled, disconnected))
        );
    }

    @Override
    public synchronized void clearPlayerStates() {
        ClientManager.getPlayerStateManager().clearStates();
    }

    @Override
    public void registerClientQuitEvent(Runnable action) {
        ClientLifecycleEvents.CLIENT_STOPPING.register(_ -> action.run());
    }

    @Override
    public Executor getIoWorkerExecutor() {
        return Util.ioPool();
    }

    @Override
    public String getDigest(byte[] publicKey) throws CryptException {
        return new BigInteger(Crypt.digestData(
                "",
                Crypt.byteToPublicKey(publicKey),
                Crypt.generateSecretKey()
        )).toString(16);
    }

    @Override
    public boolean requestSessionServerJoin(String digest) {
        ClientHandshakePacketListenerImpl login = new ClientHandshakePacketListenerImpl(null, Minecraft.getInstance(), null, null, false, null, _ -> {}, null, null);

        Component error = ((ClientLoginNetworkHandlerInvoker) login).invokeAuthenticateServer(digest);
        if (error != null) {
            this.sendMessage(error.getString(), true);
            return false;
        }

        return true;
    }
}