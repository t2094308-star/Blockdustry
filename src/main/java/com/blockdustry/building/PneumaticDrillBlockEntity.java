package com.blockdustry.building;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

// 气动钻头（Mindustry pneumatic-drill）方块实体：mechanical-drill 的上位进阶钻机，size 2。
// 原版数据：tier=3、drillTime=400、size=2、耗水 3.5/60·s boost、造价 铜×18+石墨×10（Blocks.java L2878-2885）喵
//
// 机制与动画结构沿用现有 DrillBlockEntity（mechanical-drill 模板，勿改它），差异点：
//  - 钻速按原版 drillTime 比例：mechanical 600 帧 → 现模 40 tick；pneumatic 400 帧 → 27 tick（约 1.5 倍速）喵
//  - 带 warmup 预热（Drill.warmupSpeed=0.015），驱动旋转/粒子动画，可随渲染器读到喵
//  - 动画/粒子：updateEffect=pulverizeSmall（灰尘，钻时随机）、drillEffect=Fx.mine（矿色尘，出矿时）、
//    rotator/top/item 由渲染器叠加原版 PNG（见 PneumaticDrillBlockEntityRenderer）喵
//  - 原版 consumeLiquid(water 3.5/60).boost：mod 无液体系统，未实现（见整合清单「已知差异」）喵
public class PneumaticDrillBlockEntity extends BlockdustryBuildingEntity {
    // 采集进度阈值（mod tick）：机械 600 帧→40 tick；气动 400 帧→400/600*40≈26.67→27 tick，忠实相对钻速喵
    private static final int DRILL_TIME = 27;
    // Mindustry Drill.warmupSpeed 默认 0.015（每 tick 向目标爬升/衰减）喵
    private static final float WARMUP_SPEED = 0.015f;

    private float progress;
    private float warmup;
    private float lastSyncedWarmup = -1f; // warmup 同步游标（变化超阈值才发包，GraphitePress 同款）喵

    public PneumaticDrillBlockEntity(BlockPos pos, BlockState state) {
        super(PneumaticDrillRegistrar.PNEUMATIC_DRILL_ENTITY.get(), pos, state);
    }

    public PneumaticDrillBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 渲染器/Jade 读：预热值 0..1，驱动钻头转速与粒子频率喵
    public float getWarmup() {
        return warmup;
    }

    // 渲染器读：当前正在开采的矿石（客户端算 dominant，服务端 tick 缓存一致）喵
    public Item getMinedOre() {
        return detectDominantOre();
    }

    // 每次模组 tick：先卸库存，再扫下方矿石（无矿/满停摆），挖对应矿石并产出（优先 offload）喵
    @Override
    protected void tickAnchor() {
        if (getStoredCount() > 0 && getStoredItem() != null && dumpItem(getStoredItem())) {
            removeOne();
        }
        Item ore = detectDominantOre();
        if (ore == null) {
            warmup = Math.max(0f, warmup - WARMUP_SPEED);
            progress = 0f;
            syncWarmup();
            return;
        }
        if (isFull()) {
            warmup = Math.max(0f, warmup - WARMUP_SPEED);
            syncWarmup();
            return;
        }
        // 预热爬升（Drill.approachDelta(warmup, speed=1, 0.015)）喵
        warmup = Math.min(1f, warmup + WARMUP_SPEED);
        progress += warmup;
        // updateEffect = pulverizeSmall：钻时随机灰尘（chance 0.02/tick * warmup，Mindustry updateEffectChance）喵
        if (level.random.nextFloat() < 0.02f * warmup) {
            spawnUpdateParticles(ore);
        }
        if (progress >= DRILL_TIME) {
            progress = 0f;
            if (!dumpItem(ore)) {
                storeItem(ore);
            }
            // drillEffect = Fx.mine：出矿时矿色尘（GraphitePress 同款「每产出播一次」惯例，保证可见）喵
            spawnMineParticles(ore);
        }
        syncWarmup();
    }

    // 扫 2×2 占地正下方，统计各矿石数量，取数量最多的为 dominant（Mindustry Drill.countOre 语义；同数取先扫到者）喵
    private Item detectDominantOre() {
        BlockPos base = hasAnchor() ? getAnchor() : worldPosition;
        int size = getSize();
        Map<Item, Integer> counts = new HashMap<>();
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                Item ore = DrillBlockEntity.oreResult(level.getBlockState(base.offset(dx, -1, dz)));
                if (ore != null) {
                    counts.merge(ore, 1, Integer::sum);
                }
            }
        }
        if (counts.isEmpty()) return null;
        Item best = null;
        int bestCount = -1;
        for (Map.Entry<Item, Integer> e : counts.entrySet()) {
            if (e.getValue() > bestCount) {
                best = e.getKey();
                bestCount = e.getValue();
            }
        }
        return best;
    }

    // updateEffect pulverizeSmall → 灰 DustParticleOptions：钻头占地范围内随机偏移喵
    private void spawnUpdateParticles(Item ore) {
        if (level instanceof ServerLevel serverLevel) {
            BlockPos base = hasAnchor() ? getAnchor() : worldPosition;
            serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.56f, 0.56f, 0.56f), 1.5f),
                    base.getX() + 1.0 + (level.random.nextDouble() * 2.0 - 1.0),
                    base.getY() + 0.5,
                    base.getZ() + 1.0 + (level.random.nextDouble() * 2.0 - 1.0),
                    3, 0.2, 0.2, 0.2, 0.1);
        }
    }

    // drillEffect Fx.mine → 矿色 DustParticleOptions：出矿时向外散 6 粒（颜色取 MC 矿石物品本色）喵
    private void spawnMineParticles(Item ore) {
        if (level instanceof ServerLevel serverLevel) {
            int[] rgb = mineColor(ore);
            BlockPos base = hasAnchor() ? getAnchor() : worldPosition;
            serverLevel.sendParticles(new DustParticleOptions(new Vector3f(rgb[0] / 255f, rgb[1] / 255f, rgb[2] / 255f), 2.0f),
                    base.getX() + 1.0, base.getY() + 0.5, base.getZ() + 1.0,
                    6, 0.5, 0.4, 0.5, 0.15);
        }
    }

    // 矿石物品 → MC 本色 RGB（0-255）：渲染器 tint 与粒子颜色共用，防两端色差喵
    public static int[] mineColor(Item ore) {
        if (ore == Items.RAW_IRON) return new int[]{216, 175, 147};
        if (ore == Items.RAW_COPPER) return new int[]{201, 138, 91};
        if (ore == Items.RAW_GOLD) return new int[]{253, 245, 95};
        if (ore == Items.COAL) return new int[]{30, 30, 30};
        if (ore == Items.DIAMOND) return new int[]{74, 237, 217};
        if (ore == Items.EMERALD) return new int[]{2, 252, 70};
        if (ore == Items.REDSTONE) return new int[]{255, 0, 60};
        if (ore == Items.LAPIS_LAZULI) return new int[]{22, 73, 197};
        return new int[]{200, 200, 200};
    }

    // warmup 变化超过阈值时同步客户端（渲染器/粒子需要 warmup，GraphitePress 同款）喵
    private void syncWarmup() {
        if (Math.abs(warmup - lastSyncedWarmup) > 0.02f) {
            lastSyncedWarmup = warmup;
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("bd_progress", progress);
        tag.putFloat("bd_warmup", warmup);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getFloat("bd_progress");
        warmup = tag.getFloat("bd_warmup");
    }
}
