package com.blockdustry.client;

import com.blockdustry.power.BatteryLargeBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

// 大型电池充电特效渲染（3×3 battery-large 版）：忠于原版 Battery.drawer =
// DrawDefault + DrawPower（mixcol 发光方片，empty→full 按 power.status lerp）+ DrawRegion("-top")。
// 原版 DrawPower：无 -power 贴图时 Fill.square 以 lerp(emptyLightColor,fullLightColor,status) 实心色方块垫底，
// 顶部盖板 battery-large-top.png 中央透明区（35%）露出变色发光核心——即「电量显示动画」喵
public class BatteryLargeBlockEntityRenderer implements BlockEntityRenderer<BatteryLargeBlockEntity> {
    // Mindustry Battery.emptyLightColor = f8c266（琥珀/空电）、fullLightColor = fb9567（橙/满电）喵
    private static final float EMPTY_R = 0xf8 / 255f, EMPTY_G = 0xc2 / 255f, EMPTY_B = 0x66 / 255f;
    private static final float FULL_R = 0xfb / 255f, FULL_G = 0x95 / 255f, FULL_B = 0x67 / 255f;
    // 纯白纹理配合 vertex 颜色染色成实心发光方片喵
    private static final ResourceLocation WHITE_TEX =
            ResourceLocation.fromNamespaceAndPath(com.blockdustry.Blockdustry.MODID, "textures/misc/white.png");
    // 顶部盖板（拷原版 battery-large-top.png）喵
    private static final ResourceLocation TOP_TEX =
            ResourceLocation.fromNamespaceAndPath(com.blockdustry.Blockdustry.MODID, "textures/block/battery_large_top.png");

    public BatteryLargeBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public AABB getRenderBoundingBox(BatteryLargeBlockEntity be) {
        return new AABB(be.getBlockPos()).inflate(3.0); // 3×3 整组，防余光剔除喵
    }

    @Override
    public void render(BatteryLargeBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return; // 只锚点格画发光+顶盖（非锚点格是填充块）喵
        int size = be.getSize();
        BlockPos base = be.getBlockPos();
        float sat = Math.max(0f, Math.min(1f, be.getPowerStatus()));
        // lerp(empty, full, sat)：满电橙红、空电琥珀喵
        int r = (int) (lerp(EMPTY_R, FULL_R, sat) * 255f);
        int g = (int) (lerp(EMPTY_G, FULL_G, sat) * 255f);
        int b = (int) (lerp(EMPTY_B, FULL_B, sat) * 255f);

        // 发光方片：Mindustry Fill.square 半径 (tilesize*size/2 - 1) = 11 世界单位 = 1.375 格，居中略内缩喵
        float hw = 1.375f;
        Vec3 center = base.getCenter().add((size - 1) / 2.0, 0.52, (size - 1) / 2.0); // 整组中心，y=顶面+0.02 喵
        float cx = (float) center.x, cz = (float) center.z;
        float gy = (float) center.y;
        VertexConsumer glowVC = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
        Matrix4f m = pose.last().pose();
        quad(glowVC, m,
                cx - hw, gy, cz - hw,
                cx + hw, gy, cz - hw,
                cx + hw, gy, cz + hw,
                cx - hw, gy, cz + hw,
                r, g, b, 255, 0f, 0f, 1f, 1f);

        // 顶部盖板：battery-large-top.png 覆盖整组 3×3，中央透明区露出下方发光核心喵
        float ty = base.getY() + 1.04f;
        VertexConsumer topVC = buffer.getBuffer(RenderType.entityTranslucent(TOP_TEX));
        quad(topVC, m,
                base.getX(), ty, base.getZ(),
                base.getX() + size, ty, base.getZ(),
                base.getX() + size, ty, base.getZ() + size,
                base.getX(), ty, base.getZ() + size,
                255, 255, 255, 255, 0f, 0f, 1f, 1f);
    }

    // 水平面四边形（uv 0..1；glow 传白纹理+顶点色即实心色块，top 传纹理+白顶点即贴图）喵
    private static void quad(VertexConsumer vc, Matrix4f m,
                             float x0, float y, float z0,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             int r, int g, int bl, int a, float u0, float v0, float u1, float v1) {
        vertex(vc, m, x0, y, z0, u0, v0, r, g, bl, a);
        vertex(vc, m, x1, y1, z1, u1, v0, r, g, bl, a);
        vertex(vc, m, x2, y2, z2, u1, v1, r, g, bl, a);
        vertex(vc, m, x3, y3, z3, u0, v1, r, g, bl, a);
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, float x, float y, float z,
                               float u, float v, int r, int g, int bl, int a) {
        vc.addVertex(m, x, y, z)
                .setColor(r, g, bl, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY) // 防 overlay 越界采样染黑（坑/PowerNode激光黑色.md）喵
                .setLight(0xF000F0) // 全亮，不随环境光照变暗喵
                .setNormal(0f, 1f, 0f);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
