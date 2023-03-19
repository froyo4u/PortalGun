package tk.meowmc.portalgun.entities;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.q_misc_util.my_util.IntBox;
import tk.meowmc.portalgun.PortalGunMod;
import tk.meowmc.portalgun.PortalGunRecord;

public class CustomPortal extends Portal {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static EntityType<CustomPortal> entityType = FabricEntityTypeBuilder.create(MobCategory.MISC, CustomPortal::new)
        .dimensions(EntityDimensions.scalable(0F, 0F))
        .build();
    
    public PortalGunRecord.PortalDescriptor descriptor;
    
    public IntBox wallBox;
    public IntBox airBox;
    
    public int thisSideUpdateCounter = 0;
    public int otherSideUpdateCounter = 0;
    
    public CustomPortal(@NotNull EntityType<?> entityType, net.minecraft.world.level.Level world) {
        super(entityType, world);
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        descriptor = PortalGunRecord.PortalDescriptor.fromTag(compoundTag.getCompound("descriptor"));
        wallBox = IntBox.fromTag(compoundTag.getCompound("wallBox"));
        airBox = IntBox.fromTag(compoundTag.getCompound("airBox"));
        thisSideUpdateCounter = compoundTag.getInt("thisSideUpdateCounter");
        otherSideUpdateCounter = compoundTag.getInt("otherSideUpdateCounter");
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.put("descriptor", descriptor.toTag());
        compoundTag.put("wallBox", wallBox.toTag());
        compoundTag.put("airBox", airBox.toTag());
        compoundTag.putInt("thisSideUpdateCounter", thisSideUpdateCounter);
        compoundTag.putInt("otherSideUpdateCounter", otherSideUpdateCounter);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (!level.isClientSide) {
            updateState();
        }
    }
    
    // disable the interpolation between last tick pos and this tick pos
    // because the portal should change abruptly
    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        setOldPosAndRot();
    }
    
    void updateState() {
        if (descriptor == null || wallBox == null) {
            LOGGER.error("Portal info abnormal {}", this);
            kill();
            return;
        }
        
        PortalGunRecord record = PortalGunRecord.get();
        PortalGunRecord.PortalInfo thisSideInfo = record.data.get(descriptor);
        PortalGunRecord.PortalInfo otherSideInfo = record.data.get(descriptor.getTheOtherSide());
        if (thisSideInfo == null) {
            // info is missing
            kill();
            return;
        }
        if (thisSideUpdateCounter != thisSideInfo.updateCounter() || !thisSideInfo.portalId().equals(getUUID())) {
            // replaced by new portal
            kill();
            return;
        }
        // check block status
        if (!PortalGunMod.isWallValid(level, wallBox) || !PortalGunMod.isAreaClear(level, airBox)) {
            kill();
            record.data.remove(descriptor);
            record.setDirty();
            level.playSound(
                null,
                getX(), getY(), getZ(),
                PortalGunMod.PORTAL_CLOSE_EVENT,
                SoundSource.PLAYERS,
                1.0F, 1.0F
            );
            return;
        }
        if (otherSideInfo == null) {
            // other side is missing, make this side inactive
            if (otherSideUpdateCounter != -1) {
                otherSideUpdateCounter = -1;
                teleportable = false;
                setIsVisible(false);
                setDestination(getOriginPos().add(0, 10, 0));
                reloadAndSyncToClient();
            }
            return;
        }
        if (otherSideInfo.updateCounter() != otherSideUpdateCounter) {
            // other side is replaced by new portal, update linking
            if (!isVisible()) {
                level.playSound(
                    null,
                    getX(), getY(), getZ(),
                    PortalGunMod.PORTAL_OPEN_EVENT,
                    SoundSource.PLAYERS,
                    1.0F, 1.0F
                );
            }
            otherSideUpdateCounter = otherSideInfo.updateCounter();
            teleportable = true;
            setIsVisible(true);
            setDestination(otherSideInfo.portalPos());
            setDestinationDimension(otherSideInfo.portalDim());
            setOtherSideOrientation(otherSideInfo.portalOrientation());
            reloadAndSyncToClient();
            return;
        }
    }
    
}
