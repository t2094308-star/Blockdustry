package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.SorterBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;

// 分拣器渲染（Mindustry SorterBuild.draw）：设定物品未设 → 顶面中央画 cross 叉（拷原版 cross-full）；
// 已设定 → 顶面中央画该物品图标。全亮 + NO_OVERLAY 防暗/黑（坑/BER渲染.md）喵
public class SorterBlockEntityRenderer implements BlockEntityRenderer<SorterBlockEntity> {
    // Mindustry Sorter.cross（@-cross fallback cross-full）喵
    private static final ResourceLocation CROSS_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/cross_full.png");

    public SorterBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(SorterBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return;
        Item sortItem = be.getSortItem();
        if (sortItem == null) {
            drawCross(pose, buffer);
        } else {
            drawItem(be, sortItem, pose, buffer);
        }
    }

    // 顶面中央平铺 cross 叉（Mindustry sortItem==null 画 cross）喵
    private static void drawCross(PoseStack pose, MultiBufferSource buffer) {
        pose.pushPose();
        pose.translate(0.5, 0.505, 0.5);
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(CROSS_TEX));
        var matrix = pose.last().pose();
        float h = 0.25f; // 半宽 1/4 格，居中不遮四邻喵
        vc.addVertex(matrix, -h, 0f, -h).setUv(0f, 0f).setColor(255, 255, 255, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, -h, 0f, h).setUv(0f, 1f).setColor(255, 255, 255, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, h, 0f, h).setUv(1f, 1f).setColor(255, 255, 255, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, h, 0f, -h).setUv(1f, 0f).setColor(255, 255, 255, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        pose.popPose();
    }

    // 顶面中央画设定物品图标（Mindustry sortItem!=null 画物品色块；这里用 MC 物品精灵更直观）喵
    private static void drawItem(SorterBlockEntity be, Item item, PoseStack pose, MultiBufferSource buffer) {
        pose.pushPose();
        pose.translate(0.5, 0.51, 0.5);
        pose.mulPose(Axis.XP.rotationDegrees(-90.0F)); // 平躺：+Z 贴图法线转到 +Y 朝上喵
        pose.scale(0.4f, 0.4f, 0.4f);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                item.getDefaultInstance(), ItemDisplayContext.FIXED,
                0xF000F0, OverlayTexture.NO_OVERLAY, pose, buffer, be.getLevel(), 0);
        pose.popPose();
    }
}
