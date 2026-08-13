package com.blockdustry.building;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// 传送带方块：facing 输送方向 + conn 连接类型（决定渲染变体）喵
public class ConveyorBlock extends BlockdustryBuildingBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Connection> CONN = EnumProperty.create("conn", Connection.class);
    // 传送带是低矮平台（0~4 高度），碰撞箱与模型一致喵
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 4, 16);

    public enum Connection implements StringRepresentable {
        NONE("none"), STRAIGHT("straight"), CORNER("corner"),
        CORNER_LEFT("corner_left"), CORNER_RIGHT("corner_right"),
        T("t"), CROSS("cross");

        private final String name;

        Connection(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public ConveyorBlock(Properties properties, Supplier<BlockEntityType<?>> entityType) {
        super(properties, entityType, 1);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH).setValue(CONN, Connection.NONE));
    }

    public Direction getFacing(BlockState state) {
        return state.getValue(FACING);
    }

    // 低矮平台碰撞箱与选中箱喵
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, CONN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx)
                .setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) updateConnection(level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide) updateConnection(level, pos);
    }

    private static void updateConnection(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof ConveyorBlock)) return;
        Connection c = computeConnection(level, pos, state);
        if (state.getValue(CONN) != c) {
            level.setBlock(pos, state.setValue(CONN, c), 3);
        }
    }

    // Mindustry 语义：传送带只与「朝向彼此」的相邻传送带连接（并行同向带独立不 blend）喵
    private static boolean connects(Level level, BlockPos pos, BlockState state, Direction dir) {
        BlockState neighbor = level.getBlockState(pos.relative(dir));
        if (!(neighbor.getBlock() instanceof ConveyorBlock)) return false;
        Direction myFacing = state.getValue(FACING);
        Direction nFacing = neighbor.getValue(FACING);
        return myFacing == dir || nFacing == dir.getOpposite();
    }

    private static Connection computeConnection(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        boolean f = connects(level, pos, state, facing);
        boolean b = connects(level, pos, state, facing.getOpposite());
        boolean l = connects(level, pos, state, facing.getClockWise());
        boolean r = connects(level, pos, state, facing.getCounterClockWise());
        int n = (f ? 1 : 0) + (b ? 1 : 0) + (l ? 1 : 0) + (r ? 1 : 0);
        if (n == 0) return Connection.NONE;
        if (f && b) return (l && r) ? Connection.CROSS : (l || r) ? Connection.T : Connection.STRAIGHT;
        if ((l && r) && !f && !b) return Connection.STRAIGHT;
        // 转角：前/后 + 左/右各连一侧。侧向在 facing 顺时针侧(l)=右转，逆时针侧(r)=左转喵
        // conveyor-1 原版贴图=左转（带面从逆时针侧进），右转需用垂直翻转的镜像贴图喵
        if ((f || b) && (l || r)) {
            return l ? Connection.CORNER_RIGHT : Connection.CORNER_LEFT;
        }
        return Connection.STRAIGHT;
    }
}
