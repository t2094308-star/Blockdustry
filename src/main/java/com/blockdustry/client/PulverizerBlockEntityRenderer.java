package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.production.PulverizerBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

import org.joml.Matrix4f;

// 粉碎机动画渲染（Mindustry DrawMulti(DrawDefault, DrawRegion("-rotator"){spinSprite=true;rotateSpeed=2}, DrawRegion("-top"))）：
//   1) base 由方块模型渲染（blockstate：顶面 pulverizer.png 机器本体，侧面石料）喵
//   2) rotator（pulverizer-rotator 原版贴图）：绕 Y 旋转，速度受 warmup 驱动，停转时停（模板 PneumaticDrillBlockEntityRenderer）喵
//   3) top（pulverizer-top 原版贴图）：静态顶盖，画在 rotator 上方防共面渗色喵
// 转盘角速度忠实原版：Mindustry totalProgress += warmup（60 tick/s），DrawRegion rotateSpeed=2 → r_deg = totalProgress×2 = 120°/s（warmup=1）。
// MC 20 tick/s → 每 tick 弧度 = 120/20 × π/180 ≈ 0.1047 喵
public class PulverizerBlockEntityRenderer implements BlockEntityRenderer<PulverizerBlockEntity> {
    private static final ResourceLocation ROTATOR =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/pulverizer_rotator.png");
    private static final ResourceLocation TOP =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/pulverizer_top.png");
    // Mindustry pulverizer rotateSpeed = 2f；弧度/tick = 120°/s ÷ 20 tick/s × π/180 ≈ 0.1047 喵
    private static final float SPIN_RAD_PER_TICK = (float) (120.0 / 20.0 * Math.PI / 180.0);

    public PulverizerBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public AABB getRenderBoundingBox(PulverizerBlockEntity be) {
        BlockPos anchor = be.hasAnchor() ? be.getAnchor() : be.getBlockPos();
        int size = be.getSize();
        return new AABB(anchor.getX(), anchor.getY(), anchor.getZ(),
                anchor.getX() + size, anchor.getY() + size, anchor.getZ() + size).inflate(2.0);
    }

    @Override
    public void render(PulverizerBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return; // size 1 恒锚点，保留守卫防未来扩格喵
        float warmup = be.getWarmup();
        float angle = (be.getLevel().getGameTime() + partialTick) * warmup * SPIN_RAD_PER_TICK;
        pose.pushPose();
        // size 1 中心在 +0.5,+0.5；贴顶面上方避免 z-fighting 喵
        pose.translate(0.5f, 1.02f, 0.5f);

        // 1) rotator：整幅 1×1 平面，绕 Y 旋转（全亮 + NO_OVERLAY，坑-BER渲染.md）喵
        pose.pushPose();
        pose.mulPose(Axis.YP.rotation(angle));
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(ROTATOR));
        addQuad(vc, pose.last().pose(), 0.5f, 255, 255, 255);
        pose.popPose();

        // 2) top：静态整幅 1×1，略高于 rotator 防共面渗色喵
        pose.translate(0f, 0.004f, 0f);
        VertexConsumer vcTop = buffer.getBuffer(RenderType.entityCutout(TOP));
        addQuad(vcTop, pose.last().pose(), 0.5f, 255, 255, 255);

        pose.popPose();
    }

    // XZ 平面 quad（y=0），half 为半边长，uv 覆盖整张贴图，法线朝上。
    // 全亮 + NO_OVERLAY：透传 light 白天极暗会压黑深色像素（坑-BER渲染.md）喵
    private static void addQuad(VertexConsumer vc, Matrix4f matrix, float half, int r, int g, int b) {
        vc.addVertex(matrix, -half, 0f, -half).setUv(0f, 0f).setColor(r, g, b, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, -half, 0f, half).setUv(0f, 1f).setColor(r, g, b, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, half, 0f, half).setUv(1f, 1f).setColor(r, g, b, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, half, 0f, -half).setUv(1f, 0f).setColor(r, g, b, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
    }
}
