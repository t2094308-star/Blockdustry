package com.blockdustry.building;

import java.util.function.Supplier;

import net.minecraft.world.level.block.entity.BlockEntityType;

// 电力源方块：1×1 调试建筑（Mindustry sandbox power-source），无限产电，无需交互逻辑喵
public class PowerSourceBlock extends BlockdustryBuildingBlock {
    public PowerSourceBlock(Properties properties, Supplier<BlockEntityType<?>> entityType, int size) {
        super(properties, entityType, size);
    }
}
