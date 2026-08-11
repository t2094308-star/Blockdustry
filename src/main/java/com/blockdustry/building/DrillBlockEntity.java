package com.blockdustry.building;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

// 采集机方块实体：下方有矿石时无限挖掘，产出对应矿石（挖掘等级限制后续再加）喵
public class DrillBlockEntity extends BlockdustryBuildingEntity {
    // 采集进度阈值，达到后产出一个物品喵
    private static final int PROGRESS_THRESHOLD = 40;
    // 采集进度（不持久化，仅内存中累计）喵
    private int progress;

    public DrillBlockEntity(BlockPos pos, BlockState state) {
        super(BlockdustryBlocks.DRILL_ENTITY.get(), pos, state);
    }

    // 每模组 tick：先卸库存，再检测下方矿石（无矿则停），产对应矿石（优先 offload）喵
    @Override
    protected void tickAnchor() {
        if (getStoredCount() > 0 && getStoredItem() != null && dumpItem(getStoredItem())) {
            removeOne();
        }
        Item ore = detectOre();
        if (ore == null) {
            progress = 0;
            return;
        }
        if (isFull()) return;
        progress += 1;
        if (progress >= PROGRESS_THRESHOLD) {
            progress = 0;
            if (!dumpItem(ore)) {
                storeItem(ore);
            }
        }
    }

    // 遍历 2×2 占地正下方找矿石，返回对应产出物品；无矿返回 null 喵
    private Item detectOre() {
        BlockPos base = hasAnchor() ? getAnchor() : worldPosition;
        int size = getSize();
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                BlockState below = level.getBlockState(base.offset(dx, -1, dz));
                Item ore = oreResult(below);
                if (ore != null) return ore;
            }
        }
        return null;
    }

    // 矿石方块 → 产出物品映射（Mindustry 钻机采下方矿石；放置检查也复用）喵
    public static Item oreResult(BlockState state) {
        Block b = state.getBlock();
        if (b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE) return Items.RAW_IRON;
        if (b == Blocks.COPPER_ORE || b == Blocks.DEEPSLATE_COPPER_ORE) return Items.RAW_COPPER;
        if (b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE) return Items.RAW_GOLD;
        if (b == Blocks.COAL_ORE || b == Blocks.DEEPSLATE_COAL_ORE) return Items.COAL;
        if (b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE) return Items.DIAMOND;
        if (b == Blocks.EMERALD_ORE || b == Blocks.DEEPSLATE_EMERALD_ORE) return Items.EMERALD;
        if (b == Blocks.REDSTONE_ORE || b == Blocks.DEEPSLATE_REDSTONE_ORE) return Items.REDSTONE;
        if (b == Blocks.LAPIS_ORE || b == Blocks.DEEPSLATE_LAPIS_ORE) return Items.LAPIS_LAZULI;
        return null;
    }
}
