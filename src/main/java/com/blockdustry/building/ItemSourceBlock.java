package com.blockdustry.building;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

// 物品源方块：1×1 建筑，空手右键在服务端循环切换产物（煤→石墨→硅→铅）喵。
// 覆写 useWithoutItem（1.21.1 空手交互入口），空手右键即切换喵
public class ItemSourceBlock extends BlockdustryBuildingBlock {
    public ItemSourceBlock(Properties properties, Supplier<BlockEntityType<?>> entityType, int size) {
        super(properties, entityType, size);
    }

    // 空手右键：服务端切换产物并提示玩家喵
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof ItemSourceBlockEntity src) {
                src.cycleProduct();
                if (player instanceof ServerPlayer sp) {
                    sp.sendSystemMessage(Component.literal(
                            "物品源产物已切换为: " + src.getProduct().getDescription().getString()), true);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
