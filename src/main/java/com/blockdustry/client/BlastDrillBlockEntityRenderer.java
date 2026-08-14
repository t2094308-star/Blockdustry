package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.BlastDrillBlockEntity;

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

// 爆破钻头渲染（Mindustry Drill.draw() 移植）：4×4 单层建筑，锚点格 BER 画整组喵。
// 层次（原版顺序）：base(底) → rim(红色发光，additive 用 entityTranslucent 近似) → rotator(旋转钻头, 角度=timeDrilled*6)
//   → top(顶板) → mine item(当前矿物，染物品色)喵。
// ⚠️ 全亮 + NO_OVERLAY（坑-BER渲染 §1）：手绘 quad 透传 light 白天极暗会压黑；多 quad 叠画用 y 偏移防共面渗色（坑 §3）喵
public class BlastDrillBlockEntityRenderer implements BlockEntityRenderer<BlastDrillBlockEntity> {
    private static final ResourceLocation TEX_BASE =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/blast_drill.png");
    private static final ResourceLocation TEX_RIM =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/blast_drill_rim.png");
    private static final ResourceLocation TEX_ROTATOR =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/blast_drill_rotator.png");
    private static final ResourceLocation TEX_TOP =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/blast_drill_top.png");
    private static final ResourceLocation TEX_ITEM =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/blast_drill_item.png");
    private static final ResourceLocation TEX_WHITE =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/misc/white.png");

    // 原版 Drill.heatColor = #ff5512（rim 发光色）喵
    private static final int HEAT_R = 0xff, HEAT_G = 0x55, HEAT_B = 0x12;
    // 基座侧面底色（近似深灰金属，避免顶面贴图拉伸当侧面——坑-机器侧面贴图）喵
    private static final int SIDE_R = 0x3a, SIDE_G = 0x3a, SIDE_B = 0x3a;

    // 4×4 占地：以锚点为原点的 box 边长 4，顶面中心在 +2,+2 喵
    private static final float SIZE = 4f;
    private static final float HALF = 2f;

    public BlastDrillBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    // 视锥剔除边界：锚点格默认 1×1×1 包不住 4×4 视觉，扩到整组（坑-T13 核心余光剔除同款）喵
    @Override
    public AABB getRenderBoundingBox(BlastDrillBlockEntity be) {
        BlockPos anchor = be.hasAnchor() ? be.getAnchor() : be.getBlockPos();
        return new AABB(anchor.getX(), anchor.getY(), anchor.getZ(),
                anchor.getX() + 4, anchor.getY() + 1, anchor.getZ() + 4).inflate(1.0);
    }

    @Override
    public void render(BlastDrillBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return; // 只在锚点格画一次整组喵
        float gameTime = be.getLevel().getGameTime() + partialTick;
        float warmup = be.getWarmup();

        // —— 基座 box：顶面贴 blast_drill 底图，四侧面深灰（entityCutout 不透明）——
        drawBaseBox(pose, buffer);

        // —— 顶面叠层（中心在锚点 +2,+2），层叠 y 偏移防共面渗色（坑-BER渲染 §3）——
        pose.pushPose();
        pose.translate(HALF, 1.002f, HALF);

        // rim 红色发光：alpha = warmup * 0.6 * (0.7 + |sin(time*2π/3)| * 0.3)（原版 absin(Time.time,3,0.3)）喵
        float pulse = Math.abs((float) Math.sin(gameTime * (float) (Math.PI * 2.0 / 3.0))) * 0.3f;
        float rimAlpha = warmup * 0.6f * (0.7f + pulse);
        if (rimAlpha > 0.01f) {
            drawTintedQuad(pose, buffer, TEX_RIM, RenderType.entityTranslucent(TEX_RIM),
                    0.002f, HEAT_R, HEAT_G, HEAT_B, (int) (rimAlpha * 255f));
        }

        // rotator 旋转钻头：角度 = timeDrilled * rotateSpeed（6），随 warmup 累积转速喵
        pose.pushPose();
        pose.translate(0, 0.006f, 0);
        pose.mulPose(Axis.YP.rotationDegrees(be.getSpin() * be.getRotateSpeed()));
        drawPlainQuad(pose, buffer, TEX_ROTATOR, RenderType.entityCutout(TEX_ROTATOR), 0f);
        pose.popPose();

        // top 顶板喵
        drawPlainQuad(pose, buffer, TEX_TOP, RenderType.entityCutout(TEX_TOP), 0.008f);

        // mine item：染当前矿物色（原版 Draw.color(dominantItem.color)）喵
        if (be.getDominantItem() != null) {
            float[] c = BlastDrillBlockEntity.oreColor(be.getDominantItem());
            drawTintedQuad(pose, buffer, TEX_ITEM, RenderType.entityCutout(TEX_ITEM),
                    0.010f, (int) (c[0] * 255f), (int) (c[1] * 255f), (int) (c[2] * 255f), 255);
        }
        pose.popPose();
    }

    // 基座 box：顶面（y=1.0，底图）+ 四面（深灰）。box 本体 x0..4 y0..1 z0..4，在锚点格本地坐标直接画喵
    private void drawBaseBox(PoseStack pose, MultiBufferSource buffer) {
        VertexConsumer top = buffer.getBuffer(RenderType.entityCutout(TEX_BASE));
        var m = pose.last().pose();
        // 顶面 y=1：整张 blast_drill.png（0..4 × 0..4）喵
        quad(top, m, 0, 1, 0, 4, 1, 0, 4, 1, 4, 0, 1, 4,
                0, 0, 1, 0, 1, 1, 0, 1, 255, 255, 255, 255, 0, 1, 0);
        // 四面（白色纹理染深灰）
        VertexConsumer side = buffer.getBuffer(RenderType.entityCutout(TEX_WHITE));
        // 北 z=0
        quad(side, m, 0, 0, 0, 4, 0, 0, 4, 1, 0, 0, 1, 0,
                0, 1, 1, 1, 1, 0, 0, 0, SIDE_R, SIDE_G, SIDE_B, 255, 0, 0, -1);
        // 南 z=4
        quad(side, m, 0, 0, 4, 4, 0, 4, 4, 1, 4, 0, 1, 4,
                0, 1, 1, 1, 1, 0, 0, 0, SIDE_R, SIDE_G, SIDE_B, 255, 0, 0, 1);
        // 西 x=0
        quad(side, m, 0, 0, 4, 0, 0, 0, 0, 1, 0, 0, 1, 4,
                0, 1, 1, 1, 1, 0, 0, 0, SIDE_R, SIDE_G, SIDE_B, 255, -1, 0, 0);
        // 东 x=4
        quad(side, m, 4, 0, 0, 4, 0, 4, 4, 1, 4, 4, 1, 0,
                0, 1, 1, 1, 1, 0, 0, 0, SIDE_R, SIDE_G, SIDE_B, 255, 1, 0, 0);
    }

    // 普通不透明 quad（-2..2 绕当前 pose 中心）喵
    private void drawPlainQuad(PoseStack pose, MultiBufferSource buffer, ResourceLocation tex,
                               RenderType type, float yOffset) {
        drawTintedQuad(pose, buffer, tex, type, yOffset, 255, 255, 255, 255);
    }

    // 带色/alpha 的 quad（-2..2 绕当前 pose 中心）喵
    private void drawTintedQuad(PoseStack pose, MultiBufferSource buffer, ResourceLocation tex,
                                RenderType type, float yOffset, int r, int g, int b, int a) {
        VertexConsumer vc = buffer.getBuffer(type);
        var matrix = pose.last().pose();
        float y = yOffset;
        vc.addVertex(matrix, -HALF, y, -HALF).setUv(0f, 0f).setColor(r, g, b, a).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, -HALF, y, HALF).setUv(0f, 1f).setColor(r, g, b, a).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, HALF, y, HALF).setUv(1f, 1f).setColor(r, g, b, a).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, HALF, y, -HALF).setUv(1f, 0f).setColor(r, g, b, a).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
    }

    // 通用 4 顶点 quad：坐标 + 归一化 UV + 色 + 法线，全亮 NO_OVERLAY 喵
    private void quad(VertexConsumer vc, Matrix4f m,
                      float x0, float y0, float z0, float x1, float y1, float z1,
                      float x2, float y2, float z2, float x3, float y3, float z3,
                      float u0, float v0, float u1, float v1, float u2, float v2, float u3, float v3,
                      int r, int g, int b, int a, float nx, float ny, float nz) {
        vc.addVertex(m, x0, y0, z0).setUv(u0, v0).setColor(r, g, b, a).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(nx, ny, nz);
        vc.addVertex(m, x1, y1, z1).setUv(u1, v1).setColor(r, g, b, a).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(nx, ny, nz);
        vc.addVertex(m, x2, y2, z2).setUv(u2, v2).setColor(r, g, b, a).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(nx, ny, nz);
        vc.addVertex(m, x3, y3, z3).setUv(u3, v3).setColor(r, g, b, a).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(nx, ny, nz);
    }
}
