package tk.meowmc.portalgun.client.renderer;

import software.bernie.geckolib3.renderer.geo.GeoItemRenderer;
import tk.meowmc.portalgun.client.renderer.models.PortalGunModel;
import tk.meowmc.portalgun.items.PortalGunItem;

public class PortalGunRenderer extends GeoItemRenderer<PortalGunItem> {
    public PortalGunRenderer() {
        super(new PortalGunModel());
    }
}
