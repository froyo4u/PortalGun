package tk.meowmc.portalgun.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.items.PortalGunItem;

import static tk.meowmc.portalgun.Portalgun.PORTALGUN;
import static tk.meowmc.portalgun.items.PortalGunItem.*;

public class RemoteCallables {

//    public static void removeOldPortals(ServerPlayer user) {
//        PortalGunItem gunItem = (PortalGunItem) Portalgun.PORTALGUN;
//
//        if (gunItem.newPortal1 != null) {
//            gunItem.newPortal1.kill();
//            user.level.playSound(null,
//                    gunItem.newPortal1.getX(),
//                    gunItem.newPortal1.getY(),
//                    gunItem.newPortal1.getZ(),
//                    Portalgun.PORTAL_CLOSE_EVENT,
//                    SoundSource.NEUTRAL,
//                    1.0F,
//                    1F);
//            gunItem.portalsTag.remove("PrimaryPortal" + user.getStringUUID());
//            gunItem.newPortal1 = null;
//            portal1Exists = false;
//        }
//        if (gunItem.newPortal2 != null) {
//            gunItem.newPortal2.kill();
//            user.level.playSound(null,
//                    gunItem.newPortal2.getX(),
//                    gunItem.newPortal2.getY(),
//                    gunItem.newPortal2.getZ(),
//                    Portalgun.PORTAL_CLOSE_EVENT,
//                    SoundSource.NEUTRAL,
//                    1.0F,
//                    1F);
//            gunItem.portalsTag.remove("SecondaryPortal" + user.getStringUUID());
//            gunItem.newPortal2 = null;
//            portal2Exists = false;
//        }
//
//        if (gunItem.portalOutline1 != null) {
//            gunItem.portalOutline1.kill();
//            gunItem.portalsTag.remove("PrimaryOutline" + user.getStringUUID());
//            gunItem.portalOutline1 = null;
//            outline1Exists = false;
//        }
//        if (gunItem.portalOutline2 != null) {
//            gunItem.portalOutline2.kill();
//            gunItem.portalsTag.remove("SecondaryOutline" + user.getStringUUID());
//            gunItem.portalOutline2 = null;
//            outline2Exists = false;
//        }
//        resetWaits(user);
//        gunItem.tag.remove(user.level.dimension().toString());
//    }
//
//    public static void portal1Place(ServerPlayer user) {
//        PortalGunItem gunItem = (PortalGunItem) PORTALGUN;
//        boolean portalGunActive = user.isHolding(PORTALGUN);
//        if (!user.getCooldowns().isOnCooldown(gunItem) && portalGunActive) {
//            gunItem.portal1Spawn(user.level, user, user.getUsedItemHand());
//        }
//    }

    @Environment(EnvType.CLIENT)
    public static void playAnim() {
        Minecraft client = Minecraft.getInstance();

        PortalGunItem gunItem = (PortalGunItem) PORTALGUN;
        boolean portalGunActive = client.player.isHolding(PORTALGUN);
        ItemStack itemStack = client.player.getItemInHand(InteractionHand.MAIN_HAND);
//        AnimationController animController = GeckoLibUtil.getControllerForStack(gunItem.animationFactory, itemStack, controllerName);
//        if (!client.player.getCooldowns().isOnCooldown(gunItem) && portalGunActive) {
//            animController.markNeedsReload();
//            animController.setAnimation(new AnimationBuilder().addAnimation("portal_shoot", false));
//        }
//        client.missTime = 0;
    }

//    public static void resetWaits(ServerPlayer user) {
//        waitPortal = false;
//    }

}
