package tk.meowmc.portalgun.client.renderer.models;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import tk.meowmc.portalgun.items.ClawItem;

import static tk.meowmc.portalgun.Portalgun.id;

public class ClawModel extends GeoModel<ClawItem> {
    @Override
    public ResourceLocation getModelResource(ClawItem object) {
        return id("geo/portalgun_claw.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ClawItem object) {
        return id("textures/item/portal_gun.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ClawItem animatable) {
        return null;
    }
}
