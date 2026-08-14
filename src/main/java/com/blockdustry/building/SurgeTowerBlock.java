package com.blockdustry.building;

import java.util.function.Supplier;

import net.minecraft.world.level.block.entity.BlockEntityType;

// Mindustry PowerNode 涌电塔（surge-tower）：2×2 电力节点，远距输电喵。
// 忠实原版：size=2、maxNodes=2、laserRange=40f（参数在 SurgeTowerBlockEntity）喵。
// 注意：原版 surgeTower = new PowerNode("surge-tower"){{ size = 2; }}，占地 2×2 而非 3×3，
// 本类按原版 2×2 实现（task 文案的「3×3」为误，数据以 Blocks.java L2494 为准）喵
public class SurgeTowerBlock extends BlockdustryBuildingBlock {
    public SurgeTowerBlock(Properties properties, Supplier<BlockEntityType<?>> entityType) {
        super(properties, entityType, 2);
    }
}
