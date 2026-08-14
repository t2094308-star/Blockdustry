package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.DiodeBlock;
import com.blockdustry.building.DiodeBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import org.joml.Matrix4f;

// Mindustry PowerDiode 箭头渲染喵。
// 忠实原版 PowerDiode.draw()：Draw.rect(region, x, y, 0) + Draw.rect(arrow, x, y, rotdeg())。
// 方块模型 = diode 底座（cube_all 用 diode.png），本 BER 在顶面画随朝向旋转的 diode-arrow 箭头喵。
// 已知小差距：物品栏预览只显示底座（与 drill 旋转钻头同惯例，箭头为 BER 叠加）喵
public class DiodeBlockEntityRenderer implements BlockEntityRenderer<DiodeBlockEntity> {
    private static final ResourceLocation ARROW_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/diode_arrow.png");

    public DiodeBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(DiodeBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!(be.getBlockState().getBlock() instanceof DiodeBlock db)) return;
        Direction facing = db.getFacing(be.getBlockState());
        // 顶面中央，y 略高于 1 防与底座顶面 z-fighting（坑/BER渲染.md）喵
        pose.pushPose();
        pose.translate(0.5, 1.002, 0.5);
        // 箭头纹理 up 默认指向 +Z（南）；facing.toYRot()：南=0/西=90/北=180/东=270 → 顺时旋转对齐朝向喵
        pose.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(ARROW_TEX));
        quad(vc, pose.last().pose());
        pose.popPose();
    }

    // 平躺 quad：纹理 up（PNG 顶部，V=0）朝向 +Z（南）；宽 0.9 略小于整格防渗色喵
    private static void quad(VertexConsumer vc, Matrix4f m) {
        float h = 0.45f;
        vertex(vc, m, -h, 0, -h, 0f, 1f);
        vertex(vc, m, h, 0, -h, 1f, 1f);
        vertex(vc, m, h, 0, h, 1f, 0f);
        vertex(vc, m, -h, 0, h, 0f, 0f);
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, float x, float y, float z, float u, float v) {
        vc.addVertex(m, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY) // 防 overlay 纹理越界采样染黑喵
                .setLight(0xF000F0) // 全亮，不随环境光照变暗喵
                .setNormal(0f, 1f, 0f); // 固定朝上，diffuse 光照恒定最亮喵
    }
}
