package com.blockdustry.building;

import java.util.function.Supplier;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

// Mindustry PowerDiode：1×1 可旋转单向二极管喵。
// 忠实原版：rotate=true（放置随朝向）、solid、insulated、group=power 喵。
// FACING 仅作朝向（箭头方向/输入输出侧判定），机制见 DiodeBlockEntity 喵
public class DiodeBlock extends BlockdustryBuildingBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public DiodeBlock(Properties properties, Supplier<BlockEntityType<?>> entityType) {
        super(properties, entityType, 1);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    // 二极管朝向（箭头方向 = 输电方向 = front 侧）喵
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
