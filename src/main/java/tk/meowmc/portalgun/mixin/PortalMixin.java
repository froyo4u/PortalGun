package tk.meowmc.portalgun.mixin;

import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.portal.PortalLike;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Portal.class)
public abstract class PortalMixin extends Entity implements PortalLike {

    public PortalMixin(EntityType<?> type, World world) {
        super(type, world);
    }

}
