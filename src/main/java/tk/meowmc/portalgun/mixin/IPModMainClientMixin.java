package tk.meowmc.portalgun.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.IPModMainClient;

@Mixin(IPModMainClient.class)
public class IPModMainClientMixin {

    @Inject(
            method = "showOptiFineWarning",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void dontShowOptiFineWarning(CallbackInfo ci) {
        ci.cancel();
    }
}
