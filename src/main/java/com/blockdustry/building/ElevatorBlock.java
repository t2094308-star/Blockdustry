package com.blockdustry.building;

import java.util.function.Supplier;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 垂直提升机方块（T9 方案 B1）：直上带，无水平朝向，输出固定朝上；
// 模型用传送带风格的竖管（见 assets/.../models/block/elevator.json）喵
public class ElevatorBlock extends BlockdustryBuildingBlock {
    public ElevatorBlock(Properties properties, Supplier<BlockEntityType<?>> entityType) {
        super(properties, entityType, 1);
    }

    // 输出方向固定朝上（y 轴），与传送带 getFacing(BlockState) 同形喵
    public Direction getFacing(BlockState state) {
        return Direction.UP;
    }
}
