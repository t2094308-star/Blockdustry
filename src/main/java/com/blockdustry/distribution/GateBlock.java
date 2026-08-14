package com.blockdustry.distribution;

import java.util.function.Supplier;

import com.blockdustry.building.BlockdustryBuildingBlock;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

// Mindustry OverflowGate 方块（overflow-gate / underflow-gate，size 1）喵。
// invert 标志区分两闸（Mindustry underflowGate = new OverflowGate{ invert = true }）喵。
// FACING 仅作视觉朝向（贴图箭头随放置旋转）；路由机制与朝向无关，完全由「物品来向」决定（见 GateBlockEntity）喵。
public class GateBlock extends BlockdustryBuildingBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    // false = overflow gate（直通优先，满则溢流）；true = underflow gate（侧向优先，侧堵才直通）喵
    private final boolean invert;

    public GateBlock(Properties properties, Supplier<BlockEntityType<?>> entityType, boolean invert) {
        super(properties, entityType, 1);
        this.invert = invert;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public boolean isInvert() {
        return invert;
    }

    public Direction getFacing(BlockState state) {
        return state.getValue(FACING);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx)
                .setValue(FACING, ctx.getHorizontalDirection());
    }
}
