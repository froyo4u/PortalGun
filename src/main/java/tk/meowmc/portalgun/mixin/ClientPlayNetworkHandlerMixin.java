package tk.meowmc.portalgun.mixin;

import com.qouteall.immersive_portals.McHelper;
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
import tk.meowmc.portalgun.misc.PortalPersistentState;

import java.util.UUID;

import static tk.meowmc.portalgun.items.PortalGunItem.KEY;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    private MinecraftClient client;


    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void afterGameJoin(GameJoinS2CPacket packet, CallbackInfo ci){
        Portalgun.log(Level.INFO, "After Game Join!");
        UUID uuid = client.player.getUuid();
        PortalPersistentState portalPersistentState = McHelper.getServerWorld(client.world.getRegistryKey()).getPersistentStateManager().getOrCreate(() -> new PortalPersistentState(KEY), KEY);
        portalPersistentState.portals = portalPersistentState.getPortals();
    }

}
