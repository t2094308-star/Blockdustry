package com.blockdustry.building;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

// laser-drill 方块物品：继承多格放置物品，放置前预检 3×3 下方有矿石喵。
// Mindustry Drill.canPlaceOn：多格建筑任一格下方可挖矿即可放置，无矿不能放喵
public class LaserDrillBuildingItem extends BlockdustryBuildingItem {
    private final int size;

    public LaserDrillBuildingItem(Block block, Item.Properties properties, int size) {
        super(block, properties, size);
        this.size = size;
    }

    @Override
    public InteractionResult place(BlockPlaceContext ctx) {
        // 放置基准格与原版 BlockItem 一致：点击处或相邻格喵
        BlockPos base = ctx.getClickedPos();
        if (!ctx.getLevel().getBlockState(base).canBeReplaced(ctx)) {
            base = base.relative(ctx.getClickedFace());
        }
        boolean hasOre = false;
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                if (DrillBlockEntity.oreResult(ctx.getLevel().getBlockState(base.offset(dx, -1, dz))) != null) {
                    hasOre = true;
                    break;
                }
            }
            if (hasOre) break;
        }
        if (!hasOre) return InteractionResult.FAIL;
        return super.place(ctx);
    }
}
