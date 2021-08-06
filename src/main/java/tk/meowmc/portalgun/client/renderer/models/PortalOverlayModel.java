package tk.meowmc.portalgun.client.renderer.models;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class PortalOverlayModel<T extends Entity> extends EntityModel<T> {
    public static int textureWidth;
    public static int textureHeight;
    private final ModelPart bone;

    public PortalOverlayModel(ModelPart modelPart) {
        textureWidth = 32;
        textureHeight = 64;
        bone = modelPart.getChild("base");
        bone.setPivot(0.0F, 8.0F, 0.5F);
    }

    public static TexturedModelData createModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("base", ModelPartBuilder.create()
                        .cuboid(-8.0F, -16.0F, 16.0F, 32.0F, 0.0F, 0.0F, true),
                ModelTransform.pivot(0, 0, 0)
        );

        return TexturedModelData.of(modelData, textureWidth, textureHeight);
    }

    @Override
    public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        //previously the render function, render code was moved to a method below
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {

        bone.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setRotationAngle(ModelPart bone, float x, float y, float z) {
        bone.pitch = x;
        bone.yaw = y;
        bone.roll = z;
    }
}