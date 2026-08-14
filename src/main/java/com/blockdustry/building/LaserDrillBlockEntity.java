package com.blockdustry.building;

import java.util.HashMap;
import java.util.Map;

import com.blockdustry.power.BlockdustryPowerNode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// Mindustry「激光钻头」laser-drill 最小移植（size 3、tier 4、耗电）喵。
// 忠于原版 Blocks.java L2887-2898 + Drill.java 机制：drillTime=280、tier=4、consumePower(1.10f)、
// drillEffect=Fx.mineBig（产矿矿石色碎屑）、updateEffect=Fx.pulverizeMedium（工作灰色粉尘）、
// 无液体 boost（consumeLiquid 的水 boost 本轮不做，见整合清单）喵。
// 原版当前版本 laser-drill 无独立激光光束绘制，视觉 = 底座（模型顶面 laser_drill.png 含紫色激光能量装饰）+ rotator 旋转 + top 盖喵
public class LaserDrillBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    // —— 原版参数（Blocks.java L2887-2898 / Drill.java 字段）喵 ——
    public static final float DRILL_TIME = 280f;            // laser-drill drillTime=280 喵
    public static final int TIER = 4;                       // tier=4：可挖硬度≤4 的矿（钍 4/钛 3/煤铅 2/铜铅 1）喵
    public static final float HARDNESS_MULT = 50f;          // Drill.hardnessDrillMultiplier=50 喵
    public static final float WARMUP_SPEED = 0.015f;        // Drill.warmupSpeed=0.015 喵
    public static final float ROTATE_SPEED = 2f;            // Drill.rotateSpeed=2 喵
    public static final float POWER_NEEDED = 1.10f;         // consumePower(1.10f) 喵
    public static final float UPDATE_EFFECT_CHANCE = 0.02f; // Drill.updateEffectChance=0.02 喵
    public static final float DRILL_EFFECT_CHANCE = 0.02f;  // Drill.drillEffectChance=0.02 喵
    private static final int DUMP_INTERVAL = 4;             // Mindustry Building 默认 dumpTime=4 喵

    private float progress;      // 挖掘进度（原版 DrillBuild.progress）喵
    private float warmup;        // 预热 0..1（原版 warmup，驱动钻速/钻头转速）喵
    private float timeDrilled;   // 累计运转时间（原版 timeDrilled，驱动钻头旋转角度）喵
    private float powerStatus;   // 电网满足率 0..1（BlockdustryPowerNode）喵
    private int dumpTimer;       // 卸货轮询节拍喵
    private float lastSyncedSpin = -1f; // 客户端旋转同步游标（阈值发包）喵

    private Item dominantItem;   // 当前主要采的矿（原版 dominantItem）喵
    private int dominantCount;   // 该矿在 3×3 下方的格数（原版 dominantItems）喵

    public LaserDrillBlockEntity(BlockPos pos, BlockState state) {
        super(LaserDrillRegistrar.LASER_DRILL_ENTITY.get(), pos, state);
    }

    // 钻头旋转角（供渲染器）：原版 Drawf.spinSprite(rotator, x, y, timeDrilled * rotateSpeed) 喵
    public float getSpin() {
        return timeDrilled;
    }

    public float getWarmup() {
        return warmup;
    }

    // 渲染器读：当前开采矿物（染 mine item 矿团层，参考 BlastDrillBlockEntity/PneumaticDrillBlockEntity）喵
    public Item getDominantItem() {
        return dominantItem;
    }

    // 每模组 tick：先卸库存，再统计下方矿石（无矿则停），有电则挖掘产出喵
    @Override
    protected void tickAnchor() {
        // 卸库存：把存量卸给相邻可接收的传送带/建筑（原版 dump 轮询）喵
        if (++dumpTimer >= DUMP_INTERVAL) {
            dumpTimer = 0;
            Item stored = getStoredItem();
            if (stored != null && getStoredCount() > 0 && dumpItem(stored)) {
                removeOne();
            }
        }

        countOre();
        if (dominantItem == null) {
            warmup = decay(warmup);
            return;
        }

        float delay = getDrillTime(dominantItem);
        boolean powered = getPowerStatus() > 0.01f;

        if (powered && !isFull() && dominantCount > 0) {
            // 原版 speed = lerp(1, liquidBoostIntensity, optionalEfficiency) * efficiency；无液体 boost → speed = efficiency喵
            float speed = getPowerStatus();
            warmup = approach(warmup, speed, WARMUP_SPEED);
            timeDrilled += warmup;
            progress += dominantCount * speed * warmup;

            // updateEffect = Fx.pulverizeMedium：工作时灰色粉尘（原版 chance * warmup）喵
            if (level instanceof ServerLevel sl && level.random.nextFloat() < UPDATE_EFFECT_CHANCE * warmup) {
                spawnPulverize(sl);
            }

            // 产矿（原版 progress >= delay 时 offload 若干）喵
            if (progress >= delay && !isFull()) {
                int amount = (int) (progress / delay);
                progress %= delay;
                for (int i = 0; i < amount && !isFull(); i++) {
                    if (!dumpItem(dominantItem)) {
                        storeItem(dominantItem);
                    }
                }
                // drillEffect = Fx.mineBig：产矿时矿石色碎屑（原版 chance * warmup）喵
                if (level instanceof ServerLevel sl && level.random.nextFloat() < DRILL_EFFECT_CHANCE * warmup) {
                    spawnMineBig(sl, dominantItem);
                }
            }
        } else {
            warmup = decay(warmup);
        }

        // 客户端钻头旋转同步：timeDrilled 变化超阈值发一次更新包（渲染器读 getSpin）喵
        if (Math.abs(timeDrilled - lastSyncedSpin) > 0.5f) {
            lastSyncedSpin = timeDrilled;
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    // 统计 size×size 下方矿石，取数量最多的作为 dominant（Mindustry Drill.countOre 语义：多格同一矿加速挖）喵
    private void countOre() {
        BlockPos base = hasAnchor() ? getAnchor() : worldPosition;
        Map<Item, Integer> counts = new HashMap<>();
        int size = getSize();
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                Item ore = DrillBlockEntity.oreResult(level.getBlockState(base.offset(dx, -1, dz)));
                if (ore != null) {
                    counts.merge(ore, 1, Integer::sum);
                }
            }
        }
        Item best = null;
        int bestCount = 0;
        for (Map.Entry<Item, Integer> e : counts.entrySet()) {
            if (e.getValue() > bestCount) {
                best = e.getKey();
                bestCount = e.getValue();
            }
        }
        dominantItem = best;
        dominantCount = bestCount;
    }

    // 原版 getDrillTime = (drillTime + hardnessDrillMultiplier * item.hardness) / drillMultiplier 喵
    private float getDrillTime(Item item) {
        return DRILL_TIME + HARDNESS_MULT * oreHardness(item);
    }

    // MC 矿石 → Mindustry 硬度体系映射（依据 Ores/Items 原版硬度：铜 1、煤/铅 2、钛 3、钍 4）喵
    private static int oreHardness(Item item) {
        if (item == Items.COAL) return 2;
        if (item == Items.RAW_IRON) return 2;    // 近铅（2）喵
        if (item == Items.RAW_GOLD) return 1;    // 金软喵
        if (item == Items.DIAMOND) return 3;     // 近钛（3）喵
        if (item == Items.EMERALD) return 3;     // 近钛（3）喵
        return 1;                                // 铜/红石/青金石喵
    }

    private static float approach(float cur, float target, float rate) {
        if (cur < target) return Math.min(cur + rate, target);
        return Math.max(cur - rate, target);
    }

    private float decay(float cur) {
        return Math.max(0f, cur - WARMUP_SPEED);
    }

    // Fx.pulverizeMedium（30帧 5 个 stoneGray 小方块）→ MC CRIT 灰色小方片粉尘喵
    private void spawnPulverize(ServerLevel sl) {
        double x = worldPosition.getX() + getSize() / 2.0 + 0.5;
        double y = worldPosition.getY() + 1.1;
        double z = worldPosition.getZ() + getSize() / 2.0 + 0.5;
        sl.sendParticles(ParticleTypes.CRIT, x, y, z, 5, 0.25, 0.05, 0.25, 0.01);
    }

    // Fx.mineBig（30帧 6 个矿石色→灰白小方块）→ MC ITEM_CRACK 携带矿石物品，颜色即矿石色喵
    private void spawnMineBig(ServerLevel sl, Item ore) {
        double x = worldPosition.getX() + getSize() / 2.0 + 0.5;
        double y = worldPosition.getY() + 1.1;
        double z = worldPosition.getZ() + getSize() / 2.0 + 0.5;
        sl.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ore)), x, y, z, 6, 0.4, 0.15, 0.4, 0.02);
    }

    // —— BlockdustryPowerNode：laser-drill 耗电 1.10（Mindustry consumePower(1.10f)），产/存 0 喵 ——
    @Override public float powerProduction() { return 0f; }
    @Override public float powerNeeded() { return POWER_NEEDED; }
    @Override public float powerCapacity() { return 0f; }
    @Override public float powerStored() { return 0f; }
    @Override public float getPowerStatus() { return powerStatus; }
    @Override public void setPowerStatus(float status) { this.powerStatus = status; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("bd_progress", progress);
        tag.putFloat("bd_warmup", warmup);
        tag.putFloat("bd_spin", timeDrilled);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getFloat("bd_progress");
        warmup = tag.getFloat("bd_warmup");
        timeDrilled = tag.getFloat("bd_spin");
    }
}
