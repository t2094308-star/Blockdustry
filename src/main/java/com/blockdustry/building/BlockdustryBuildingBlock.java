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
    // 多格建筑的格内位置（跨格贴图用）：3×3 共 9 值，行0/1/2 × 列0/1/2 喵
    // 顺序 NW,N,NE,W,C,E,SW,S,SE：保持 2×2 的 NW/NE/SW/SE 序列不变，兼容旧存档喵
    public enum Corner implements net.minecraft.util.StringRepresentable {
        NW("nw"), N("n"), NE("ne"), W("w"), C("c"), E("e"), SW("sw"), S("s"), SE("se");

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

    // 由 dx/dz（0..size-1，size 支持 1/2/3）换算格内方位喵
    // size==2：四象限 NW/NE/SW/SE（原 2×2 语义，drill/graphite_press）喵
    // size>=3：九宫格 行0/1/2 × 列0/1/2 → NW,N,NE / W,C,E / SW,S,SE（core/unit_factory）喵
    public static Corner cornerFor(int dx, int dz, int size) {
        if (size <= 1) return Corner.NW;
        if (size == 2) {
            if (dx == 0 && dz == 0) return Corner.NW;
            if (dx != 0 && dz == 0) return Corner.NE;
            if (dx == 0 && dz != 0) return Corner.SW;
            return Corner.SE;
        }
        if (dx <= 0) {
            if (dz <= 0) return Corner.NW;
            if (dz == 1) return Corner.W;
            return Corner.SW;
        } else if (dx == 1) {
            if (dz <= 0) return Corner.N;
            if (dz == 1) return Corner.C;
            return Corner.S;
        } else {
            if (dz <= 0) return Corner.NE;
            if (dz == 1) return Corner.E;
            return Corner.SE;
        }
    }

    // 便捷重载：不传 size 时按 2×2 语义（原行为）喵
    public static Corner cornerFor(int dx, int dz) {
        return cornerFor(dx, dz, 2);
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
