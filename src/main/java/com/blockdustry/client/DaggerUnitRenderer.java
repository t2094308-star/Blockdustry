package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.client.model.DaggerModel;
import com.blockdustry.entities.DaggerUnitEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

// Dagger 地面单位 3D 渲染：Blockbench 导出的 DaggerModel（双足机器人 + 炮管），
// 替代旧的平面 quad，行为不变喵
public class DaggerUnitRenderer extends EntityRenderer<DaggerUnitEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/entity/dagger.png");
    // Mindustry dagger 约 0.7 格，模型 1 格高（16 单位）需整体缩放喵
    private static final float SCALE = 0.7f;

    private final DaggerModel model;

    public DaggerUnitRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new DaggerModel(ctx.bakeLayer(DaggerModel.LAYER_LOCATION));
    }

    @Override
    public void render(DaggerUnitEntity entity, float yaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffer, int light) {
        // 整体缩放到 dagger 尺寸；模型脚底在 y=0，无需平移喵
        pose.scale(SCALE, SCALE, SCALE);
        // 模型前方为 +z（炮管/眼睛朝 +z），绕 y 轴对齐实体朝向（Minecraft yaw=0 朝南）喵
        float entityYaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        pose.mulPose(Axis.YP.rotationDegrees(-entityYaw));

        // 用实体行走动画状态驱动 setupAnim（walk/idle 动画）喵
        float limbSwing = entity.walkAnimation.position(partialTick);
        float limbSwingAmount = entity.walkAnimation.speed(partialTick);
        model.setupAnim(entity, limbSwing, limbSwingAmount, entity.tickCount + partialTick, 0.0F, 0.0F);

        // ⚠️ 沿用全亮 0xF000F0 + NO_OVERLAY：透传 light 白天 blockLight=0 时极暗，
        // 深色贴图 × 暗光 ≈ 纯黑（研究-炮管黑.md 踩过的坑，必须全亮）喵
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        model.renderToBuffer(pose, vc, 0xF000F0, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }

    @Override
    public ResourceLocation getTextureLocation(DaggerUnitEntity entity) {
        return TEXTURE;
    }
}
