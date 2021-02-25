package tk.meowmc.portalgun.misc;

import com.qouteall.immersive_portals.McHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.World;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.items.PortalGunItem;

import static tk.meowmc.portalgun.items.PortalGunItem.*;

public class RemoteCallables {

    public static void removeOldPortal1(ServerPlayerEntity user) {
        PortalPersistentState persistentState = McHelper.getServerWorld(user.world.getRegistryKey()).getPersistentStateManager().getOrCreate(() -> new PortalPersistentState(KEY), KEY);
        String key = user.getUuidAsString() + "-portalGunPortal0";
        PortalGunItem.newPortal1.kill();
        World world = user.world;
        world.playSound(null,
                newPortal1.getX(),
                newPortal1.getY(),
                newPortal1.getZ(),
                Portalgun.PORTAL_CLOSE_EVENT,
                SoundCategory.NEUTRAL,
                1.0F,
                1F);
        persistentState.getPortals().remove(key);
        persistentState.markDirty();
    }

    public static void removeOldPortal2(ServerPlayerEntity user) {
        PortalPersistentState persistentState = McHelper.getServerWorld(clientStatic.world.getRegistryKey()).getPersistentStateManager().getOrCreate(() -> new PortalPersistentState(KEY), KEY);
        String key = user.getUuidAsString() + "-portalGunPortal1";
        PortalGunItem.newPortal2.kill();
        World world = user.world;
        world.playSound(null,
                newPortal2.getX(),
                newPortal2.getY(),
                newPortal2.getZ(),
                Portalgun.PORTAL_CLOSE_EVENT,
                SoundCategory.NEUTRAL,
                1.0F,
                1F);
        persistentState.getPortals().remove(key);
        persistentState.markDirty();
    }

}
