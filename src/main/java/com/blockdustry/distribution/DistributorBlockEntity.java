package com.blockdustry.distribution;

import com.blockdustry.building.RouterBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// Mindustry distributor：Router 子类（Blocks.java: `distributor = new Router("distributor"){ size = 2 }`）。
// 行为与 Router 完全一致：收 1 件、轮询卸给相邻可接收建筑；仅占地 2×2（size=2，圆周均分到 2×2 四周）喵
public class DistributorBlockEntity extends RouterBlockEntity {
    public DistributorBlockEntity(BlockPos pos, BlockState state) {
        super(JunctionRegistrar.DISTRIBUTOR_ENTITY.get(), pos, state);
    }

    public DistributorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
