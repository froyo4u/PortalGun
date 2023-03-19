package tk.meowmc.portalgun.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.block_manipulation.BlockManipulationClient;
import qouteall.q_misc_util.api.McRemoteProcedureCall;
import tk.meowmc.portalgun.PortalGunMod;

@Mixin(BlockManipulationClient.class)
public class MixinImmPtlBlockManipulationClient {
    
    /**
     * TODO PR Fabric API to add the event, let ImmPtl fire the event and use the event
     */
    @Inject(
        method = "myAttackBlock",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void onMyAttackBlock(CallbackInfoReturnable<Boolean> cir) {
        ItemStack mainHandItem = Minecraft.getInstance().player.getMainHandItem();
        if (mainHandItem.getItem() == PortalGunMod.PORTAL_GUN) {
            McRemoteProcedureCall.tellServerToInvoke(
                "tk.meowmc.portalgun.misc.RemoteCallables.onClientLeftClickPortalGun"
            );
            // return true to disable block breaking logic in continueAttack()
            cir.setReturnValue(true);
        }
    }
}
