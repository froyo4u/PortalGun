package tk.meowmc.portalgun.client.renderer;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import tk.meowmc.portalgun.items.ClawItem;

public class ClawRenderer extends GeoItemRenderer<ClawItem> {
    public ClawRenderer() {
        super(
            new DefaultedItemGeoModel<ClawItem>(new ResourceLocation("portalgun", "portalgun_claw"))
                .withAltTexture(new ResourceLocation("portalgun", "portalgun"))
        );
    }
}
