package com.blockdustry.defense;

import com.blockdustry.building.BlockdustryBuildingEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 高级墙体方块实体（plastanium/thorium/surge 墙，Mindustry Wall.WallBuild）：纯被动静态方块，无每 tick 逻辑喵。
// 与批1F 的 WallBlockEntity 同构（T43/T44 已占 defense.WallBlockEntity 类名与「wall」BE id），
// 本类独立命名 AdvancedWallBlockEntity + BE id「advanced_wall」，避免并行注册冲突喵。
// 仅承载队伍/装甲/整组共享血量（BlockdustryBuildingEntity 基础能力），渲染走方块模型（无 BER）喵。
// 装甲：原版 Wall 未设 armor，三墙 armor 均 0（仅血量递增）喵。
// surge 受击放电由 AdvancedWallRegistrar.SurgeHandler 事件处理，不在 tick 内喵。
public class AdvancedWallBlockEntity extends BlockdustryBuildingEntity {
    public AdvancedWallBlockEntity(BlockPos pos, BlockState state) {
        super(AdvancedWallRegistrar.WALL_ENTITY.get(), pos, state);
    }

    public AdvancedWallBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // Mindustry Wall 无 updateTile 逻辑，墙体纯静态喵
    @Override
    protected void tickAnchor() {
    }
}
