package com.blockdustry.building;

import com.blockdustry.BlockdustryTeams;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

// 建筑方块物品：多格放置时预检区域、填充其余格、统一设锚点与队伍喵
public class BlockdustryBuildingItem extends BlockItem {
    private final int size;

    public BlockdustryBuildingItem(Block block, Item.Properties properties, int size) {
        super(block, properties);
        this.size = size;
    }

    @Override
    public InteractionResult place(BlockPlaceContext ctx) {
        // 放置基准格（与原版 BlockItem 一致：点击处或相邻格）喵
        BlockPos base = ctx.getClickedPos();
        if (!ctx.getLevel().getBlockState(base).canBeReplaced(ctx)) {
            base = base.relative(ctx.getClickedFace());
        }
        // 预检 size×size 区域全部可替换，否则失败不消耗喵
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                if (!ctx.getLevel().getBlockState(base.offset(dx, 0, dz)).canBeReplaced(ctx)) {
                    return InteractionResult.FAIL;
                }
            }
        }
        InteractionResult result = super.place(ctx);
        // 服务端：填充其余格 + 统一锚点 + 继承放置者队伍喵
        if (result.consumesAction() && !ctx.getLevel().isClientSide && size > 1) {
            ServerLevel serverLevel = (ServerLevel) ctx.getLevel();
            for (int dx = 0; dx < size; dx++) {
                for (int dz = 0; dz < size; dz++) {
                    BlockPos p = base.offset(dx, 0, dz);
                    if (!p.equals(base)) {
                        serverLevel.setBlockAndUpdate(p, getBlock().defaultBlockState()
                                .setValue(BlockdustryBuildingBlock.CORNER, BlockdustryBuildingBlock.cornerFor(dx, dz)));
                    }
                    if (serverLevel.getBlockEntity(p) instanceof BlockdustryBuildingEntity b) {
                        b.setAnchor(base);
                        b.setChanged();
                    }
                    if (ctx.getPlayer() != null) {
                        BlockdustryTeams.setTeam(serverLevel, p, BlockdustryTeams.getTeam(ctx.getPlayer()));
                    }
                }
            }
        }
        return result;
    }
}
