package tk.meowmc.portalgun.misc;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import tk.meowmc.portalgun.PortalGunMod;

public class RemoteCallables {
    public static void onClientLeftClickPortalGun(
        ServerPlayer player
    ) {
        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemInHand.getItem() == PortalGunMod.PORTAL_GUN) {
            PortalGunMod.PORTAL_GUN.onAttack(player, player.level, InteractionHand.MAIN_HAND);
        }
        else {
            PortalGunMod.LOGGER.error("Invalid left click packet");
        }
    }
}
