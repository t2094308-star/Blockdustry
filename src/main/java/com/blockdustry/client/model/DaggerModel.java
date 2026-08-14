package com.blockdustry.client.model;

import com.blockdustry.Blockdustry;
import com.blockdustry.entities.DaggerUnitEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

// Blockbench 5.1.6 modded_entity 导出的 dagger 3D 模型（Mindustry 双足机器人 + 炮管）喵
// 所有 cube 几何/UV/PartPose 与 Blockbench 导出 dagger_exported.java 完全一致，禁止手改坐标喵
public class DaggerModel extends EntityModel<DaggerUnitEntity> {
    // 模型层位置：在 BlockdustryClient.RegisterLayerDefinitions 里烘焙喵
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "dagger"), "main");

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart weapon;
    private final ModelPart leg_left;
    private final ModelPart leg_right;

    public DaggerModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.weapon = this.body.getChild("weapon");
        this.leg_left = root.getChild("leg_left");
        this.leg_right = root.getChild("leg_right");
    }

    // LayerDefinition 数据来源：Blockbench 导出（modded_entity → Java Class）喵
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.0F, -3.0F, 8.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(30, 0).addBox(-2.5F, -4.0F, -2.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
        .texOffs(52, 0).addBox(-1.0F, -3.0F, 2.4F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, 0.0F));

        PartDefinition weapon = body.addOrReplaceChild("weapon", CubeListBuilder.create().texOffs(0, 15).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -5.0F, 3.0F));

        PartDefinition leg_left = partdefinition.addOrReplaceChild("leg_left", CubeListBuilder.create().texOffs(20, 15).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
        .texOffs(0, 26).addBox(-1.5F, 4.0F, -3.0F, 3.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 19.0F, 0.0F));

        PartDefinition leg_right = partdefinition.addOrReplaceChild("leg_right", CubeListBuilder.create().texOffs(34, 15).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
        .texOffs(20, 26).addBox(-1.5F, 4.0F, -3.0F, 3.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 19.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(DaggerUnitEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // walk 动画（对应 Blockbench walk 关键帧：双腿 ±30° 相反相位、身体 ±2° 俯仰）喵
        float walk = Mth.cos(limbSwing * 0.6662F) * 0.5F * limbSwingAmount;
        this.leg_left.xRot = walk;
        this.leg_right.xRot = -walk;
        this.body.xRot = Mth.sin(limbSwing * 0.6662F) * 0.03F * limbSwingAmount;
        // idle 动画（对应 Blockbench idle 关键帧：头部绕 y ±10°、炮管绕 x ±5°）喵
        this.head.yRot = Mth.sin(ageInTicks * 0.1F) * 0.1F;
        this.weapon.xRot = Mth.sin(ageInTicks * 0.05F) * 0.08F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
                               int packedOverlay, int color) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leg_left.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leg_right.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
