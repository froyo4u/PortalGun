package tk.meowmc.portalgun.mixin;

import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.portal.PortalLike;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tk.meowmc.portalgun.ducks.IEPortal;

import static tk.meowmc.portalgun.items.PortalGunItem.newPortal1;
import static tk.meowmc.portalgun.items.PortalGunItem.newPortal2;

@Mixin(Portal.class)
public abstract class PortalMixin extends Entity implements PortalLike, IEPortal {

    public PortalMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public Boolean getActive(Portal portal) {
        return portal.getDataTracker().get(ISACTIVE);
    }

    @Override
    public void setActive(Boolean active, Portal portal) {
        portal.getDataTracker().set(ISACTIVE, active);
    }


    /**
     * @author me
     * @reason cuz
     */
    @Overwrite
    protected void initDataTracker() {
        this.getDataTracker().startTracking(ISACTIVE, false);
    }

    @Inject(method = "tick", at = @At(
            value = "INVOKE",
            target = "Lcom/qouteall/immersive_portals/my_util/SignalArged;emit(Ljava/lang/Object;)V"))
    public void tickMixin(CallbackInfo ci) {
        if (newPortal1 != null)
            setActive(false, newPortal1);
        if (newPortal2 != null)
            setActive(false, newPortal2);
    }

}
