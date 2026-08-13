package com.blockdustry.building;

import java.util.function.Supplier;

import com.blockdustry.lib.BlockHealthApi;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

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
    // 建筑视觉/碰撞高度（格）：核心 3 高（BER 画 3×3×3 立方体），其余建筑默认 1 层喵
    private final int height;

    public BlockdustryBuildingBlock(Properties properties, Supplier<BlockEntityType<?>> entityType, int size) {
        this(properties, entityType, size, 1);
    }

    public BlockdustryBuildingBlock(Properties properties, Supplier<BlockEntityType<?>> entityType, int size, int height) {
        super(properties);
        this.entityType = entityType;
        this.size = size;
        this.height = Math.max(1, height);
        this.registerDefaultState(this.stateDefinition.any().setValue(CORNER, Corner.NW));
    }

    public int getSize() {
        return size;
    }

    public int getHeight() {
        return height;
    }

    // 碰撞箱：多格建筑返回「整组包围盒」（从该格本地坐标看，覆盖整个 size×size×height 区域）。
    // 背景：模型置空后基类默认 getShape 只返回该格 1×1×1，核心视觉 3 高但 y=1/2 层无方块 → 上部没碰撞箱，
    //       玩家直接穿过（研究-渲染与模型坑.md §3）。每格都返回组包围盒（非锚点格用负偏移），
    //       整组在世界坐标重合为同一个大 AABB，等价于整座建筑一个实心碰撞体喵
    //
    // 顶层碰撞修复：MC 碰撞求解器（BlockCollisions）只扫「实体 AABB + 上下左右各 1 格」的方块位置，
    //       基座块的 3 高包围盒在玩家站到顶部时距基座 2+ 格，根本不会被扫描 → 顶部穿入（研究-渲染与模型坑.md §3 补坑）。
    //       修复：核心放置时在上层也放同款隐形方块（模型置空，仅锚点格 BER 画一次），
    //       每格按「与锚点的 y 层差」返回对应高度 = (height - layer) 的组包围盒，三层拼成完整 3×3×3，层内都在求解器可达范围喵
    // getShape / getCollisionShape 都走 layer 感知，保证两者一致（含 F3+B 碰撞轮廓与鼠标高亮）喵
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (size <= 1) {
            return super.getShape(state, level, pos, context);
        }
        return groupShape(state, height - layerAt(level, pos));
    }

    // 碰撞箱：与 getShape 同源，显式覆写确保一致（默认实现虽委托 getShape，但显式声明更稳，且按层裁剪高度）喵
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (size <= 1) {
            return super.getCollisionShape(state, level, pos, context);
        }
        return groupShape(state, height - layerAt(level, pos));
    }

    // 组包围盒：h 为该层向上覆盖的格数（基座 h=height，往上每层减 1）喵
    private VoxelShape groupShape(BlockState state, int h) {
        Corner corner = state.getValue(CORNER);
        int dx = cornerDx(corner, size);
        int dz = cornerDz(corner, size);
        // Block.box 单位 1/16；允许越出该格（负偏移/超过 16），碰撞检测按世界坐标正确展开喵
        return Block.box(-dx * 16.0, 0.0, -dz * 16.0,
                (size - dx) * 16.0, Math.max(1, h) * 16.0, (size - dz) * 16.0);
    }

    // 本格相对锚点的 y 层差（基座 0，上层 1/2…）；无锚点/无 BE 时按基座处理喵
    private int layerAt(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BlockdustryBuildingEntity be) {
            BlockPos anchor = be.getAnchor();
            if (anchor != null) {
                return pos.getY() - anchor.getY();
            }
        }
        return 0;
    }

    // corner → 组内 x 偏移（0..size-1，与 cornerFor 互逆）喵
    private static int cornerDx(Corner corner, int size) {
        if (size <= 2) {
            return (corner == Corner.NE || corner == Corner.SE) ? 1 : 0;
        }
        return switch (corner) {
            case N, C, S -> 1;
            case NE, E, SE -> 2;
            default -> 0; // NW, W, SW
        };
    }

    // corner → 组内 z 偏移（0..size-1）喵
    private static int cornerDz(Corner corner, int size) {
        if (size <= 2) {
            return (corner == Corner.SW || corner == Corner.SE) ? 1 : 0;
        }
        return switch (corner) {
            case SW, S, SE -> 2;
            case W, C, E -> 1;
            default -> 0; // NW, N, NE
        };
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

    // 任一格被破坏时，联动破坏同建筑其余格（服务端，整组塌落符合 Mindustry 语义）。
    // 多格建筑上层还有隐形碰撞格（见 getShape 注释），需把 height 层全部扫掉喵
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && size > 1 && !state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockdustryBuildingEntity b && b.getAnchor() != null) {
                BlockPos anchor = b.getAnchor();
                // 整组共享血量：注销组（幂等），避免组注册残留喵
                BlockHealthApi.unregisterGroup((ServerLevel) level, anchor);
                for (int lvl = 0; lvl < height; lvl++) {
                    for (int dx = 0; dx < size; dx++) {
                        for (int dz = 0; dz < size; dz++) {
                            BlockPos p = anchor.offset(dx, lvl, dz);
                            if (!p.equals(pos) && level.getBlockState(p).is(this)) {
                                level.destroyBlock(p, false);
                            }
                        }
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
