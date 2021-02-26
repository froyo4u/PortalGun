package tk.meowmc.portalgun.misc;

import com.qouteall.immersive_portals.McHelper;
import net.minecraft.item.ItemStack;
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
        if (newPortal1.isAlive()) {
            newPortal1.kill();
            World world = user.world;
            world.playSound(null,
                    newPortal1.getX(),
                    newPortal1.getY(),
                    newPortal1.getZ(),
                    Portalgun.PORTAL_CLOSE_EVENT,
                    SoundCategory.NEUTRAL,
                    1.0F,
                    1F);
        }
        persistentState.getPortals().remove(key);
        persistentState.markDirty();
    }

    public static void removeOldPortal2(ServerPlayerEntity user) {
        PortalPersistentState persistentState = McHelper.getServerWorld(user.world.getRegistryKey()).getPersistentStateManager().getOrCreate(() -> new PortalPersistentState(KEY), KEY);
        String key = user.getUuidAsString() + "-portalGunPortal1";
        if (newPortal2.isAlive()) {
            newPortal2.kill();
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

    public static void portal1Place(ServerPlayerEntity user) {
        PortalGunItem gunItem = (PortalGunItem) Portalgun.PORTALGUN;
        ItemStack itemStack = user.getStackInHand(user.getActiveHand());
        boolean portalGunActive = itemStack.getItem() == Portalgun.PORTALGUN;
            if (newPortal1 != null && portalGunActive) {
                if (user.handSwingTicks == -1 && newPortal1.age >= 2 && newPortal1.isAlive()) {
                    gunItem.portal1Spawn(user.world, user, user.getActiveHand());
                } else if (user.handSwingTicks == -1 && !newPortal1.isAlive()) {
                    gunItem.portal1Spawn(user.world, user, user.getActiveHand());
                    newPortal1.removed = false;
                }
            } else if (newPortal1 == null && portalGunActive) {
                gunItem.portal1Spawn(user.world, user, user.getActiveHand());
            }
    }

    public static void resetWaits(ServerPlayerEntity user)
    {
        waitPortal1 = false;
        waitPortal2 = false;
    }

}
