package com.blockdustry.client;

import com.blockdustry.building.ElevatorBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;

// 垂直提升机渲染：画带内物品（随 progress 沿 +y 从格底升到格顶）喵
public class ElevatorBlockEntityRenderer implements BlockEntityRenderer<ElevatorBlockEntity> {
    public ElevatorBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(ElevatorBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return;
        Item[] items = be.beltItems();
        float[] prog = be.beltProgress();
        for (int i = 0; i < ElevatorBlockEntity.SLOTS; i++) {
            if (items[i] == null) continue;
            // 直接使用服务端 progress：物品移动慢（0.046 格/tick），免去 partialTick 平滑可避免阻塞时来回抖动喵
            float p = Math.min(1f, prog[i]);
            // 像素转方块单位：pose.translate 用「格」(1/16) 而非像素，直接写 0.25+15.5 会把物品画到天上 15 格喵
            double y = (0.25 + p * 15.5) / 16.0; // 格底 0.0156 → 格顶 0.984（block 单位）喵
            pose.pushPose();
            pose.translate(0.5, y, 0.5);
            pose.mulPose(Axis.XP.rotationDegrees(-90.0F)); // 物品平躺：+Z 贴图法线转到 +Y 朝上喵
            pose.scale(0.5f, 0.5f, 0.5f); // 缩小 50%，与传送带物品一致喵
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    items[i].getDefaultInstance(), ItemDisplayContext.FIXED,
                    light, overlay, pose, buffer, be.getLevel(), 0);
            pose.popPose();
        }
    }
}
