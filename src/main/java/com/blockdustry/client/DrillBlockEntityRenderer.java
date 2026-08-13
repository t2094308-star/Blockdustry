package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.DrillBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

// 采集机旋转钻头渲染：锚点格上方叠加 Mindustry rotator 旋转动画喵
public class DrillBlockEntityRenderer implements BlockEntityRenderer<DrillBlockEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/drill_rotator.png");

    public DrillBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(DrillBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        // 只在锚点格渲染一次，避免四格各转一个喵
        if (!be.isAnchor()) return;
        float angle = (be.getLevel().getGameTime() + partialTick) * 0.3f;
        pose.pushPose();
        // 锚点格是 2×2 建筑左下角，中心在 +1,+1；贴顶面上方避免 z-fighting 喵
        pose.translate(1.0f, 1.02f, 1.0f);
        pose.mulPose(Axis.YP.rotation(angle));
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        var matrix = pose.last().pose();
        // 2×2 平面 quad（-1..1），uv 覆盖整张 rotator 图，法线朝上。
        // 全亮 + NO_OVERLAY：透传 light 在白天极暗会把深棕叶片压黑/消失（研究-炮管黑.md 同款坑，T6 修复）喵
        vc.addVertex(matrix, -1f, 0f, -1f).setUv(0f, 0f).setColor(255, 255, 255, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, -1f, 0f, 1f).setUv(0f, 1f).setColor(255, 255, 255, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 1f, 0f, 1f).setUv(1f, 1f).setColor(255, 255, 255, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 1f, 0f, -1f).setUv(1f, 0f).setColor(255, 255, 255, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        pose.popPose();
    }
}
