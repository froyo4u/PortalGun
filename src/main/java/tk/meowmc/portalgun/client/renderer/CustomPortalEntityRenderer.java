package tk.meowmc.portalgun.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import tk.meowmc.portalgun.PortalGunMod;
import tk.meowmc.portalgun.client.PortalgunClient;
import tk.meowmc.portalgun.client.renderer.models.PortalOverlayModel;
import tk.meowmc.portalgun.entities.CustomPortal;

import static net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;
import static tk.meowmc.portalgun.PortalGunMod.id;

public class CustomPortalEntityRenderer extends EntityRenderer<CustomPortal> {
    private final PortalOverlayModel model;
    
    public CustomPortalEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new PortalOverlayModel(context.bakeLayer(PortalgunClient.OVERLAY_MODEL_LAYER));
    }
    
    @Override
    public void render(CustomPortal entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        matrices.pushPose();
        matrices.mulPose(entity.getOrientationRotation().toMcQuaternion());
        matrices.translate(0, 0, PortalGunMod.portalOverlayOffset);
        
        int color = entity.descriptor.side().getColorInt();
        
        int r = (color & 0xFF0000) >> 16;
        int g = (color & 0xFF00) >> 8;
        int b = color & 0xFF;
        
        this.model.renderToBuffer(
            matrices,
            vertexConsumers.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity))),
            light, NO_OVERLAY, r / 255.0f, g / 255.0f, b / 255.0f, 1.0f
        );
        matrices.popPose();
    }
    
    @Override
    public ResourceLocation getTextureLocation(CustomPortal entity) {
        return id("textures/entity/overlay.png");
    }
    
}
