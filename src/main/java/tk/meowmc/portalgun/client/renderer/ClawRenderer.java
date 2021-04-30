package tk.meowmc.portalgun.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.renderer.geo.GeoItemRenderer;
import tk.meowmc.portalgun.client.renderer.models.ClawModel;
import tk.meowmc.portalgun.items.ClawItem;

public class ClawRenderer extends GeoItemRenderer<ClawItem> {
    public ClawRenderer() {
        super(new ClawModel());
    }

    @Override
    public void render(ItemStack itemStack, ModelTransformation.Mode mode, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (mode == ModelTransformation.Mode.GUI) {
            RenderSystem.pushMatrix();
            VertexConsumerProvider.Immediate irendertypebuffer$impl = MinecraftClient.getInstance().getBufferBuilders()
                    .getEntityVertexConsumers();
            DiffuseLighting.disableGuiDepthLighting();
            super.render(itemStack, mode, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
            irendertypebuffer$impl.draw();
            RenderSystem.enableDepthTest();
            DiffuseLighting.enableGuiDepthLighting();
            RenderSystem.popMatrix();
        } else
            super.render(itemStack, mode, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
    }
}
