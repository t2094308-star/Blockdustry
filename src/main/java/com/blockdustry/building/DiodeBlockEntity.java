package com.blockdustry.building;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.blockdustry.power.BlockdustryPowerNode;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// Mindustry PowerDiode 单向二极管实体喵。
// 忠实原版 PowerDiode.updateTile 算法：二极管自己不产/不存电（无电力模块），
// 只把「后侧电网」电池电能按电池百分比差转移给「前侧电网」——仅当后侧百分比高于前侧时才流动（单向）喵。
// 网格聚合沿 getPowerLinks() 双向 BFS（前向 links + 反向索引，覆盖电池侧自身无 links 的情况）喵。
// 已知模型差距：Blockdustry 电池为覆盖率简化模型（status 每 tick 被电网覆盖写回），
// 二极管按该模型允许的电池 status 调整执行原版转移公式，见整合清单「已知差距」喵
public class DiodeBlockEntity extends BlockdustryBuildingEntity {
    public DiodeBlockEntity(BlockPos pos, BlockState state) {
        this(DiodeSurgeTowerRegistrar.DIODE_ENTITY.get(), pos, state);
    }

    public DiodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 二极管不吃任何物品喵
    @Override
    public boolean acceptsItem(Item item) {
        return false;
    }

    @Override
    protected void tickAnchor() {
        if (level == null || level.isClientSide) return;
        if (!(getBlockState().getBlock() instanceof DiodeBlock db)) return;

        Direction facing = db.getFacing(getBlockState());
        BlockPos frontPos = worldPosition.relative(facing);
        BlockPos backPos = worldPosition.relative(facing.getOpposite());

        BlockEntity frontBE = level.getBlockEntity(frontPos);
        BlockEntity backBE = level.getBlockEntity(backPos);
        if (!(backBE instanceof BlockdustryPowerNode back) || !(frontBE instanceof BlockdustryPowerNode front)) return;

        // 同队判定（原版 back().team != team || front().team != team → return）喵
        BlockdustryTeam team = getTeam();
        if (team == null || !team.canInteract(back.getTeam()) || !team.canInteract(front.getTeam())) return;

        // 每 tick 建一次图上下文（全部有电建筑 + 反向索引），供两侧聚合复用喵
        GraphContext ctx = new GraphContext();

        // 两侧属同一电网（原版 backGraph == frontGraph → return）喵
        if (ctx.connected(backPos, frontPos)) return;

        float backStored = ctx.totalStored(backPos);
        float backCapacity = ctx.totalCapacity(backPos);
        float frontStored = ctx.totalStored(frontPos);
        float frontCapacity = ctx.totalCapacity(frontPos);

        // 任一侧无电池 → 无转移（原版公式 capacity=0 时 amount=0）喵
        if (backCapacity <= 0f || frontCapacity <= 0f) return;

        // 单向：仅当后侧电池百分比 > 前侧时才流动喵
        if (backStored / backCapacity <= frontStored / frontCapacity) return;

        float targetPercentage = (frontStored + backStored) / (frontCapacity + backCapacity);
        // 转移后侧/前侧差的一半，使两侧趋近目标百分比（原版公式）喵
        float amount = (targetPercentage * frontCapacity - frontStored) / 2f;
        // 防超发：不超过前侧可容纳量喵
        amount = Math.max(0f, Math.min(amount, frontCapacity - frontStored));
        if (amount <= 0f) return;

        // 后侧扣电、前侧充电（按容量比例分摊到各电池）喵
        ctx.applyTransfer(backPos, -amount, backCapacity);
        ctx.applyTransfer(frontPos, amount, frontCapacity);
    }

    // —— 网格聚合上下文（沿 getPowerLinks() 双向 BFS）喵 ——
    private final class GraphContext {
        private final Map<Long, BlockdustryPowerNode> byPos = new HashMap<>();
        // 反向索引：targetPos → 所有「links 里含 targetPos」的节点 pos 喵
        private final Map<Long, List<Long>> reverse = new HashMap<>();

        GraphContext() {
            for (BlockdustryBuildingEntity b : BlockdustryBuildings.all()) {
                if (b instanceof BlockdustryPowerNode p) {
                    byPos.put(b.getBlockPos().asLong(), p);
                    for (BlockPos link : p.getPowerLinks()) {
                        reverse.computeIfAbsent(link.asLong(), k -> new ArrayList<>()).add(p.getPos().asLong());
                    }
                }
            }
        }

        // a 与 b 是否在同一电网（双向可达）喵
        boolean connected(BlockPos from, BlockPos target) {
            Set<Long> visited = new HashSet<>();
            Queue<Long> queue = new ArrayDeque<>();
            queue.add(from.asLong());
            visited.add(from.asLong());
            while (!queue.isEmpty()) {
                long p = queue.poll();
                if (p == target.asLong()) return true;
                BlockdustryPowerNode node = byPos.get(p);
                if (node != null) {
                    for (BlockPos link : node.getPowerLinks()) {
                        if (visited.add(link.asLong())) queue.add(link.asLong());
                    }
                }
                List<Long> rev = reverse.get(p);
                if (rev != null) {
                    for (long r : rev) {
                        if (visited.add(r)) queue.add(r);
                    }
                }
            }
            return false;
        }

        float totalStored(BlockPos start) {
            final float[] sum = {0f};
            visit(start, n -> sum[0] += n.powerStored());
            return sum[0];
        }

        float totalCapacity(BlockPos start) {
            final float[] sum = {0f};
            visit(start, n -> sum[0] += n.powerCapacity());
            return sum[0];
        }

        // 按容量比例把 amount（正充/负扣）分摊到该侧所有电池喵
        void applyTransfer(BlockPos start, float amount, float totalCapacity) {
            if (amount == 0f || totalCapacity <= 0f) return;
            visit(start, n -> {
                float cap = n.powerCapacity();
                if (cap <= 0f) return;
                float portion = amount * (cap / totalCapacity);
                float newStored = Math.max(0f, Math.min(cap, n.powerStored() + portion));
                n.setPowerStatus(newStored / cap);
            });
        }

        private void visit(BlockPos start, java.util.function.Consumer<BlockdustryPowerNode> visitor) {
            Set<Long> visited = new HashSet<>();
            Queue<Long> queue = new ArrayDeque<>();
            queue.add(start.asLong());
            visited.add(start.asLong());
            while (!queue.isEmpty()) {
                long p = queue.poll();
                BlockdustryPowerNode node = byPos.get(p);
                if (node != null) {
                    visitor.accept(node);
                    for (BlockPos link : node.getPowerLinks()) {
                        if (visited.add(link.asLong())) queue.add(link.asLong());
                    }
                }
                List<Long> rev = reverse.get(p);
                if (rev != null) {
                    for (long r : rev) {
                        if (visited.add(r)) queue.add(r);
                    }
                }
            }
        }
    }
}
