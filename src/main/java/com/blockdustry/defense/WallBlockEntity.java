package com.blockdustry.defense;

import com.blockdustry.building.BlockdustryBuildingEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 墙体方块实体（Mindustry Wall.WallBuild）：纯被动静态方块，无每 tick 逻辑喵。
// 仅承载队伍/装甲/整组共享血量（BlockdustryBuildingEntity 基础能力），渲染走方块模型（无 BER）喵
public class WallBlockEntity extends BlockdustryBuildingEntity {

    public WallBlockEntity(BlockPos pos, BlockState state) {
        super(DefenseRegistrar.WALL_ENTITY.get(), pos, state);
    }

    public WallBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void tickAnchor() {
        // Mindustry Wall 无 updateTile 逻辑，墙体纯静态喵
    }
}
