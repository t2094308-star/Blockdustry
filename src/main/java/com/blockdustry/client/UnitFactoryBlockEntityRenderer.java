package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.BlockdustryBuildingBlock;
import com.blockdustry.building.UnitFactoryBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

// 单位工厂预热染色渲染（Mindustry DrawWarmupRegion）：顶面按各自 corner 叠加 ground_factory.png 九宫格切片，
// 固定暖橙 #ff9b59，alpha 随 warmup 线性 + 正弦呼吸闪烁（参考 CombustionGeneratorBlockEntityRenderer）喵
public class UnitFactoryBlockEntityRenderer implements BlockEntityRenderer<UnitFactoryBlockEntity> {
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/ground_factory.png");

    public UnitFactoryBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(UnitFactoryBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        // warmup 只存在锚点格（非锚点格不 tick 也不同步），统一从锚点读，保证 3×3 整栋同步亮喵
        UnitFactoryBlockEntity src = be;
        if (!be.isAnchor() && be.hasAnchor() && be.getLevel() != null) {
            BlockEntity anchor = be.getLevel().getBlockEntity(be.getAnchor());
            if (anchor instanceof UnitFactoryBlockEntity a) src = a;
        }
        float warmup = src.getWarmup();
        if (warmup <= 0f || be.getLevel() == null) return;
        // Mindustry DrawWarmupRegion：alpha = warmup*(1-sinMag) + absin(time, 8, 0.6)*warmup，呼吸闪烁喵
        float absin = Math.abs((float) Math.sin(be.getLevel().getGameTime() * (Math.PI * 2f / 8f)));
        float alpha = warmup * 0.4f + absin * 0.6f * warmup;
        if (alpha < 0.01f) return;
        // 本格 corner → 九宫格 UV 切片（与方块模型顶面裁剪对齐）喵
        BlockdustryBuildingBlock.Corner corner = be.getBlockState().getValue(BlockdustryBuildingBlock.CORNER);
        float cell = 1f / 3f;
        float[] uv = uvFor(corner, cell);
        pose.pushPose();
        pose.translate(0.5f, 1.001f, 0.5f); // 抬到顶面之上防 z-fighting 喵
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEX));
        var matrix = pose.last().pose();
        // 暖橙 #ff9b59 染色（255,155,89），全亮不随环境光变暗，NO_OVERLAY 避免越界采样染黑喵
        vc.addVertex(matrix, -0.5f, 0f, -0.5f).setUv(uv[0], uv[1]).setColor(255, 155, 89, (int) (255 * alpha)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, -0.5f, 0f, 0.5f).setUv(uv[0], uv[3]).setColor(255, 155, 89, (int) (255 * alpha)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 0.5f, 0f, 0.5f).setUv(uv[2], uv[3]).setColor(255, 155, 89, (int) (255 * alpha)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 0.5f, 0f, -0.5f).setUv(uv[2], uv[1]).setColor(255, 155, 89, (int) (255 * alpha)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        pose.popPose();
    }

    // corner → 九宫格切片 [u0, v0, u1, v1]（与 3×3 模型裁剪一致的象限）喵
    private static float[] uvFor(BlockdustryBuildingBlock.Corner corner, float cell) {
        int col, row;
        switch (corner) {
            case N -> { col = 1; row = 0; }
            case NE -> { col = 2; row = 0; }
            case W -> { col = 0; row = 1; }
            case C -> { col = 1; row = 1; }
            case E -> { col = 2; row = 1; }
            case SW -> { col = 0; row = 2; }
            case S -> { col = 1; row = 2; }
            case SE -> { col = 2; row = 2; }
            default -> { col = 0; row = 0; } // NW
        }
        return new float[]{col * cell, row * cell, (col + 1) * cell, (row + 1) * cell};
    }
}
