package tk.meowmc.portalgun.client.renderer;

import tk.meowmc.portalgun.client.PortalgunClient;
import tk.meowmc.portalgun.client.renderer.models.PortalOverlayModel;
import tk.meowmc.portalgun.entities.CustomPortal;

import static net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;
import static tk.meowmc.portalgun.Portalgun.id;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CustomPortalEntityRenderer extends EntityRenderer<CustomPortal> {
    private final PortalOverlayModel model;
    
    public CustomPortalEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new PortalOverlayModel<>(context.bakeLayer(PortalgunClient.OVERLAY_MODEL_LAYER));
    }
    
    @Override
    public void render(CustomPortal entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        matrices.pushPose();
//        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(entity.yaw));
//        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(entity.pitch));
//        matrices.multiply(Vec3f.NEGATIVE_Z.getDegreesQuaternion(entity.getRoll()));
        
        
        int color = entity.colorInt * -1;
        
        int r = (color & 0xFF0000) >> 16;
        int g = (color & 0xFF00) >> 8;
        int b = color & 0xFF;
        
        this.model.renderToBuffer(matrices, vertexConsumers.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity))), light, NO_OVERLAY, r, g, b, 1F);
        matrices.popPose();
    }
    
    @Override
    public ResourceLocation getTextureLocation(CustomPortal entity) {
        return id("textures/entity/overlay.png");
    }
    
}
