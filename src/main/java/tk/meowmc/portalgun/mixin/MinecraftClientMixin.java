package tk.meowmc.portalgun.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tk.meowmc.portalgun.misc.AnimationMethods;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(
            method = {"handleBlockBreaking"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"
            )},
            cancellable = true
    )
    private void onHandleBlockBreaking(boolean bl, CallbackInfo ci) {
        AnimationMethods.doBlockBreaking(bl);
        ci.cancel();
    }

    @Inject(
            method = {"doAttack"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void onDoAttack(CallbackInfo ci) {
        AnimationMethods.doAttack();
        ci.cancel();
    }

    @Inject(
            method = {"doItemUse"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"
            )},
            cancellable = true
    )
    private void doItemUse(CallbackInfo ci) {
        AnimationMethods.doItemUse();
    }

}
