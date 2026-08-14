package com.blockdustry.defense;

import java.util.function.Supplier;

import com.blockdustry.building.BlockdustryBuildingBlock;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

// 防御墙方块基类（Mindustry Wall 迁移）：静态实心墙，size 1/2 均占 1 层高喵
public class WallBlock extends BlockdustryBuildingBlock {
    public WallBlock(BlockBehaviour.Properties properties, Supplier<BlockEntityType<?>> entityType, int size) {
        super(properties, entityType, size, 1);
    }
}
