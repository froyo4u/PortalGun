package tk.meowmc.portalgun.client.renderer.models;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import tk.meowmc.portalgun.entities.PortalOverlay;

public class PortalOverlayModel extends EntityModel<PortalOverlay> {
    private final ModelPart bone;

    public PortalOverlayModel() {
        textureWidth = 32;
        textureHeight = 64;
        bone = new ModelPart(this);
        bone.setPivot(0.0F, 8.0F, 0.5F);


        ModelPart cube_r1 = new ModelPart(this);
        cube_r1.setPivot(0.0F, 0.0F, -0.5F);
        bone.addChild(cube_r1);
        setRotationAngle(cube_r1, 0.0F, 3.1416F, 0.0F);
        cube_r1.setTextureOffset(0, 16).addCuboid(-8.0F, -16.0F, 0.0F, 16.0F, 32.0F, 0.0F, 0.0F, true);
    }

    @Override
    public void setAngles(PortalOverlay entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
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