package tk.meowmc.portalgun.entities;

import com.qouteall.immersive_portals.PehkuiInterface;
import com.qouteall.immersive_portals.my_util.DQuaternion;
import com.qouteall.immersive_portals.my_util.RotationHelper;
import com.qouteall.immersive_portals.my_util.SignalArged;
import com.qouteall.immersive_portals.my_util.SignalBiArged;
import com.qouteall.immersive_portals.portal.Portal;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import tk.meowmc.portalgun.Portalgun;

import java.util.Objects;

import static tk.meowmc.portalgun.items.PortalGunItem.*;

public class CustomPortal extends Portal {
    public static final SignalArged<CustomPortal> clientPortalTickSignal = new SignalArged();
    public static final SignalArged<CustomPortal> serverPortalTickSignal = new SignalArged();
    public static final SignalArged<CustomPortal> portalCacheUpdateSignal = new SignalArged();
    public static final SignalArged<CustomPortal> portalDisposeSignal = new SignalArged();
    public static final SignalBiArged<CustomPortal, CompoundTag> readPortalDataSignal = new SignalBiArged();
    public static final SignalBiArged<CustomPortal, CompoundTag> writePortalDataSignal = new SignalBiArged();
    public static EntityType<CustomPortal> entityType;

    public CustomPortal(@NotNull EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void transformVelocity(@NotNull Entity entity) {
        if (PehkuiInterface.isPehkuiPresent) {
            if (this.teleportChangesScale) {
                entity.setVelocity(this.transformLocalVecNonScale(entity.getVelocity()));
            } else {
                entity.setVelocity(this.transformLocalVec(entity.getVelocity()));
            }
        } else {
            entity.setVelocity(this.transformLocalVec(entity.getVelocity()));
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
    }

    @Override
    public void tick() {
        super.tick();
        if (!world.isClient) {

            Vec3d axisHAbs = new Vec3d(Math.abs(axisH.x), Math.abs(axisH.y), Math.abs(axisH.z));

            @NotNull BlockPos portalUpperPos = new BlockPos(
                    this.getPos().getX() - this.axisW.crossProduct(axisHAbs).getX(),
                    this.getPos().getY() - this.axisW.crossProduct(this.axisH).getY(),
                    this.getPos().getZ() - this.axisW.crossProduct(this.axisH).getZ());
            @NotNull BlockPos portalLowerPos = new BlockPos(
                    this.getPos().getX() - this.axisW.crossProduct(axisHAbs).getX() - this.axisH.getX(),
                    this.getPos().getY() - this.axisW.crossProduct(this.axisH).getY() - this.axisH.getY(),
                    this.getPos().getZ() - this.axisW.crossProduct(this.axisH).getZ() - this.axisH.getZ());

            BlockState portalUpperBlockState = this.world.getBlockState(portalUpperPos);
            BlockState portalLowerBlockState = this.world.getBlockState(portalLowerPos);


            DQuaternion rotationInverse = DQuaternion.fromMcQuaternion(getRotation()).getConjugated();

            Direction portalDirection = Direction.fromRotation(RotationHelper.dotProduct4d(getRotation(), rotationInverse.toMcQuaternion()));

            if ((!portalUpperBlockState.isOpaque() || !portalLowerBlockState.isOpaque()) || (!world.getBlockState(getBlockPos()).isAir()) || world.getBlockState(new BlockPos(getPos().getX() - axisH.getX(), getPos().getY() - axisH.getY(), getPos().getZ() - axisH.getZ())).isOpaque()) {
                Portalgun.logString(Level.INFO, "Upper" + portalUpperPos.toString());
                Portalgun.logString(Level.INFO, "Lower" + portalLowerPos.toString());

                this.kill();
                world.playSound(null,
                        this.getPos().getX(),
                        this.getPos().getY(),
                        this.getPos().getZ(),
                        Portalgun.PORTAL_CLOSE_EVENT,
                        SoundCategory.NEUTRAL,
                        1.0F,
                        1F);
                if (Objects.equals(this.portalTag, "portalgun_portal1")) {
                    if (newPortal2 != null) {
                        newPortal2.setDestination(newPortal2.getPos());
                        newPortal2.reloadAndSyncToClient();
                    }
                    portal1Exists = false;
                } else if (Objects.equals(this.portalTag, "portalgun_portal2")) {
                    if (newPortal1 != null) {
                        newPortal1.setDestination(newPortal1.getPos());
                        newPortal1.reloadAndSyncToClient();
                    }
                    portal2Exists = false;
                }
            }
        }
    }
}
