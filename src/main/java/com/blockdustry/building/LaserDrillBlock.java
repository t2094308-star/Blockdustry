package com.blockdustry.building;

import java.util.function.Supplier;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

// laser-drill 激光钻头方块（Mindustry size 3）：继承多格建筑骨架 BlockdustryBuildingBlock 喵。
// 3×3 占地：blockstate 覆盖 9 个 corner 变体（NW/N/NE/W/C/E/SW/S/SE），模型按象限裁顶面 1/3 喵
public class LaserDrillBlock extends BlockdustryBuildingBlock {

    public LaserDrillBlock(BlockBehaviour.Properties properties, Supplier<BlockEntityType<?>> entityType, int size) {
        super(properties, entityType, size);
    }
}
