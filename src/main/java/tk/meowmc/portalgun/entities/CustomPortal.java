package tk.meowmc.portalgun.entities;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
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
import tk.meowmc.portalgun.PortalGunRecord;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.items.PortalGunItem;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class CustomPortal extends Portal {
    public static final EntityDataAccessor<String> outline = SynchedEntityData.defineId(CustomPortal.class, EntityDataSerializers.STRING);
    
    public static EntityType<CustomPortal> entityType = FabricEntityTypeBuilder.create(MobCategory.MISC, CustomPortal::new)
        .dimensions(EntityDimensions.scalable(0F, 0F))
        .build();
    
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
            // TODO break portal if necessary
        }
    }


}
