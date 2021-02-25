package tk.meowmc.portalgun.misc;

import com.qouteall.immersive_portals.McHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import tk.meowmc.portalgun.items.PortalGunItem;

import static tk.meowmc.portalgun.items.PortalGunItem.KEY;
import static tk.meowmc.portalgun.items.PortalGunItem.clientStatic;

public class RemoteCallables {

    public static void removeOldPortal1(ServerPlayerEntity user) {
        PortalPersistentState persistentState = McHelper.getServerWorld(user.world.getRegistryKey()).getPersistentStateManager().getOrCreate(() -> new PortalPersistentState(KEY), KEY);
        String key = user.getUuidAsString() + "-portalGunPortal0";
        PortalGunItem.newPortal1.kill();
        persistentState.getPortals().remove(key);
        persistentState.markDirty();
    }

    public static void removeOldPortal2(ServerPlayerEntity user) {
        PortalPersistentState persistentState = McHelper.getServerWorld(clientStatic.world.getRegistryKey()).getPersistentStateManager().getOrCreate(() -> new PortalPersistentState(KEY), KEY);
        String key = user.getUuidAsString() + "-portalGunPortal1";
        PortalGunItem.newPortal2.kill();
        persistentState.getPortals().remove(key);
        persistentState.markDirty();
    }

}
