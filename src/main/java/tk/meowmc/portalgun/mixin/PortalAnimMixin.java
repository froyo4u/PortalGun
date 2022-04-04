package tk.meowmc.portalgun.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import qouteall.imm_ptl.core.portal.PortalAnimation;

@Mixin(PortalAnimation.class)
public abstract class PortalAnimMixin {
    @Final
    @Shadow
    public int durationTicks = 0;
}
