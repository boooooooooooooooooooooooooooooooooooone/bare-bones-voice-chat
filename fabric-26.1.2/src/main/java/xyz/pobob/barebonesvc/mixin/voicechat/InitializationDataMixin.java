package xyz.pobob.barebonesvc.mixin.voicechat;

import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.net.SecretPacket;
import de.maxhenkel.voicechat.voice.client.InitializationData;
import de.maxhenkel.voicechat.voice.common.Secret;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.pobob.barebonesvc.client.BareBonesVCClient;

import java.util.UUID;

@Mixin(InitializationData.class)
public class InitializationDataMixin {
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/net/SecretPacket;getVoiceHost()Ljava/lang/String;"
            )
    )
    private String voiceHost(SecretPacket instance) {
        return (instance == null) ? "" : instance.getVoiceHost();
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/net/SecretPacket;getServerPort()I"
            )
    )
    private int port(SecretPacket instance) {
        return (instance == null) ? BareBonesVCClient.INSTANCE.port : instance.getServerPort();
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/net/SecretPacket;getPlayerUUID()Ljava/util/UUID;"
            )
    )
    private UUID uuid(SecretPacket instance) {
        return (instance == null) ? Minecraft.getInstance().getGameProfile().id() : instance.getPlayerUUID();
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/net/SecretPacket;getSecret()Lde/maxhenkel/voicechat/voice/common/Secret;"
            )
    )
    private Secret secret(SecretPacket instance) {
        return (instance == null) ? null : instance.getSecret();
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/net/SecretPacket;getCodec()Lde/maxhenkel/voicechat/config/ServerConfig$Codec;"
            )
    )
    private ServerConfig.Codec codec(SecretPacket instance) {
        return (instance == null) ? switch (BareBonesVCClient.INSTANCE.config.getCodec()) {
            case VOIP -> ServerConfig.Codec.VOIP;
            case AUDIO -> ServerConfig.Codec.AUDIO;
            case RESTRICTED_LOWDELAY -> ServerConfig.Codec.RESTRICTED_LOWDELAY;
        } : instance.getCodec();
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/net/SecretPacket;getMtuSize()I"
            )
    )
    private int mtuSize(SecretPacket instance) {
        return (instance == null) ? 2048 : instance.getMtuSize();
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/net/SecretPacket;getVoiceChatDistance()D"
            )
    )
    private double voiceDistance(SecretPacket instance) {
        return (instance == null) ? 48d : instance.getVoiceChatDistance();
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/net/SecretPacket;getKeepAlive()I"
            )
    )
    private int keepAlive(SecretPacket instance) {
        return (instance == null) ? -1 : instance.getKeepAlive();
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/net/SecretPacket;groupsEnabled()Z"
            )
    )
    private boolean groupsEnabled(SecretPacket instance) {
        return (instance == null) ? false : instance.groupsEnabled();
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/net/SecretPacket;allowRecording()Z"
            )
    )
    private boolean allowRecording(SecretPacket instance) {
        return (instance == null) ? false : instance.allowRecording();
    }
}