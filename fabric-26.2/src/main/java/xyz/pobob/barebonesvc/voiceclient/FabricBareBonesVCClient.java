package xyz.pobob.barebonesvc.voiceclient;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.intercompatibility.FabricClientCompatibilityManager;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.voice.client.AudioChannel;
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
import xyz.pobob.barebonesvc.mixin.ClientLoginNetworkHandlerAccessor;
import xyz.pobob.barebonesvc.mixin.playerstate.ClientPlayerStateManagerAccessor;
import xyz.pobob.barebonesvc.mixin.voicechat.ClientManagerAccessor;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;

public class FabricBareBonesVCClient extends BareBonesVCClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BareBonesVC.MOD_ID);

    public ClientVoicechat client;

    public synchronized Map<UUID, AudioChannel> getAudioChannels() {
        return this.client.getAudioChannels();
    }

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
    public void shutdownVanillaSVC() {
        FabricClientCompatibilityManager.INSTANCE.emitDisconnectedEvent();
    }

    @Override
    public void startVanillaSVC() {
        ((ClientManagerAccessor) ClientManager.instance()).invokeOnJoinWorld();
    }

    @Override
    public void initializeOurSVC() {
        this.client = new ClientVoicechat();

        if (VoicechatClient.CLIENT_CONFIG.muteOnJoin.get()) {
            ClientManager.getPlayerStateManager().setMuted(true);
        }

        FabricClientCompatibilityManager.INSTANCE.emitVoiceChatConnectedEvent(null);
        this.client.onVoiceChatConnected(null);
    }

    @Override
    public boolean isOurSVCRunning() {
        return this.client != null;
    }

    @Override
    public void passSoundPacketToSVC(byte[] audio, long sequenceNumber, UUID uuid, boolean whispering) {

        if (this.isOurSVCRunning() && !VoicechatClient.CLIENT_CONFIG.disabled.get()) {
            AudioChannel channel = this.getAudioChannels().get(uuid);
            if (channel == null) {
                channel = new AudioChannel(
                        this.client,
                        null,
                        uuid
                );
                channel.start();
                this.getAudioChannels().put(uuid, channel);
            }
            channel.addToQueue(new PlayerSoundPacket(
                    uuid,
                    uuid,
                    audio,
                    sequenceNumber,
                    whispering,
                    whispering ? this.config.getWhisperDistance() : this.config.getVoiceDistance(),
                    null
            ));
        }
    }

    @Override
    public void shutdownOurSVC() {
        if (this.client != null) {
            this.client.closeMicThread();
            this.client.close();
            this.client = null;
        }
    }

    @Override
    public void updatePlayerState(UUID uuid, String username, boolean disabled, boolean disconnected) {
        ((ClientPlayerStateManagerAccessor) ClientManager.getPlayerStateManager()).invokeUpdatePlayerState(
                null,
                new PlayerStatePacket(new PlayerState(uuid, username, disabled, disconnected))
        );
    }

    @Override
    public void clearPlayerStates() {
        ClientManager.getPlayerStateManager().clearStates();
    }

    @Override
    public void pruneAudioChannels() {
        this.getAudioChannels().values().stream().filter(AudioChannel::canKill).forEach(AudioChannel::closeAndKill);
        this.getAudioChannels().entrySet().removeIf(entry -> entry.getValue().isClosed());
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

        Component error = ((ClientLoginNetworkHandlerAccessor) login).invokeAuthenticateServer(digest);
        if (error != null) {
            this.sendMessage(error.getString(), true);
            return false;
        }

        return true;
    }
}