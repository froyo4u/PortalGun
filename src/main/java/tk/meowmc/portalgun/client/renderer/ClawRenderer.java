package tk.meowmc.portalgun.client.renderer;

import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import tk.meowmc.portalgun.client.renderer.models.ClawModel;
import tk.meowmc.portalgun.items.ClawItem;

public class ClawRenderer extends GeoItemRenderer<ClawItem> {
    public ClawRenderer() {
        super(new ClawModel());
    }
}
