package tk.meowmc.portalgun.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import qouteall.imm_ptl.core.teleportation.ServerTeleportationManager;

@Mixin(ServerTeleportationManager.class)
public abstract class ServerTeleportationManagerMixin {
    @ModifyArgs(method = "onPlayerTeleportedInClient", at = @At(
            value = "INVOKE",
            target = "Ljava/lang/String;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"),
            remap = false)
    private void mixin(Args args) {
        args.set(0, "%s may have Peek-A-Portal'd");
    }
}
