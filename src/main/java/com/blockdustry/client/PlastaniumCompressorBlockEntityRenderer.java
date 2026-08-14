package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.PlastaniumCompressorBlockEntity;

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

// 塑钢压缩机 DrawFade 顶面叠层渲染（Mindustry drawer = DrawMulti(DrawDefault, DrawFade)）喵。
// -top 白色线稿贴图（plastanium-compressor-top.png，Mindustry 原版 64×64，白线稿）整张铺在 2×2 顶面，
// alpha = absin(totalProgress, 3, 0.6) * warmup —— 随 warmup 渐热、正弦呼吸闪烁（DrawFade 原版公式）喵。
// 时序说明：Mindustry totalProgress 每 tick +1（60tick/s），absin scale=3 即周期 3 时间单位；
// 本 mod 沿用现有约定（对照 CombustionGenerator DrawWarmupRegion 的 scale→MC tick 映射），
// 用 MC gameTime 直接作周期 3 MC tick，呈现与原版同类的快速脉动喵。
public class PlastaniumCompressorBlockEntityRenderer implements BlockEntityRenderer<PlastaniumCompressorBlockEntity> {
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/plastanium_compressor_top.png");

    public PlastaniumCompressorBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    // 多格建筑 BER 剔除边界：从锚点扩到覆盖整组 2×2，防余光/边缘整颗消失（坑/碰撞箱.md §3）喵
    @Override
    public AABB getRenderBoundingBox(PlastaniumCompressorBlockEntity be) {
        BlockPos anchor = be.hasAnchor() ? be.getAnchor() : be.getBlockPos();
        int size = be.getSize();
        return new AABB(anchor.getX(), anchor.getY(), anchor.getZ(),
                anchor.getX() + size, anchor.getY() + size, anchor.getZ() + size).inflate(2.0);
    }

    @Override
    public void render(PlastaniumCompressorBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return; // 只在锚点格画一次喵
        float warmup = be.getWarmup();
        if (warmup <= 0f) return;
        // DrawFade：alpha = abs(sin(totalProgress/3 * 2π)) * 0.6 * warmup；MC gameTime 作周期 3 喵
        float t = be.getLevel().getGameTime() + partialTick;
        float absin = Math.abs((float) Math.sin(t * (Math.PI * 2f / 3f)));
        float alpha = absin * 0.6f * warmup;
        if (alpha < 0.01f) return;
        pose.pushPose();
        // 2×2 顶面中心（锚点格本地 +1,+1），略高于顶面防 z-fighting 喵
        pose.translate(1.0f, 1.02f, 1.0f);
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEX));
        var matrix = pose.last().pose();
        // 2×2 平面 quad（-1..1），uv 覆盖整张 -top 图，法线朝上；白线稿不染色，仅透明度调制喵
        // 全亮 + NO_OVERLAY：透传 light 在白天极暗会把白色压没（坑/BER渲染.md）喵
        vc.addVertex(matrix, -1f, 0f, -1f).setUv(0f, 0f).setColor(255, 255, 255, (int) (255 * alpha)).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, -1f, 0f, 1f).setUv(0f, 1f).setColor(255, 255, 255, (int) (255 * alpha)).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 1f, 0f, 1f).setUv(1f, 1f).setColor(255, 255, 255, (int) (255 * alpha)).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 1f, 0f, -1f).setUv(1f, 0f).setColor(255, 255, 255, (int) (255 * alpha)).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        pose.popPose();
    }
}
