package tk.meowmc.portalgun.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tk.meowmc.portalgun.Portalgun;

import static qouteall.q_misc_util.api.McRemoteProcedureCall.tellServerToInvoke;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Final
    @Shadow
    private MinecraftClient client;

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void afterGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        Portalgun.logString(Level.INFO, "After Game Join!");
        tellServerToInvoke("tk.meowmc.portalgun.misc.RemoteCallables.resetWaits");
    }

}

