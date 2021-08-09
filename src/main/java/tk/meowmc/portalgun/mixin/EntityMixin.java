package tk.meowmc.portalgun.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.portal.Portal;
import tk.meowmc.portalgun.entities.CustomPortal;
import tk.meowmc.portalgun.misc.EntityAttatchments;
import tk.meowmc.portalgun.misc.VelocityTransfer;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityAttatchments, VelocityTransfer {

    @Shadow
    public World world;

    @Shadow
    public abstract double offsetX(double widthScale);

    @Shadow
    public abstract Direction getMovementDirection();

    @Shadow
    public abstract Box getBoundingBox();

    @Shadow
    public abstract Vec3d getVelocity();

    @Shadow
    public abstract void setVelocity(Vec3d velocity);

    @Shadow public abstract boolean isSneaking();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo ci) {
        Vec3d expand = this.getVelocity().multiply(20);
        Box streachedBB = this.getBoundingBox().stretch(expand);

        List<CustomPortal> globalPortals = this.world.getEntitiesByClass(CustomPortal.class, streachedBB, Portal::isTeleportable);

        for (CustomPortal globalPortal : globalPortals) {
            if (streachedBB.intersects(globalPortal.getBoundingBox())) {
                Vec3d portalFacing = new Vec3d((int) globalPortal.getNormal().getX(), (int) globalPortal.getNormal().getY(), (int) globalPortal.getNormal().getZ());
                double offsetX = 0;
                double offsetZ = 0;
                double offsetY = 0;

                Box streachedPortalBB = globalPortal.getBoundingBox().stretch(portalFacing.getX() * Math.abs(this.getVelocity().getX()) * 10, portalFacing.getY() * Math.abs(this.getVelocity().getY()) * 10, portalFacing.getZ() * Math.abs(this.getVelocity().getZ()) * 10);
                if (streachedPortalBB.intersects(this.getBoundingBox())) {
                    if (Math.abs(this.getVelocity().y) > Math.abs(this.getVelocity().x) || Math.abs(this.getVelocity().z) > Math.abs(this.getVelocity().x)) {
                        offsetX = (this.getBoundingBox().getCenter().x - globalPortal.getBoundingBox().getCenter().x) * .05;
                    }
                    if (Math.abs(this.getVelocity().y) > Math.abs(this.getVelocity().z) || Math.abs(this.getVelocity().x) > Math.abs(this.getVelocity().z)) {
                        offsetZ = (this.getBoundingBox().getCenter().z - globalPortal.getBoundingBox().getCenter().z) * .05;
                    }
                    if (Math.abs(this.getVelocity().z) > Math.abs(this.getVelocity().y) || Math.abs(this.getVelocity().x) > Math.abs(this.getVelocity().y)) {
                        offsetY = (this.getBoundingBox().getCenter().y - globalPortal.getBoundingBox().getCenter().y) * .05;
                    }
                    if (!this.getBoundingBox().intersects(globalPortal.getBoundingBox()) && !this.isSneaking())
                        this.setVelocity(this.getVelocity().add(-offsetX, -offsetY, -offsetZ));
                }
            }
        }
    }

}
