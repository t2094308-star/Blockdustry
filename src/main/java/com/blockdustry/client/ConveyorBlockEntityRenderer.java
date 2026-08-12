package com.blockdustry.client;

import com.blockdustry.building.ConveyorBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;

// 传送带渲染：画带上物品（随 progress 沿朝向移动）喵
public class ConveyorBlockEntityRenderer implements BlockEntityRenderer<ConveyorBlockEntity> {
    public ConveyorBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(ConveyorBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return;
        Direction facing = be.getFacing();
        Item[] items = be.beltItems();
        float[] prog = be.beltProgress();
        for (int i = 0; i < ConveyorBlockEntity.SLOTS; i++) {
            if (items[i] == null) continue;
            // 直接使用服务端 progress：物品移动慢（0.046 格/tick），免去 partialTick 平滑可避免阻塞时来回抖动喵
            float p = Math.min(1f, prog[i]);
            double along = p - 0.5;
            pose.pushPose();
            pose.translate(0.5 + facing.getStepX() * along, 0.25, 0.5 + facing.getStepZ() * along);
            pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot())); // 物品正面朝传送方向喵
            pose.mulPose(Axis.XP.rotationDegrees(-90.0F)); // 物品平躺：+Z 贴图法线转到 +Y 朝上，不再竖立广告牌喵
            pose.scale(0.5f, 0.5f, 0.5f); // 缩小 50%（Mindustry 传送带物品比 1 格小），放平躺旋转之后喵
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    items[i].getDefaultInstance(), ItemDisplayContext.FIXED,
                    light, overlay, pose, buffer, be.getLevel(), 0);
            pose.popPose();
        }
    }
}
