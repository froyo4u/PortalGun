package tk.meowmc.portalgun.client.renderer;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import tk.meowmc.portalgun.client.renderer.models.PortalOverlayModel;
import tk.meowmc.portalgun.entities.PortalOverlay;

import static tk.meowmc.portalgun.Portalgun.id;

public class PortalOverlayRenderer extends EntityRenderer<PortalOverlay> {
    private final PortalOverlayModel model = new PortalOverlayModel();

    public PortalOverlayRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }


    @Override
    public void render(PortalOverlay entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        matrices.push();
        matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(entity.yaw));
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(entity.pitch));
        matrices.multiply(Vector3f.NEGATIVE_Z.getDegreesQuaternion(entity.getRoll()));


        int color = entity.getColorInt() * -1;

        int r = (color & 0xFF0000) >> 16;
        int g = (color & 0xFF00) >> 8;
        int b = color & 0xFF;

        this.model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(this.getTexture(entity))), light, OverlayTexture.DEFAULT_UV, r, g, b, 1F);
        matrices.pop();
    }

    @Override
    public Identifier getTexture(PortalOverlay entity) {
        return id("textures/entity/overlay.png");
    }
}
