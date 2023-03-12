package tk.meowmc.portalgun;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import qouteall.q_misc_util.Helper;
import qouteall.q_misc_util.MiscHelper;
import qouteall.q_misc_util.dimension.DimId;
import qouteall.q_misc_util.my_util.DQuaternion;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PortalGunRecord extends SavedData {
    private static Logger LOGGER = LogManager.getLogger();
    
    public static enum PortalGunKind {
        _2x1;
        
        public static PortalGunKind fromString(String c) {
            return switch (c) {
                case "_2x1" -> _2x1;
                default -> _2x1;
            };
        }
        
        public double getWidth() {
            return switch (this) {
                case _2x1 -> 0.9;
            };
        }
        
        public double getHeight() {
            return switch (this) {
                case _2x1 -> 1.8;
            };
        }
    }
    
    public static enum PortalGunSide {
        blue, // left click
        orange; // right click
        
        public static PortalGunSide fromString(String c) {
            return switch (c) {
                case "orange" -> orange;
                case "blue" -> blue;
                default -> orange;
            };
        }
        
        public PortalGunSide getTheOtherSide() {
            return switch (this) {
                case orange -> blue;
                case blue -> orange;
            };
        }
        
        public int getColorInt() {
            return switch (this) {
                case orange -> 0xee7f1b;
                case blue -> 0x005ddf;
            };
        }
    }
    
    public static record PortalDescriptor(
        UUID playerId,
        PortalGunKind kind,
        PortalGunSide side
    ) {
        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("playerId", playerId);
            tag.putString("kind", kind.name());
            tag.putString("side", side.name());
            return tag;
        }
        
        public static PortalDescriptor fromTag(CompoundTag tag) {
            return new PortalDescriptor(
                tag.getUUID("playerId"),
                PortalGunKind.fromString(tag.getString("kind")),
                PortalGunSide.fromString(tag.getString("side"))
            );
        }
        
        public PortalDescriptor getTheOtherSide() {
            return new PortalDescriptor(playerId, kind, side.getTheOtherSide());
        }
    }
    
    public static record PortalInfo(
        UUID portalId,
        ResourceKey<Level> portalDim,
        Vec3 portalPos,
        DQuaternion portalOrientation,
        int updateCounter
    ) {
        CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("portalId", portalId);
            tag.putString("portalDim", portalDim.location().toString());
            tag.putDouble("portalPosX", portalPos.x);
            tag.putDouble("portalPosY", portalPos.y);
            tag.putDouble("portalPosZ", portalPos.z);
            tag.put("portalOrientation", portalOrientation.toTag());
            tag.putInt("updateCounter", updateCounter);
            return tag;
        }
        
        static PortalInfo fromTag(CompoundTag tag) {
            return new PortalInfo(
                tag.getUUID("portalId"),
                DimId.idToKey(new ResourceLocation(tag.getString("portalDim"))),
                new Vec3(
                    tag.getDouble("portalPosX"),
                    tag.getDouble("portalPosY"),
                    tag.getDouble("portalPosZ")
                ),
                DQuaternion.fromTag(tag.getCompound("portalOrientation")),
                tag.getInt("updateCounter")
            );
        }
    }
    
    public final Map<PortalDescriptor, PortalInfo> data;
    
    public PortalGunRecord(Map<PortalDescriptor, PortalInfo> data) {
        this.data = data;
    }
    
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
        ListTag dataTag = new ListTag();
        
        data.forEach((key, value) -> {
            CompoundTag entryTag = new CompoundTag();
            entryTag.put("key", key.toTag());
            entryTag.put("value", value.toTag());
            dataTag.add(entryTag);
        });
        
        compoundTag.put("data", dataTag);
        
        return compoundTag;
    }
    
    public static PortalGunRecord load(CompoundTag compoundTag) {
        ListTag dataTag = compoundTag.getList("data", 10);
        
        try {
            Map<PortalDescriptor, PortalInfo> data = dataTag.stream()
                .map(CompoundTag.class::cast)
                .collect(Collectors.toMap(
                    entryTag -> PortalDescriptor.fromTag(entryTag.getCompound("key")),
                    entryTag -> PortalInfo.fromTag(entryTag.getCompound("value"))
                ));
            
            return new PortalGunRecord(data);
        }
        catch (Exception e) {
            LOGGER.error("Failed to deserialize portal gun info", e);
            return new PortalGunRecord(new HashMap<>());
        }
    }
}
