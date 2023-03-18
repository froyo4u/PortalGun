package tk.meowmc.portalgun.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tk.meowmc.portalgun.PortalGunMod;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    // disable swing
//    @Inject(
//        method = "swing(Lnet/minecraft/world/InteractionHand;Z)V",
//        at = @At("HEAD"),
//        cancellable = true
//    )
//    private void swing(InteractionHand hand, boolean updateSelf, CallbackInfo ci) {
//        Entity entity = (Entity) (Object) this;
//        if (entity instanceof Player player) {
//            ItemStack itemInHand = player.getItemInHand(hand);
//            if (itemInHand.getItem() == PortalGunMod.PORTAL_GUN) {
//                ci.cancel();
//            }
//        }
//    }
}
