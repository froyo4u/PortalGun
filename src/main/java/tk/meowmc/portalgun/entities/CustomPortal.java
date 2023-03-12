package tk.meowmc.portalgun.entities;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.q_misc_util.my_util.IntBox;
import tk.meowmc.portalgun.PortalGunRecord;
import tk.meowmc.portalgun.items.PortalGunItem;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public class CustomPortal extends Portal {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static EntityType<CustomPortal> entityType = FabricEntityTypeBuilder.create(MobCategory.MISC, CustomPortal::new)
        .dimensions(EntityDimensions.scalable(0F, 0F))
        .build();
    
    public int colorInt;
    
    @Nullable
    public UUID ownerId;
    public PortalGunRecord.PortalGunKind kind;
    
    @Nullable
    public IntBox wallBox;
    
    public CustomPortal(@NotNull EntityType<?> entityType, net.minecraft.world.level.Level world) {
        super(entityType, world);
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        colorInt = compoundTag.getInt("colorInt");
        ownerId = compoundTag.hasUUID("ownerId") ? compoundTag.getUUID("ownerId") : null;
        kind = PortalGunRecord.PortalGunKind.fromString(compoundTag.getString("kind"));
        wallBox = IntBox.fromTag(compoundTag.getCompound("wallBox"));
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("colorInt", colorInt);
        if (ownerId != null) {
            compoundTag.putUUID("ownerId", ownerId);
        }
        compoundTag.putString("kind", kind.name());
        if (wallBox != null) {
            compoundTag.put("wallBox", wallBox.toTag());
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (!level.isClientSide) {
            updateState();
        }
    }
    
    void updateState() {
        if (ownerId == null || wallBox == null) {
            LOGGER.error("Portal without owner");
            kill();
            return;
        }
        
        PortalGunRecord portalGunRecord = PortalGunRecord.get();
        Map<PortalGunRecord.PortalGunKind, PortalGunRecord.PortalGunInfo> map1 = portalGunRecord.data.get(ownerId);
        if (map1 == null) {
            kill();
            return;
        }
        
        PortalGunRecord.PortalGunInfo info = map1.get(kind);
        if (info == null) {
            kill();
            return;
        }
        
        boolean wallIntact = wallBox.fastStream().allMatch(p -> PortalGunItem.isBlockSolid(level, p));
        if (!wallIntact) {
            map1.remove(kind);
            kill();
            return;
        }
        
        UUID currPortalUUID = getUUID();
        if (info.portal1() != null && info.portal2() != null) {
            if (!info.portal1().portalId().equals(currPortalUUID) &&
                !info.portal2().portalId().equals(currPortalUUID)
            ) {
                kill();
                return;
            }
            if (!isVisible() && info.portal1().portalId().equals(currPortalUUID)) {
                setIsVisible(true);
                teleportable = true;
                setDestinationDimension(info.portal2().portalDim());
                setDestination(info.portal2().portalPos());
                setOtherSideOrientation(info.portal2().portalOrientation());
                reloadAndSyncToClient();
            }
        }
    }
    
}
