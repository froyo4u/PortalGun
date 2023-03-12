package tk.meowmc.portalgun.client.renderer;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import tk.meowmc.portalgun.items.PortalGunItem;

public class PortalGunItemRenderer extends GeoItemRenderer<PortalGunItem> {
    public PortalGunItemRenderer() {
        super(new DefaultedItemGeoModel<>(new ResourceLocation("portalgun", "portalgun")));
    }
}
