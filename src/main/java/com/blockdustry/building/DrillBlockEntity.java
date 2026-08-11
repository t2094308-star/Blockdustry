package com.blockdustry.building;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

// 采集机方块实体：周期性积累进度，采到的矿石存入内置库存（满则自动停止，忠于 Mindustry）喵
public class DrillBlockEntity extends BlockdustryBuildingEntity {
    // 采集进度阈值，达到后产出一个物品喵
    private static final int PROGRESS_THRESHOLD = 40;
    // 采集进度（不持久化，仅内存中累计）喵
    private int progress;

    public DrillBlockEntity(BlockPos pos, BlockState state) {
        super(BlockdustryBlocks.DRILL_ENTITY.get(), pos, state);
    }

    // 每模组 tick：先卸库存给相邻传送带/容器，再产出（优先 offload，无人收才入库存）喵
    @Override
    protected void tickAnchor() {
        if (getStoredCount() > 0 && getStoredItem() != null && dumpItem(getStoredItem())) {
            removeOne();
        }
        if (isFull()) return;
        progress += 1;
        if (progress >= PROGRESS_THRESHOLD) {
            progress = 0;
            if (!dumpItem(Items.RAW_IRON)) {
                storeItem(Items.RAW_IRON);
            }
        }
    }
}
