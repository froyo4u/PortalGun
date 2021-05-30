package tk.meowmc.portalgun.entities;

import com.qouteall.immersive_portals.PehkuiInterface;
import com.qouteall.immersive_portals.my_util.SignalArged;
import com.qouteall.immersive_portals.my_util.SignalBiArged;
import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.portal.PortalManipulation;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.items.PortalGunItem;

import java.util.Objects;
import java.util.UUID;

import static tk.meowmc.portalgun.items.PortalGunItem.*;

public class CustomPortal extends Portal {
    public static final SignalArged<CustomPortal> clientPortalTickSignal = new SignalArged();
    public static final SignalArged<CustomPortal> serverPortalTickSignal = new SignalArged();
    public static final SignalArged<CustomPortal> portalCacheUpdateSignal = new SignalArged();
    public static final SignalArged<CustomPortal> portalDisposeSignal = new SignalArged();
    public static final SignalBiArged<CustomPortal, CompoundTag> readPortalDataSignal = new SignalBiArged();
    public static final SignalBiArged<CustomPortal, CompoundTag> writePortalDataSignal = new SignalBiArged();
    public static final TrackedData<String> outline = DataTracker.registerData(CustomPortal.class, TrackedDataHandlerRegistry.STRING);
    public static EntityType<CustomPortal> entityType;

    public CustomPortal(@NotNull EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        getDataTracker().startTracking(outline, "null");
    }

    public String getOutline() {
        return getDataTracker().get(outline);
    }

    public void setOutline(String outline) {
        getDataTracker().set(CustomPortal.outline, outline);
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

            BlockPos portalUpperPos = new BlockPos(
                    this.getX() - axisW.crossProduct(axisH).getX(),
                    this.getY() - axisW.crossProduct(axisH).getY(),
                    this.getZ() - axisW.crossProduct(axisH).getZ());
            BlockPos portalLowerPos = new BlockPos(
                    this.getX() - axisW.crossProduct(axisH).getX() - Math.abs(axisH.getX()),
                    this.getY() - axisW.crossProduct(axisH).getY() + axisH.getY(),
                    this.getZ() - axisW.crossProduct(axisH).getZ() - Math.abs(axisH.getZ()));

            BlockState portalUpperBlockState = this.world.getBlockState(portalUpperPos);
            BlockState portalLowerBlockState = this.world.getBlockState(portalLowerPos);


            Direction portalDirection = Direction.fromVector((int) this.getNormal().getX(), (int) this.getNormal().getY(), (int) this.getNormal().getZ());

            if ((!world.getBlockState(portalUpperPos).isSideSolidFullSquare(world, portalUpperPos, portalDirection)) ||
                    (!world.getBlockState(portalLowerPos).isSideSolidFullSquare(world, portalLowerPos, portalDirection)
                    ) || (!world.getBlockState(new BlockPos(this.getPos())).isAir()) || (!world.getBlockState(new BlockPos(
                    this.getX() - Math.abs(axisH.getX()),
                    this.getY() + axisH.getY(),
                    this.getZ() - Math.abs(axisH.getZ()))).isAir())) {
                Portalgun.logString(Level.INFO, "Upper" + portalUpperPos);
                Portalgun.logString(Level.INFO, "Lower" + portalLowerPos);

                this.kill();
                world.playSound(null,
                        this.getPos().getX(),
                        this.getPos().getY(),
                        this.getPos().getZ(),
                        Portalgun.PORTAL_CLOSE_EVENT,
                        SoundCategory.NEUTRAL,
                        1.0F,
                        1F);
                if (!this.getOutline().equals("null")) {
                    PortalOverlay portalOutline;
                    portalOutline = (PortalOverlay) ((ServerWorld) world).getEntity(UUID.fromString(this.getOutline()));
                    if (portalOutline != null)
                        portalOutline.kill();
                }
                PortalGunItem gunItem = (PortalGunItem) Portalgun.PORTALGUN;
                if (Objects.equals(this.portalTag, "portalgun_portal1")) {
                    if (portal2Exists) {
                        gunItem.newPortal2.setDestination(gunItem.newPortal2.getPos());
                        PortalManipulation.adjustRotationToConnect(gunItem.newPortal2, gunItem.newPortal2);
                        gunItem.newPortal2.reloadAndSyncToClient();
                    }
                    portal1Exists = false;
                } else if (Objects.equals(this.portalTag, "portalgun_portal2")) {
                    if (portal1Exists) {
                        gunItem.newPortal1.setDestination(gunItem.newPortal1.getPos());
                        PortalManipulation.adjustRotationToConnect(gunItem.newPortal1, gunItem.newPortal1);
                        gunItem.newPortal1.reloadAndSyncToClient();
                    }
                    portal2Exists = false;
                }
            }
        }
    }


}
