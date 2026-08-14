package com.blockdustry.building;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blockdustry.power.BlockdustryPowerNode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

// 爆破钻头方块实体（Mindustry blast-drill DrillBuild 移植，Blocks.java L2900-2919 + Drill.java 机制）喵。
// 原版参数：drillTime=280、tier=5、size=4、drawRim=true、hasPower、updateEffect=Fx.pulverizeRed(概率0.03)、
//   drillEffect=Fx.mineHuge(概率0.02)、rotateSpeed=6、warmupSpeed=0.01、itemCapacity=20、liquidBoostIntensity=1.8、
//   consumePower(3f)。Blockdustry 无液体管线 → 水 boost 不启用（speed=efficiency=电网满足率）喵。
// 机制移植与 laser-drill（并行 agent，直接上位）对齐：raw drillTime + hardness、warmup 预热、ItemParticleOption 粒子喵
public class BlastDrillBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    // —— 原版参数（Blocks.java L2900-2919 / Drill.java 字段）喵 ——
    public static final float DRILL_TIME = 280f;            // blast-drill drillTime=280 喵
    public static final int TIER = 5;                       // tier=5：可挖全部矿石（硬度≤5）喵
    public static final float HARDNESS_MULT = 50f;          // Drill.hardnessDrillMultiplier=50 喵
    public static final float WARMUP_SPEED = 0.01f;         // blast-drill warmupSpeed=0.01（激光 0.015、气动默认 0.015）喵
    public static final float ROTATE_SPEED = 6f;            // blast-drill rotateSpeed=6 喵
    public static final float POWER_NEEDED = 3f;            // consumePower(3f) 喵
    public static final float UPDATE_EFFECT_CHANCE = 0.03f; // blast-drill updateEffectChance=0.03（pulverizeRed）喵
    public static final float DRILL_EFFECT_CHANCE = 0.02f;  // Drill.drillEffectChance=0.02（mineHuge）喵
    private static final int ITEM_CAPACITY = 20;            // blast-drill itemCapacity=20 喵
    private static final int DUMP_INTERVAL = 4;             // Mindustry Building 默认 dumpTime=4（与 laser 一致）喵
    private static final int SIZE = 4;                      // blast-drill size=4 喵

    // 原版 Fx.pulverizeRed 颜色：Pal.redDust=#ffa480（MC Dust 粒子近似，红色碎屑）喵
    private static final Vector3f RED_DUST = new Vector3f(0xff / 255f, 0xa4 / 255f, 0x80 / 255f);

    private float progress;      // 挖掘进度（原版 DrillBuild.progress）喵
    private float warmup;        // 预热 0..1（原版 warmup，驱动钻速/钻头转速/rim 光晕）喵
    private float timeDrilled;   // 累计运转时间（原版 timeDrilled，驱动钻头旋转角度）喵
    private float powerStatus;   // 电网满足率 0..1（BlockdustryPowerNode）喵
    private int dumpTimer;       // 卸货轮询节拍喵
    private float lastSyncedSpin = -1f; // 客户端旋转同步游标（阈值发包，laser 同款）喵

    private Item dominantItem;   // 当前主要采的矿（原版 dominantItem）喵
    private int dominantCount;   // 该矿在 4×4 下方的格数（原版 dominantItems）喵

    public BlastDrillBlockEntity(BlockPos pos, BlockState state) {
        super(BlastDrillRegistrar.BLAST_DRILL_ENTITY.get(), pos, state);
    }

    public BlastDrillBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 渲染器读：钻头旋转角（原版 Drawf.spinSprite(rotator, x, y, timeDrilled * rotateSpeed)）喵
    public float getSpin() {
        return timeDrilled;
    }

    public float getWarmup() {
        return warmup;
    }

    public float getRotateSpeed() {
        return ROTATE_SPEED;
    }

    // 渲染器读：当前开采矿物（染 mine item 层）喵
    public Item getDominantItem() {
        return dominantItem;
    }

    // —— 库存容量 20（原版 itemCapacity=20；基类私有 itemCapacity=10 且 isFull 直接用，需一并覆写）喵
    @Override
    public int getCapacity() {
        return ITEM_CAPACITY;
    }

    @Override
    public boolean isFull() {
        return getStoredCount() >= ITEM_CAPACITY;
    }

    // —— 钻机机制：忠实移植 Drill.updateTile()（与 laser-drill 对齐）——
    @Override
    protected void tickAnchor() {
        // 1. 卸库存：把存量卸给相邻可接收建筑（原版 dump 轮询）喵
        if (++dumpTimer >= DUMP_INTERVAL) {
            dumpTimer = 0;
            Item stored = getStoredItem();
            if (stored != null && getStoredCount() > 0 && dumpItem(stored)) {
                removeOne();
            }
        }

        // 2. 统计 4×4 占地下方矿石（countOre：dominantItem + dominantCount）喵
        countOre();
        if (dominantItem == null) {
            warmup = decay(warmup);
            syncSpin();
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

            // updateEffect = Fx.pulverizeRed：红色碎屑（原版 chance * warmup，位置在建筑中心 ±size*2 单位=1 格）喵
            if (level instanceof ServerLevel sl && level.random.nextFloat() < UPDATE_EFFECT_CHANCE * warmup) {
                spawnPulverizeRed(sl);
            }

            // 3. 产矿：progress 攒满 delay 后一次产出 amount 个（offload 优先卸邻居，否则入库存）喵
            if (progress >= delay && !isFull()) {
                int amount = (int) (progress / delay);
                progress %= delay;
                for (int i = 0; i < amount && !isFull(); i++) {
                    if (!dumpItem(dominantItem)) {
                        storeItem(dominantItem);
                    }
                }
                // drillEffect = Fx.mineHuge：产矿时矿石色大碎屑（原版 chance * warmup）喵
                if (level instanceof ServerLevel sl && level.random.nextFloat() < DRILL_EFFECT_CHANCE * warmup) {
                    spawnMineHuge(sl, dominantItem);
                }
            }
        } else {
            warmup = decay(warmup);
        }

        syncSpin();
    }

    // 统计 size×size 下方矿石，取数量最多的作为 dominant（Mindustry Drill.countOre 语义：多格同一矿加速挖）喵
    private void countOre() {
        BlockPos base = hasAnchor() ? getAnchor() : worldPosition;
        Map<Item, Integer> counts = new HashMap<>();
        for (int dx = 0; dx < SIZE; dx++) {
            for (int dz = 0; dz < SIZE; dz++) {
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

    // MC 矿石 → Mindustry 硬度体系映射（依据 Ores/Items 原版硬度：铜 1、煤/铅 2、钛 3、钍 4；blast tier5 全采）喵
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

    // Fx.pulverizeRed（40帧 5 个 redDust→stoneGray 小方块）→ MC 红色 Dust 粒子（原点色 #ffa480）喵
    private void spawnPulverizeRed(ServerLevel sl) {
        BlockPos c = center();
        for (int i = 0; i < 5; i++) {
            double ang = level.random.nextDouble() * Math.PI * 2.0;
            double dist = (3.0 + level.random.nextDouble() * 8.0) / 8.0; // Mindustry 单位 → 格（tilesize=8）
            sl.sendParticles(new DustParticleOptions(RED_DUST, 1.0f),
                    c.getX() + 0.5 + Math.cos(ang) * dist,
                    c.getY() + 0.4,
                    c.getZ() + 0.5 + Math.sin(ang) * dist,
                    1, 0, 0.02, 0, 0.05);
        }
    }

    // Fx.mineHuge（40帧 8 个矿石色→灰白大方块）→ MC ITEM_CRACK 携带矿石物品（颜色即矿石本色，laser 同款）喵
    private void spawnMineHuge(ServerLevel sl, Item ore) {
        BlockPos c = center();
        sl.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ore)),
                c.getX() + 0.5, c.getY() + 0.5, c.getZ() + 0.5,
                8, 0.5, 0.2, 0.5, 0.03);
    }

    // 4×4 建筑中心（锚点 +2,+2）喵
    private BlockPos center() {
        BlockPos base = hasAnchor() ? getAnchor() : worldPosition;
        return base.offset(SIZE / 2, 0, SIZE / 2);
    }

    // 矿石 → Mindustry 物品色近似（raw_* 用 MC 物品外观近似色，未命中灰白）；渲染器 mine item tint 与客户端共用喵
    public static float[] oreColor(Item item) {
        if (item == Items.RAW_IRON) return hex("d8d7d8");
        if (item == Items.RAW_COPPER) return hex("d9815f");
        if (item == Items.RAW_GOLD) return hex("fcee4b");
        if (item == Items.COAL) return hex("2a2a2a");
        if (item == Items.DIAMOND) return hex("4aedd9");
        if (item == Items.EMERALD) return hex("17dd62");
        if (item == Items.REDSTONE) return hex("ff0303");
        if (item == Items.LAPIS_LAZULI) return hex("2a4dc8");
        return hex("c0c0c0");
    }

    private static float[] hex(String s) {
        int v = Integer.parseInt(s, 16);
        return new float[]{((v >> 16) & 0xFF) / 255f, ((v >> 8) & 0xFF) / 255f, (v & 0xFF) / 255f};
    }

    // 客户端钻头旋转/矿物同步：timeDrilled 变化超阈值发一次更新包（渲染器读 getSpin/getDominantItem）喵
    private void syncSpin() {
        if (Math.abs(timeDrilled - lastSyncedSpin) > 0.5f) {
            lastSyncedSpin = timeDrilled;
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    // —— BlockdustryPowerNode：耗电 3（原版 consumePower(3f)），产/存 0 喵 ——
    @Override public float powerProduction() { return 0f; }
    @Override public float powerCapacity() { return 0f; }
    @Override public float powerStored() { return 0f; }
    @Override public float getPowerStatus() { return powerStatus; }
    @Override public void setPowerStatus(float status) { this.powerStatus = status; }

    @Override
    public float powerNeeded() {
        // 仅锚点格计入耗电：4×4 共 16 格 BE 都会进电网结算，非锚点格返回 0，避免耗电被计 16 次喵
        return isAnchor() ? POWER_NEEDED : 0f;
    }

    @Override
    public java.util.List<BlockPos> getPowerLinks() {
        // 非锚点格把自己并入锚点所在电网（与 UnitFactory 一致），保证整座建筑同网喵
        if (isAnchor()) return List.of();
        BlockPos anchor = getAnchor();
        return anchor != null ? List.of(anchor) : List.of();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("bd_progress", progress);
        tag.putFloat("bd_warmup", warmup);
        tag.putFloat("bd_spin", timeDrilled);
        tag.putString("bd_dominant", dominantItem == null
                ? "" : String.valueOf(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(dominantItem)));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getFloat("bd_progress");
        warmup = tag.getFloat("bd_warmup");
        timeDrilled = tag.getFloat("bd_spin");
        String s = tag.getString("bd_dominant");
        if (!s.isEmpty()) {
            Item it = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.tryParse(s));
            if (it != null && it != Items.AIR) dominantItem = it;
        }
    }
}
