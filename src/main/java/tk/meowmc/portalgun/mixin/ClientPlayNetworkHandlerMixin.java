package tk.meowmc.portalgun.mixin;

import com.qouteall.immersive_portals.McHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tk.meowmc.portalgun.Portalgun;

import static com.qouteall.immersive_portals.network.McRemoteProcedureCall.tellServerToInvoke;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Shadow
    private MinecraftClient client;

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void afterGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        Portalgun.logString(Level.INFO, "After Game Join!");
        McHelper.executeOnServerThread(() -> {
            tellServerToInvoke("tk.meowmc.portalgun.misc.RemoteCallables.resetWaits");
        });

    }

}

