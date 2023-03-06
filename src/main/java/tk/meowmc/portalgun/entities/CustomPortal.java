package tk.meowmc.portalgun.entities;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.compat.PehkuiInterface;
import qouteall.imm_ptl.core.mc_utils.IPEntityEventListenableEntity;
import qouteall.imm_ptl.core.portal.*;
import qouteall.q_misc_util.Helper;
import qouteall.q_misc_util.dimension.DimId;
import qouteall.q_misc_util.my_util.SignalArged;
import qouteall.q_misc_util.my_util.SignalBiArged;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.items.PortalGunItem;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static tk.meowmc.portalgun.items.PortalGunItem.portal1Exists;
import static tk.meowmc.portalgun.items.PortalGunItem.portal2Exists;

public class CustomPortal extends Portal {
    public static final EntityDataAccessor<String> outline = SynchedEntityData.defineId(CustomPortal.class, EntityDataSerializers.STRING);
  
    public static EntityType<CustomPortal> entityType;
    
    public int colorInt;

    public CustomPortal(@NotNull EntityType<?> entityType, net.minecraft.world.level.Level world) {
        super(entityType, world);
    }
    
    @Environment(EnvType.CLIENT)
    private void acceptDataSync(Vec3 pos, CompoundTag customData) {
        setPos(pos);
        readAdditionalSaveData(customData);
    }

    @Environment(EnvType.CLIENT)
    private void startAnimationClient(PortalState animationStartState) {
    }

    @Override
    protected void defineSynchedData() {
        getEntityData().define(outline, "null");
    }

    public String getOutline() {
        return getEntityData().get(outline);
    }

    public void setOutline(String outline) {
        getEntityData().set(CustomPortal.outline, outline);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level.isClientSide) {

            BlockPos portalUpperPos = new BlockPos(
                    this.getX() - axisW.cross(axisH).x(),
                    this.getY() - axisW.cross(axisH).y(),
                    this.getZ() - axisW.cross(axisH).z());
            BlockPos portalLowerPos = new BlockPos(
                    this.getX() - axisW.cross(axisH).x() - Math.abs(axisH.x()),
                    this.getY() - axisW.cross(axisH).y() + axisH.y(),
                    this.getZ() - axisW.cross(axisH).z() - Math.abs(axisH.z()));

            BlockState portalUpperBlockState = this.level.getBlockState(portalUpperPos);
            BlockState portalLowerBlockState = this.level.getBlockState(portalLowerPos);


            Direction portalDirection = Direction.fromNormal((int) this.getNormal().x(), (int) this.getNormal().y(), (int) this.getNormal().z());

            if ((!level.getBlockState(portalUpperPos).isFaceSturdy(level, portalUpperPos, portalDirection)) ||
                    (!level.getBlockState(portalLowerPos).isFaceSturdy(level, portalLowerPos, portalDirection)
                    ) || (level.getBlockState(new BlockPos(this.position())).canOcclude()) || (level.getBlockState(new BlockPos(
                    this.getX() - Math.abs(axisH.x()),
                    this.getY() + axisH.y(),
                    this.getZ() - Math.abs(axisH.z()))).canOcclude())) {
//                Portalgun.logString(Level.INFO, "Upper" + portalUpperPos);
//                Portalgun.logString(Level.INFO, "Lower" + portalLowerPos);

                this.kill();
                level.playSound(null,
                        this.position().x(),
                        this.position().y(),
                        this.position().z(),
                        Portalgun.PORTAL_CLOSE_EVENT,
                        SoundSource.NEUTRAL,
                        1.0F,
                        1F);
//                if (!this.getOutline().equals("null")) {
//                    PortalOverlay portalOutline;
//                    portalOutline = (PortalOverlay) ((ServerWorld) world).getEntity(UUID.fromString(this.getOutline()));
//                    if (portalOutline != null)
//                        portalOutline.kill();
//                }
//                PortalGunItem gunItem = (PortalGunItem) Portalgun.PORTALGUN;
//                if (Objects.equals(this.portalTag, "portalgun_portal1")) {
//                    if (portal2Exists) {
//                        gunItem.newPortal2.setDestination(gunItem.newPortal2.getPos());
//                        PortalManipulation.adjustRotationToConnect(gunItem.newPortal2, gunItem.newPortal2);
//                        gunItem.newPortal2.reloadAndSyncToClient();
//                    }
//                    portal1Exists = false;
//                } else if (Objects.equals(this.portalTag, "portalgun_portal2")) {
//                    if (portal1Exists) {
//                        gunItem.newPortal1.setDestination(gunItem.newPortal1.getPos());
//                        PortalManipulation.adjustRotationToConnect(gunItem.newPortal1, gunItem.newPortal1);
//                        gunItem.newPortal1.reloadAndSyncToClient();
//                    }
//                    portal2Exists = false;
//                }
            }
        }
    }


}
