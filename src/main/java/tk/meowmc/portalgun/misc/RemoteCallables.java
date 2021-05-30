package tk.meowmc.portalgun.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.util.GeckoLibUtil;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.items.PortalGunItem;

import static tk.meowmc.portalgun.Portalgun.PORTALGUN;
import static tk.meowmc.portalgun.items.PortalGunItem.*;

public class RemoteCallables {

    public static void removeOldPortals(ServerPlayerEntity user) {
        PortalGunItem gunItem = (PortalGunItem) Portalgun.PORTALGUN;

        if (gunItem.newPortal1 != null) {
            gunItem.newPortal1.kill();
            user.world.playSound(null,
                    gunItem.newPortal1.getX(),
                    gunItem.newPortal1.getY(),
                    gunItem.newPortal1.getZ(),
                    Portalgun.PORTAL_CLOSE_EVENT,
                    SoundCategory.NEUTRAL,
                    1.0F,
                    1F);
            gunItem.portalsTag.remove("PrimaryPortal" + user.getUuidAsString());
            gunItem.newPortal1 = null;
            portal1Exists = false;
        }
        if (gunItem.newPortal2 != null) {
            gunItem.newPortal2.kill();
            user.world.playSound(null,
                    gunItem.newPortal2.getX(),
                    gunItem.newPortal2.getY(),
                    gunItem.newPortal2.getZ(),
                    Portalgun.PORTAL_CLOSE_EVENT,
                    SoundCategory.NEUTRAL,
                    1.0F,
                    1F);
            gunItem.portalsTag.remove("SecondaryPortal" + user.getUuidAsString());
            gunItem.newPortal2 = null;
            portal2Exists = false;
        }

        if (gunItem.portalOutline1 != null) {
            gunItem.portalOutline1.kill();
            gunItem.portalsTag.remove("PrimaryOutline" + user.getUuidAsString());
            gunItem.portalOutline1 = null;
            outline1Exists = false;
        }
        if (gunItem.portalOutline2 != null) {
            gunItem.portalOutline2.kill();
            gunItem.portalsTag.remove("SecondaryOutline" + user.getUuidAsString());
            gunItem.portalOutline2 = null;
            outline2Exists = false;
        }
        resetWaits(user);
        gunItem.tag.remove(user.world.getRegistryKey().toString());
    }

    public static void portal1Place(ServerPlayerEntity user) {
        PortalGunItem gunItem = (PortalGunItem) PORTALGUN;
        boolean portalGunActive = user.isHolding(PORTALGUN);
        if (!user.getItemCooldownManager().isCoolingDown(gunItem) && portalGunActive) {
            gunItem.portal1Spawn(user.world, user, user.getActiveHand());
        }
    }

    @Environment(EnvType.CLIENT)
    public static void playAnim() {
        MinecraftClient client = MinecraftClient.getInstance();

        PortalGunItem gunItem = (PortalGunItem) PORTALGUN;
        boolean portalGunActive = client.player.isHolding(PORTALGUN);
        ItemStack itemStack = client.player.getStackInHand(Hand.MAIN_HAND);
        AnimationController animController = GeckoLibUtil.getControllerForStack(gunItem.factory, itemStack, controllerName);
        if (!client.player.getItemCooldownManager().isCoolingDown(gunItem) && portalGunActive) {
            animController.markNeedsReload();
            animController.setAnimation(new AnimationBuilder().addAnimation("portal_shoot", false));
        }
        client.attackCooldown = 0;
    }

    public static void resetWaits(ServerPlayerEntity user) {
        waitPortal = false;
    }

}
