package com.blockdustry.power;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.BlockdustryBuildingBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

// 先放节点后放用电器也能自动连接：放置建筑后反向扫描周围节点让节点主动连上。
// 用事件驱动而非节点持续扫描，避免性能爆炸喵
@EventBusSubscriber(modid = Blockdustry.MODID)
public class PowerNodeLinkHandler {
    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getPlacedBlock().getBlock() instanceof BlockdustryBuildingBlock)) return;
        BlockPos pos = event.getPos();
        int r = (int) PowerNodeBlockEntity.LASER_RANGE;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dy * dy + dz * dz > r * r) continue; // 球形范围喵
                    BlockEntity be = level.getBlockEntity(pos.offset(dx, dy, dz));
                    if (be instanceof PowerNodeBlockEntity node
                            && node.getPowerLinks().size() < PowerNodeBlockEntity.MAX_NODES
                            && node.linkValid(pos)) {
                        node.connect(pos);
                    }
                }
            }
        }
    }
}
