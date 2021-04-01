package tk.meowmc.portalgun.mixin;

import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.teleportation.CollisionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CollisionHelper.class)
public class CollisionHelperMixin {
    @Shadow(remap = false)
    public static boolean canCollideWithPortal(Entity entity, Portal portal, float tickDelta) {
        return false;
    }

    /**
     * @author someone
     * @reason because I can
     */
    @Overwrite(remap = false)
    public static Box getStretchedBoundingBox(Entity entity) {
        Vec3d expand;

        expand = entity.getVelocity().multiply(1.2);

        Vec3d expand2 = entity.getVelocity();
        return entity.getBoundingBox().stretch(expand);
    }
}
