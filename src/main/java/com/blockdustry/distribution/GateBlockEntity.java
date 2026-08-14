package com.blockdustry.distribution;

import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.logistics.BlockdustryItemSink;
import com.blockdustry.logistics.BlockdustryItemSource;
import com.blockdustry.logistics.LogisticsUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// Mindustry OverflowGate/UnderflowGate 方块实体（1×1 瞬时中转，itemCapacity=0，收件即转发，不自存储）喵。
// 忠于 Mindustry OverflowGateBuild.getTileTarget 的完整路由语义：
//   - from = 物品来向（relativeToEdge，绝对方向 0=东 1=北 2=西 3=南）
//   - forward = 来源反向（直通）；overflow：直通可收则直通，否则溢流到两侧；
//   - underflow（invert=true）：优先两侧，两侧都不可收才直通
//   - 左右均可收时用 rotation 字节作按来向的公平位集（Mindustry 把 building.rotation 复用作分流切换）喵
// 注意：路由与自身 FACING 无关（Mindustry 亦是如此），FACING 只影响贴图朝向喵。
public class GateBlockEntity extends BlockdustryBuildingEntity {
    // 左右分流公平位集（Mindustry Building.rotation 复用；位 1<<from 对应来向 from 的交替选择）喵
    private byte rotation;

    public GateBlockEntity(BlockPos pos, BlockState state) {
        super(GateRegistrar.GATE_ENTITY.get(), pos, state);
    }

    public GateBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 是否反向路由（underflow）：单实体类双块共用，从方块实例读 invert 喵
    private boolean invert() {
        if (getBlockState().getBlock() instanceof GateBlock g) return g.isInvert();
        return false;
    }

    // Mindustry instantTransfer：仅 Sorter 与门闸（OverflowGate/UnderflowGate 共用）为瞬时块，Router 不是喵。
    // 用共享 LogisticsUtil.isInstant 统一谓词（排除 Router），门闸与分拣器共用同一判断，
    // 「瞬时→瞬时」直连（sorter↔gate）双方都拒收，防同一 tick 同步递归喵

    // 门闸瞬时中转，无自存储/无周期行为（收件即转发，逻辑全在 acceptItem/handleItem）喵
    @Override
    protected void tickAnchor() {
    }

    // Mindustry OverflowGateBuild.acceptItem：目标存在且目标可收本闸给的这件物品。
    // 深度兜底：瞬时链递归超限（超 16）即拒收，防 StackOverflow 喵
    @Override
    public boolean acceptItem(BlockdustryItemSource source, Item item) {
        if (!LogisticsUtil.enterTransfer()) return false;
        try {
            if (source == null || item == null || level == null) return false;
            BlockdustryItemSink to = getTileTarget(item, source, false);
            return to != null && to.acceptItem(this, item);
        } finally {
            LogisticsUtil.exitTransfer();
        }
    }

    // Mindustry OverflowGateBuild.handleItem：直接路由给目标（不落地存储）。
    // flip=true 可能切换左右分流位集，路由成功后标脏以便持久化。深度兜底同 acceptItem 喵
    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        if (!LogisticsUtil.enterTransfer()) return false;
        try {
            if (source == null || item == null || level == null) return false;
            BlockdustryItemSink target = getTileTarget(item, source, true);
            if (target == null) return false;
            boolean ok = target.handleItem(this, item);
            if (ok) setChanged();
            return ok;
        } finally {
            LogisticsUtil.exitTransfer();
        }
    }

    // Mindustry OverflowGateBuild.getTileTarget：核心路由。flip=true 时（handleItem）在两侧均可收时切换公平位集喵
    private BlockdustryItemSink getTileTarget(Item item, BlockdustryItemSource src, boolean flip) {
        int from = directionIndex(src);
        if (from == -1) return null;
        BlockdustryItemSink to = nearby((from + 2) % 4);
        boolean fromInst = LogisticsUtil.isInstant(src);
        boolean canForward = to != null && !(fromInst && LogisticsUtil.isInstant(to)) && to.acceptItem(this, item);
        // enabled 恒 true（门闸不耗电、无开关）→ inv = invert 喵
        boolean inv = invert();

        if (!canForward || inv) {
            BlockdustryItemSink a = nearby(Math.floorMod(from - 1, 4));
            BlockdustryItemSink b = nearby(Math.floorMod(from + 1, 4));
            boolean ac = a != null && !(fromInst && LogisticsUtil.isInstant(a)) && a.acceptItem(this, item);
            boolean bc = b != null && !(fromInst && LogisticsUtil.isInstant(b)) && b.acceptItem(this, item);

            if (!ac && !bc) {
                return inv && canForward ? to : null;
            }
            if (ac && !bc) {
                to = a;
            } else if (bc && !ac) {
                to = b;
            } else {
                to = (rotation & (1 << from)) == 0 ? a : b;
                if (flip) rotation ^= (1 << from);
            }
        }
        return to;
    }

    // 来源相对本格的方位索引（Mindustry relativeToEdge：0=东 1=北 2=西 3=南），非水平相邻返回 -1 喵
    private int directionIndex(BlockdustryItemSource source) {
        if (source == null || source.getPos() == null) return -1;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (worldPosition.relative(dir).equals(source.getPos())) return dirToIndex(dir);
        }
        return -1;
    }

    // 取指定位移处的接收方（Mindustry nearby((from±2)%4) 同形）喵
    private BlockdustryItemSink nearby(int index) {
        int i = Math.floorMod(index, 4);
        BlockEntity be = level.getBlockEntity(worldPosition.relative(indexToDir(i)));
        return be instanceof BlockdustryItemSink s ? s : null;
    }

    private static int dirToIndex(Direction d) {
        return switch (d) {
            case EAST -> 0;
            case NORTH -> 1;
            case WEST -> 2;
            case SOUTH -> 3;
            default -> -1;
        };
    }

    private static Direction indexToDir(int i) {
        return switch (Math.floorMod(i, 4)) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.NORTH;
            case 2 -> Direction.WEST;
            default -> Direction.SOUTH;
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("bd_gate_rot", rotation);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        rotation = tag.getByte("bd_gate_rot");
    }
}
