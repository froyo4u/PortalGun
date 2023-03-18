package tk.meowmc.portalgun.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.q_misc_util.api.McRemoteProcedureCall;
import tk.meowmc.portalgun.PortalGunMod;
import tk.meowmc.portalgun.items.PortalGunItem;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow
    @Nullable
    public HitResult hitResult;
    
    @Shadow
    @Nullable
    public LocalPlayer player;
    
    @Inject(
        method = "startAttack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"
        ),
        cancellable = true
    )
    private void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        if (hitResult == null || player == null) {
            return;
        }
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() == PortalGunMod.PORTAL_GUN) {
            McRemoteProcedureCall.tellServerToInvoke(
                "tk.meowmc.portalgun.misc.RemoteCallables.onClientLeftClickPortalGun"
            );
            // return true to disable block breaking logic in continueAttack()
            cir.setReturnValue(true);
        }
    }
}
