package com.blockdustry.building;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// Router 分发器（Mindustry router）：收 1 件，轮询卸给相邻可接收传送带/建筑喵
public class RouterBlockEntity extends BlockdustryBuildingEntity {
    public RouterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockdustryBlocks.ROUTER_ENTITY.get(), pos, state);
    }

    public RouterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // Router 只存 1 件喵
    @Override
    public boolean acceptsItem(Item item) {
        return getStoredCount() < 1;
    }

    @Override
    protected void tickAnchor() {
        if (getStoredCount() > 0 && getStoredItem() != null && dumpItem(getStoredItem())) {
            removeOne();
        }
    }
}
