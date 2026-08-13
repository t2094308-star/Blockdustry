package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.BlockdustryBlocks;
import com.blockdustry.building.BlockdustryBuildingBlock;
import com.blockdustry.building.BlockdustryBuildingEntity;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

// 多格建筑选中框：准星指向多格建筑时高亮整组「大方块」而非单格喵
@EventBusSubscriber(modid = Blockdustry.MODID, value = Dist.CLIENT)
public class BlockdustryHighlightHandler {
    @SubscribeEvent
    public static void onHighlight(RenderHighlightEvent.Block event) {
        if (!(event.getTarget() instanceof BlockHitResult bhr)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        BlockPos pos = bhr.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockdustryBuildingBlock building) || building.getSize() <= 1) return;
        BlockEntity be = mc.level.getBlockEntity(pos);
        if (!(be instanceof BlockdustryBuildingEntity b) || b.getAnchor() == null) return;
        BlockPos anchor = b.getAnchor();
        int size = building.getSize();
        // 核心视觉为 3×3×3 正方体，选中框高度跟随 3 格；其余多格建筑仍 1 格高喵
        int height = (building == BlockdustryBlocks.CORE.get()) ? size : 1;
        Vec3 cam = event.getCamera().getPosition();
        AABB box = new AABB(anchor.getX(), anchor.getY(), anchor.getZ(),
                anchor.getX() + size, anchor.getY() + height, anchor.getZ() + size)
                .move(-cam.x, -cam.y, -cam.z);
        VertexConsumer lines = event.getMultiBufferSource().getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(event.getPoseStack(), lines, box, 0f, 0f, 0f, 0.4f);
        event.setCanceled(true);
    }
}
