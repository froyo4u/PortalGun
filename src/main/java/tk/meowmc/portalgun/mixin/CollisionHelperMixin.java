package tk.meowmc.portalgun.mixin;

import com.qouteall.immersive_portals.teleportation.CollisionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CollisionHelper.class)
public class CollisionHelperMixin {
    /**
     * @author someone
     * @reason because I can
     */
    @Overwrite(remap = false)
    public static Box getStretchedBoundingBox(Entity entity) {
        Vec3d expand = entity.getVelocity().multiply(1.2);
        return entity.getBoundingBox().stretch(expand);
    }
}
