package com.blockdustry.building;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

// 爆破钻头方块物品：4×4 多格放置 + 必须放在有矿石的地面上（与机械钻头一致，Mindustry 钻机要有矿可采）喵
public class BlastDrillBuildingItem extends BlockdustryBuildingItem {
    private static final int SIZE = 4;

    public BlastDrillBuildingItem(Block block, Item.Properties properties) {
        super(block, properties, SIZE);
    }

    @Override
    public InteractionResult place(BlockPlaceContext ctx) {
        // 与基类一致的基准格计算（点击处或相邻可替换格）喵
        BlockPos base = ctx.getClickedPos();
        if (!ctx.getLevel().getBlockState(base).canBeReplaced(ctx)) {
            base = base.relative(ctx.getClickedFace());
        }
        // 4×4 占地正下方必须至少有一个矿石方块，否则放置失败不消耗喵
        boolean hasOre = false;
        for (int dx = 0; dx < SIZE && !hasOre; dx++) {
            for (int dz = 0; dz < SIZE; dz++) {
                if (DrillBlockEntity.oreResult(ctx.getLevel().getBlockState(base.offset(dx, -1, dz))) != null) {
                    hasOre = true;
                    break;
                }
            }
        }
        if (!hasOre) return InteractionResult.FAIL;
        return super.place(ctx);
    }
}
