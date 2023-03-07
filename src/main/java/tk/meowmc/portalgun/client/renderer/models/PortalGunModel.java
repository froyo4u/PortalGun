package tk.meowmc.portalgun.client.renderer.models;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.resources.ResourceLocation;
import tk.meowmc.portalgun.config.PortalGunConfig;
import tk.meowmc.portalgun.items.PortalGunItem;

import static tk.meowmc.portalgun.Portalgun.id;

public class PortalGunModel /*extends AnimatedGeoModel<PortalGunItem>*/ {
    PortalGunConfig config = AutoConfig.getConfigHolder(PortalGunConfig.class).getConfig();

//    @Override
//    public ResourceLocation getModelResource(PortalGunItem object) {
//        /*if (config.enabled.enableOldPortalGunModel)
//            return id("geo/portalgun_og.geo.json");
//        else*/
//        return id("geo/portalgun.geo.json");
//    }
//
//    @Override
//    public ResourceLocation getTextureResource(PortalGunItem object) {
//        /*if (config.enabled.enableOldPortalGunModel)
//            return id("textures/item/portal_gun_og.png");
//        else*/
//        return id("textures/item/portal_gun.png");
//    }
//
//    @Override
//    public ResourceLocation getAnimationResource(PortalGunItem animatable) {
//        /*if (config.enabled.enableOldPortalGunModel)
//            return id("animations/portalgun_og.animation.json");
//        else*/
//        return id("animations/portalgun.animation.json");
//    }
}
