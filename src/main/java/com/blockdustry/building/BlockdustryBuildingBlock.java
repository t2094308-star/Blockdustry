package com.blockdustry.building;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

// 建筑方块基类：放置时创建对应建筑实体（不实现则方块无效）喵
// size 为多格边长（Mindustry size）；实体类型用 Supplier 延迟解析避免注册期 unbound 喵
public class BlockdustryBuildingBlock extends Block implements EntityBlock {
    // 多格建筑的格内位置（跨格贴图用）喵
    public enum Corner implements net.minecraft.util.StringRepresentable {
        NW("nw"), NE("ne"), SW("sw"), SE("se");

        private final String name;

        Corner(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final EnumProperty<Corner> CORNER = EnumProperty.create("corner", Corner.class);

    private final Supplier<BlockEntityType<?>> entityType;
    private final int size;

    public BlockdustryBuildingBlock(Properties properties, Supplier<BlockEntityType<?>> entityType, int size) {
        super(properties);
        this.entityType = entityType;
        this.size = size;
        this.registerDefaultState(this.stateDefinition.any().setValue(CORNER, Corner.NW));
    }

    public int getSize() {
        return size;
    }

    // 由 dx/dz（0..size-1）换算格内方位喵
    public static Corner cornerFor(int dx, int dz) {
        if (dx == 0 && dz == 0) return Corner.NW;
        if (dx != 0 && dz == 0) return Corner.NE;
        if (dx == 0 && dz != 0) return Corner.SW;
        return Corner.SE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CORNER);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return entityType.get().create(pos, state);
    }

    // 任一格被破坏时，联动破坏同建筑其余格（服务端，整组塌落符合 Mindustry 语义）喵
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && size > 1 && !state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockdustryBuildingEntity b && b.getAnchor() != null) {
                BlockPos anchor = b.getAnchor();
                for (int dx = 0; dx < size; dx++) {
                    for (int dz = 0; dz < size; dz++) {
                        BlockPos p = anchor.offset(dx, 0, dz);
                        if (!p.equals(pos) && level.getBlockState(p).is(this)) {
                            level.destroyBlock(p, false);
                        }
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
