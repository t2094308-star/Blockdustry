package com.blockdustry.distribution;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.blockdustry.building.BlockdustryBuildingBlock;

// 传送带桥方块（Mindustry ItemBridge size 1）：1×1 建筑，无固定朝向（配对方向动态决定桥面/端部渲染）喵。
// 放置时服务端调用 BE.onPlaced 触发自动配对（原版 playerPlaced）喵
public class ItemBridgeBlock extends BlockdustryBuildingBlock {
    public ItemBridgeBlock(Properties properties, Supplier<BlockEntityType<?>> entityType) {
        super(properties, entityType, 1);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ItemBridgeBlockEntity be) {
            be.onPlaced();
        }
    }
}
