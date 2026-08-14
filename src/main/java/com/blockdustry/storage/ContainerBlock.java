package com.blockdustry.storage;

import java.util.function.Supplier;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

// 存储容器方块（Mindustry StorageBlock / container，size 2）：纯存储建筑，无方向、无配置喵。
// 原版点击无配置 UI（仅通用信息面板列出库存），故不覆写 useWithoutItem；
// 简单存储显示交给渲染器（顶面主物品图标）+ Jade（内容 xN / 300）喵
public class ContainerBlock extends com.blockdustry.building.BlockdustryBuildingBlock {

    public ContainerBlock(BlockBehaviour.Properties properties, Supplier<BlockEntityType<?>> entityType, int size) {
        super(properties, entityType, size);
    }
}
