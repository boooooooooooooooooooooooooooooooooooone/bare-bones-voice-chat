package xyz.pobob.barebonesvc.mixin;

import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientLoginNetworkHandler.class)
public interface ClientLoginNetworkHandlerAccessor {
    @Invoker("joinServerSession")
    Text invokeJoinServerSession(String serverId);
}
