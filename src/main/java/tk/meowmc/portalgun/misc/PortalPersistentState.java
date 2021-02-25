package tk.meowmc.portalgun.misc;

import com.qouteall.immersive_portals.Helper;
import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.portal.Portal;
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

public class PortalPersistentState extends PersistentState {

    public HashMap<String, Portal> portals = new HashMap<>();

    public PortalPersistentState(String key) {
        super(key);
    }

    @Override
    public void fromTag(CompoundTag tag) {
        ServerWorld currWorld = McHelper.getServerWorld(World.OVERWORLD);
        this.portals = getPortalsFromTag(tag, currWorld);
    }

    public HashMap<String, Portal> getPortals() {
        return portals;
    }


    private static HashMap<String, Portal> getPortalsFromTag(CompoundTag tag, World currWorld) {
        ListTag listTag = tag.getList("portals", 10);
        HashMap<String, Portal> newData = new HashMap<>();

        for(int i = 0; i < listTag.size(); ++i) {
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

    private static Portal readPortalFromTag(World currWorld, CompoundTag compoundTag) {
        Identifier entityId = new Identifier(compoundTag.getString("entity_type"));
        EntityType<?> entityType = (EntityType) Registry.ENTITY_TYPE.get(entityId);
        Entity e = entityType.create(currWorld);
        e.fromTag(compoundTag);
        return (Portal)e;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        ListTag portalsListTag = new ListTag();

        for(String key : portals.keySet()) {
            Portal portal = portals.get(key);
            CompoundTag portalTag = new CompoundTag();
            portal.toTag(portalTag);
            portalTag.putString("entity_type", EntityType.getId(portal.getType()).toString());
            portalTag.putString("key", key);
            portalsListTag.add(portalTag);
        }
        tag.put("portals", portalsListTag);

        return tag;
    }
}
