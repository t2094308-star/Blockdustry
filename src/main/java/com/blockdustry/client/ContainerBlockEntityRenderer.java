package com.blockdustry.client;

import com.blockdustry.storage.ContainerBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;

// 存储容器渲染（Mindustry StorageBuild.draw 的简化）：顶面中央画「主存储物品」图标（Jade 亦显示主类型库存）喵。
// 无物品时不额外画（方块模型已画本体）；全亮 + NO_OVERLAY 防暗/黑（坑/BER渲染.md）喵
public class ContainerBlockEntityRenderer implements BlockEntityRenderer<ContainerBlockEntity> {

    public ContainerBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    // 多格建筑 BER 剔除边界：从锚点默认 1×1×1 扩到覆盖整组 2×2，防余光/边缘整颗消失（坑/碰撞箱.md §3）喵
    @Override
    public AABB getRenderBoundingBox(ContainerBlockEntity be) {
        BlockPos anchor = be.hasAnchor() ? be.getAnchor() : be.getBlockPos();
        int size = be.getSize();
        return new AABB(anchor.getX(), anchor.getY(), anchor.getZ(),
                anchor.getX() + size, anchor.getY() + size, anchor.getZ() + size).inflate(2.0);
    }

    @Override
    public void render(ContainerBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return; // 只在锚点格画一次喵
        Item item = be.getStoredItem();
        if (item == null) return;
        pose.pushPose();
        // 2×2 顶面中心（锚点格本地坐标 1.5,1.5），略高于顶面防 z-fighting 喵
        pose.translate(1.5, 1.01, 1.5);
        pose.mulPose(Axis.XP.rotationDegrees(-90.0F)); // 平躺：+Z 贴图法线转到 +Y 朝上喵
        pose.scale(0.8f, 0.8f, 0.8f);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                item.getDefaultInstance(), ItemDisplayContext.FIXED,
                0xF000F0, OverlayTexture.NO_OVERLAY, pose, buffer, be.getLevel(), 0);
        pose.popPose();
    }
}
