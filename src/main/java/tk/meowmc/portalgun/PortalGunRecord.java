package tk.meowmc.portalgun;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import qouteall.q_misc_util.Helper;
import qouteall.q_misc_util.MiscHelper;
import qouteall.q_misc_util.dimension.DimId;
import qouteall.q_misc_util.my_util.DQuaternion;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PortalGunRecord extends SavedData {
    public static enum PortalGunKind {
        _2x1
    }
    
    public static record SidedPortalInfo(
        UUID portalId,
        ResourceKey<Level> portalDim,
        Vec3 portalPos,
        DQuaternion portalOrientation
    ) {
        CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("portalId", portalId);
            tag.putString("portalDim", portalDim.location().toString());
            tag.putDouble("portalPosX", portalPos.x);
            tag.putDouble("portalPosY", portalPos.y);
            tag.putDouble("portalPosZ", portalPos.z);
            tag.put("portalOrientation", portalOrientation.toTag());
            return tag;
        }
        
        static SidedPortalInfo fromTag(CompoundTag tag) {
            return new SidedPortalInfo(
                tag.getUUID("portalId"),
                DimId.idToKey(new ResourceLocation(tag.getString("portalDim"))),
                new Vec3(
                    tag.getDouble("portalPosX"),
                    tag.getDouble("portalPosY"),
                    tag.getDouble("portalPosZ")
                ),
                DQuaternion.fromTag(tag.getCompound("portalOrientation"))
            );
        }
    }
    
    public static record PortalGunInfo(
        @Nullable SidedPortalInfo portal1,
        @Nullable SidedPortalInfo portal2
    ) {
        public static PortalGunInfo empty() {
            return new PortalGunInfo(null, null);
        }
        
        CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
    
            if (portal1 != null) {
                tag.put("portal1", portal1.toTag());
            }
            
            if (portal2 != null) {
                tag.put("portal2", portal2.toTag());
            }
            
            return tag;
        }
        
        static PortalGunInfo fromTag(CompoundTag tag) {
            return new PortalGunInfo(
                tag.contains("portal1") ? SidedPortalInfo.fromTag(tag.getCompound("portal1")) : null,
                tag.contains("portal2") ? SidedPortalInfo.fromTag(tag.getCompound("portal2")) : null
            );
        }
        
        public PortalGunInfo withPortal1(SidedPortalInfo portal1) {
            return new PortalGunInfo(portal1, portal2);
        }
        
        public PortalGunInfo withPortal2(SidedPortalInfo portal2) {
            return new PortalGunInfo(portal1, portal2);
        }
    }
    
    public final Map<UUID, Map<PortalGunKind, PortalGunInfo>> data;
    
    public PortalGunRecord(Map<UUID, Map<PortalGunKind, PortalGunInfo>> data) {this.data = data;}
    
    public static PortalGunRecord get() {
        ServerLevel overworld = MiscHelper.getServer().overworld();
        
        return overworld.getDataStorage().computeIfAbsent(
            PortalGunRecord::load,
            () -> {
                Helper.log("Portal gun record initialized ");
                return new PortalGunRecord(new HashMap<>());
            },
            "portal_gun_record"
        );
    }
    
    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        CompoundTag data = new CompoundTag();
        compoundTag.put("data", data);
        
        for (Map.Entry<UUID, Map<PortalGunKind, PortalGunInfo>> entry : this.data.entrySet()) {
            CompoundTag playerData = new CompoundTag();
            data.put(entry.getKey().toString(), playerData);
            
            for (Map.Entry<PortalGunKind, PortalGunInfo> portalGunEntry : entry.getValue().entrySet()) {
                playerData.put(portalGunEntry.getKey().toString(), portalGunEntry.getValue().toTag());
            }
        }
        return compoundTag;
    }
    
    public static PortalGunRecord load(CompoundTag compoundTag) {
        CompoundTag data = compoundTag.getCompound("data");
        
        return new PortalGunRecord(data.getAllKeys().stream().collect(Collectors.toMap(
            UUID::fromString,
            (key) -> {
                CompoundTag playerData = data.getCompound(key);
                
                return playerData.getAllKeys().stream().collect(Collectors.toMap(
                    PortalGunKind::valueOf,
                    (portalGunKey) -> {
                        CompoundTag portalGunData = playerData.getCompound(portalGunKey);
                        return PortalGunInfo.fromTag(portalGunData);
                    }
                ));
            }
        )));
    }
}
