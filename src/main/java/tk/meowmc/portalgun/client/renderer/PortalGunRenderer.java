package tk.meowmc.portalgun.client.renderer;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import tk.meowmc.portalgun.client.renderer.models.PortalGunModel;
import tk.meowmc.portalgun.items.PortalGunItem;

public class PortalGunRenderer extends GeoItemRenderer<PortalGunItem> {
    public PortalGunRenderer() {
        super(new DefaultedItemGeoModel<>(new ResourceLocation("portalgun", "portalgun")));
    }
}
