package xyz.pobob.barebonesvc.client;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.intercompatibility.FabricClientCompatibilityManager;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.PlayerSoundPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.text.Text;
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
        return MinecraftClient.getInstance().getGameProfile().name();
    }

    @Override
    public UUID getOwnUUID() {
        return MinecraftClient.getInstance().getGameProfile().id();
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
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.of(message), overlay);
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
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> action.run());
    }

    @Override
    public Executor getIoWorkerExecutor() {
        return Util.getIoWorkerExecutor();
    }

    @Override
    public String getDigest(byte[] publicKey) throws NetworkEncryptionException {
        return new BigInteger(NetworkEncryptionUtils.computeServerId(
                "",
                NetworkEncryptionUtils.decodeEncodedRsaPublicKey(publicKey),
                NetworkEncryptionUtils.generateSecretKey()
        )).toString(16);
    }

    @Override
    public boolean requestSessionServerJoin(String digest) {
        ClientLoginNetworkHandler login = new ClientLoginNetworkHandler(null, MinecraftClient.getInstance(), null, null, false, null, component -> {}, null, null);

        Text text = ((ClientLoginNetworkHandlerInvoker) login).invokeJoinServerSession(digest);
        if (text != null) {
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(text, true);
            }
            return false;
        }

        return true;
    }
}