package tk.meowmc.portalgun.client.renderer.models;

import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import tk.meowmc.portalgun.items.ClawItem;

import static tk.meowmc.portalgun.Portalgun.id;

public class ClawModel extends AnimatedGeoModel<ClawItem> {
    @Override
    public Identifier getModelResource(ClawItem object) {
        return id("geo/portalgun_claw.geo.json");
    }

    @Override
    public Identifier getTextureResource(ClawItem object) {
        return id("textures/item/portal_gun.png");
    }

    @Override
    public Identifier getAnimationResource(ClawItem animatable) {
        return null;
    }
}
