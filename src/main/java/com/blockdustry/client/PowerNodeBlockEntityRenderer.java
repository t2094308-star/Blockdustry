package com.blockdustry.client;

import com.blockdustry.power.PowerNodeBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

// PowerNode 激光渲染：对每个连接画一条激光线，颜色随电网满足率渐变（低=暖橙，高=亮蓝）喵
public class PowerNodeBlockEntityRenderer implements BlockEntityRenderer<PowerNodeBlockEntity> {
    public PowerNodeBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(PowerNodeBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor() || be.getPowerLinks().isEmpty()) return;
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 from = be.getBlockPos().getCenter().add(0, 0.5, 0).subtract(cam);
        float status = be.getPowerStatus();
        int r = (int) (255 * (1f - status));
        int g = (int) (255 * (0.3f + 0.5f * status));
        int b = (int) (255 * (0.2f + 0.8f * status));
        VertexConsumer vc = buffer.getBuffer(RenderType.lines());
        var matrix = pose.last().pose();
        for (BlockPos target : be.getPowerLinks()) {
            Vec3 to = target.getCenter().add(0, 0.5, 0).subtract(cam);
            vc.addVertex(matrix, (float) from.x, (float) from.y, (float) from.z)
                    .setColor(r, g, b, 200).setNormal(0f, 0f, 0f);
            vc.addVertex(matrix, (float) to.x, (float) to.y, (float) to.z)
                    .setColor(r, g, b, 200).setNormal(0f, 0f, 0f);
        }
    }
}
