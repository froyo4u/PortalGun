package tk.meowmc.portalgun.client.renderer.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

public class PortalOverlayModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart base;
    private final ModelPart cube_r1;

    public PortalOverlayModel(ModelPart root) {
        this.base = root.getChild("base");
        this.cube_r1 = base.getChild("cube_r1");
    }

    public static MeshDefinition getModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();

        PartDefinition base = modelPartData.addOrReplaceChild("base", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = base.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 16).addBox(-8.0F, -16.0F, 0.0F, 16.0F, 32.0F, 0.0F, new CubeDeformation(0.0F)).mirror(true), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 0.0F, 3.1416F, 0.0F));
        return modelData;
    }


    public static LayerDefinition getTexturedModelData() {
        return LayerDefinition.create(getModelData(), 32, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        //previously the render function, render code was moved to a method below
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        base.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setRotationAngle(ModelPart bone, float x, float y, float z) {
        bone.xRot = x;
        bone.yRot = y;
        bone.zRot = z;
    }
}