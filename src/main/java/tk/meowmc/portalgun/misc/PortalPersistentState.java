package tk.meowmc.portalgun.misc;

import com.qouteall.immersive_portals.Helper;
import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.portal.Portal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class PortalPersistentState extends PersistentState {
    public static Map<String, Portal> portals = new HashMap<>();
    MinecraftClient client = MinecraftClient.getInstance();

    public PortalPersistentState(String key) {
        super(key);
    }

    public static Map<String, Portal> getPortals() {
        return portals;
    }

    private Map<String, Portal> getPortalsFromTag(CompoundTag tag, World currWorld) {
        ListTag listTag = tag.getList("portals", 10);
        Map<String, Portal> newData = new HashMap<>();

        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            Portal e = readPortalFromTag(currWorld, compoundTag);
            if (e != null) {
                newData.put(compoundTag.getString("key"), e);
            } else {
                Helper.err("error reading portal" + compoundTag);
            }
        }

        return newData;
    }

    private Portal readPortalFromTag(World currWorld, CompoundTag compoundTag) {
        Identifier entityId = new Identifier(compoundTag.getString("entity_type"));
        EntityType<?> entityType = (EntityType) Registry.ENTITY_TYPE.get(entityId);
        Entity e = entityType.create(currWorld);
        e.fromTag(compoundTag);
        return (Portal) e;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        ServerWorld currWorld = McHelper.getServerWorld(client.world.getRegistryKey());
        portals = getPortalsFromTag(tag, currWorld);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        ListTag portalsListTag = new ListTag();

        for (String key : portals.keySet()) {
            Portal portal = portals.get(key);
            CompoundTag portalTag = new CompoundTag();
            if (portal != null) {
                portal.toTag(portalTag);
                portalTag.putString("entity_type", EntityType.getId(portal.getType()).toString());
                portalTag.putString("key", key);
                portalsListTag.add(portalTag);
                tag.put("portals", portalsListTag);
            } else if (portals == null) {
                tag.putString("useruuid", client.player.getUuidAsString());
            }
        }
        return tag;
    }


}
