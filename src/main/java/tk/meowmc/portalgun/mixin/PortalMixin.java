package tk.meowmc.portalgun.mixin;

import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.portal.PortalLike;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tk.meowmc.portalgun.misc.PortalMixinMethods;

@Mixin(Portal.class)
public abstract class PortalMixin extends Entity implements PortalLike {

    @Shadow
    public boolean teleportChangesScale;

    @Shadow
    public abstract Vec3d transformLocalVecNonScale(Vec3d localVec);

    public PortalMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /**
     * @author someone
     * @reason cuz
     */
    @Overwrite
    public void transformVelocity(Entity entity) {
        PortalMixinMethods.transformVelocity(entity, transformLocalVec(entity.getVelocity()), transformLocalVecNonScale(entity.getVelocity()), teleportChangesScale, (Portal) (Object) this);
    }

}
