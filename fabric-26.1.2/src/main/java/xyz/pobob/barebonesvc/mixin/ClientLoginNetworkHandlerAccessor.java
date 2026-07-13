package xyz.pobob.barebonesvc.mixin;

import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientHandshakePacketListenerImpl.class)
public interface ClientLoginNetworkHandlerAccessor {
    @Invoker("authenticateServer")
    Component invokeAuthenticateServer(String serverId);
}
