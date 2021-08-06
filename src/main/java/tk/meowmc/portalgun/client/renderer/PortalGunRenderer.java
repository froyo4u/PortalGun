package tk.meowmc.portalgun.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import tk.meowmc.portalgun.client.renderer.models.PortalGunModel;
import tk.meowmc.portalgun.items.PortalGunItem;

public class PortalGunRenderer extends GeoItemRenderer<PortalGunItem> {
    public PortalGunRenderer() {
        super(new PortalGunModel());
    }
}
