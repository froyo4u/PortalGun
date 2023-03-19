package tk.meowmc.portalgun.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

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
