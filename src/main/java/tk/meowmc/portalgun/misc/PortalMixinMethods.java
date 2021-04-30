package tk.meowmc.portalgun.misc;

import com.qouteall.immersive_portals.PehkuiInterface;
import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.teleportation.CollisionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.Vec3d;

public class PortalMixinMethods {
    public static void transformVelocity(Entity entity, Vec3d transformVecVelocity, Vec3d transformVecVelocityNoScale, boolean teleportChangesScale, Portal portal) {
        if (PehkuiInterface.isPehkuiPresent) {
            if (teleportChangesScale) {
                entity.setVelocity(transformVecVelocityNoScale);
            } else {
                entity.setVelocity(transformVecVelocity);
            }
        } else {
            entity.setVelocity(transformVecVelocity);
        }

        final double maxVelocity = 0.9;
        if (entity.getVelocity().length() > maxVelocity) {
            // cannot be too fast
            entity.setVelocity(entity.getVelocity().normalize().multiply(0.78));
        }

        // avoid cannot push minecart out of nether portal
        if (entity instanceof AbstractMinecartEntity && entity.getVelocity().lengthSquared() < 0.5) {
            entity.setVelocity(entity.getVelocity().multiply(2));
        }
        CollisionHelper.updateCollidingPortalNow(entity);
        CollisionHelper.updateClientGlobalPortalCollidingPortal();
    }
}
