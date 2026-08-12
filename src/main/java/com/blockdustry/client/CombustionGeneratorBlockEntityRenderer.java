package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.power.CombustionGeneratorBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

// 燃烧发电机预热染色渲染（Mindustry DrawWarmupRegion）：顶面叠加 combustion-generator-top，
// 固定暖橙 #ff9b59，alpha 随 warmup 线性 + 正弦呼吸闪烁喵
public class CombustionGeneratorBlockEntityRenderer implements BlockEntityRenderer<CombustionGeneratorBlockEntity> {
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/combustion_generator_top.png");

    public CombustionGeneratorBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(CombustionGeneratorBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return;
        float warmup = be.getWarmup();
        if (warmup <= 0f) return;
        // Mindustry DrawWarmupRegion：alpha = warmup*(1-sinMag) + absin(time, 8, 0.6)*warmup，呼吸闪烁喵
        float absin = Math.abs((float) Math.sin(be.getLevel().getGameTime() * (Math.PI * 2f / 8f)));
        float alpha = warmup * 0.4f + absin * 0.6f * warmup;
        if (alpha < 0.01f) return;
        pose.pushPose();
        pose.translate(0.5f, 1.001f, 0.5f); // 抬到顶面之上防 z-fighting 喵
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEX));
        var matrix = pose.last().pose();
        // 暖橙 #ff9b59 染色（255,155,89），全亮不随环境光变暗（之前受光照太微弱），NO_OVERLAY 避免越界采样染黑喵
        vc.addVertex(matrix, -0.5f, 0f, -0.5f).setUv(0f, 0f).setColor(255, 155, 89, (int) (255 * alpha)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, -0.5f, 0f, 0.5f).setUv(0f, 1f).setColor(255, 155, 89, (int) (255 * alpha)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 0.5f, 0f, 0.5f).setUv(1f, 1f).setColor(255, 155, 89, (int) (255 * alpha)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 0.5f, 0f, -0.5f).setUv(1f, 0f).setColor(255, 155, 89, (int) (255 * alpha)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        pose.popPose();
    }
}
