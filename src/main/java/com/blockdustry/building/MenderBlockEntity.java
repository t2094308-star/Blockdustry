package com.blockdustry.building;

import java.util.ArrayList;
import java.util.List;

import com.blockdustry.lib.BlockHealthApi;
import com.blockdustry.logistics.BlockdustryItemSource;
import com.blockdustry.power.BlockdustryPowerNode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 修理器（Mindustry mender，MendProjector size 1）：范围维修场，每 reload=200 tick 修一次范围内受损建筑。
// 数据忠于原版 Blocks.java L1919 + MendProjector.java：reload=200、range=40（=5 格）、healPercent=4%、phaseBoost=4、
// phaseRangeBoost=20、useTime=400、耗电 0.3、吃硅 boost（optionalEfficiency→phaseHeat）、itemCapacity 10、
// baseColor #84f491（渲染用）。维修触发 Fx.healBlockFull（目标块维修色闪烁 20 tick）喵。
// 机制：遍历本模组已加载建筑（BlockdustryBuildings.all），同队 + 受损（BlockHealthApi.getHpFraction<1）+ 在范围内 →
// BlockHealthApi.heal 回血，并记录最近维修批次（位置+时刻）同步给客户端渲染特效喵
public class MenderBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    private static final float RELOAD = 200f;            // Mindustry mender reload=200f 喵
    private static final float RANGE = 40f / 8f;         // Mindustry range=40 单位 → 5 格喵
    private static final float HEAL_PERCENT = 4f;        // Mindustry healPercent=4f 喵
    private static final float PHASE_BOOST = 4f;         // Mindustry phaseBoost=4f 喵
    private static final float PHASE_RANGE_BOOST = 20f / 8f; // Mindustry phaseRangeBoost=20 单位 → 2.5 格喵
    private static final float USE_TIME = 400f;          // Mindustry useTime=400f（吃硅周期）喵
    private static final float POWER_NEEDED = 0.3f;      // Mindustry consumePower(0.3f) 喵
    private static final int CAPACITY = 10;              // Mindustry Building 默认 itemCapacity 10 喵
    private static final int REPAIR_EFFECT_LIFE = 20;    // Fx.healBlockFull 寿命 20 tick 喵
    private static final int MAX_TARGETS_SYNC = 8;       // 单批次同步最多目标数，避免 NBT 膨胀喵

    private float charge;               // 0..reload 充能（原版 charge，初始随机）喵
    private float phaseHeat;            // 0..1 有硅时爬升，范围/效率加成喵
    private float powerStatus;          // 电网满足率 0..1（由 PowerGridManager 注入）喵
    private float heatForRender;        // 渲染热值 0..1（驱动顶部呼吸与方框脉冲）喵
    private long repairStartGameTime = -1;      // 最近维修批次起点（服务端游戏时刻，渲染用）喵
    private final List<BlockPos> repairTargets = new ArrayList<>(); // 最近维修批次目标（渲染用）喵
    private float lastSyncedHeat = -1f; // 同步游标，heat 变化超过阈值才发包喵

    public MenderBlockEntity(BlockPos pos, BlockState state) {
        super(MenderRegistrar.MENDER_ENTITY.get(), pos, state);
        charge = (float) Math.random() * RELOAD; // 原版 Mathf.random(reload) 喵
    }

    public MenderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        charge = (float) Math.random() * RELOAD;
    }

    // —— 渲染/Jade 用 getter ——
    public float getHeat() {
        return heatForRender;
    }

    public float getPhaseHeat() {
        return phaseHeat;
    }

    public long getRepairStartGameTime() {
        return repairStartGameTime;
    }

    public List<BlockPos> getRepairTargets() {
        return repairTargets;
    }

    public int getRepairEffectLife() {
        return REPAIR_EFFECT_LIFE;
    }

    // 接收判定：只收硅（Mindustry consumeItem(silicon).boost()）喵
    @Override
    public boolean acceptsItem(Item item) {
        return item == BlockdustryBlocks.SILICON.get() && !isFull();
    }

    // —— 范围维修主逻辑（仅锚点格 tick）喵
    @Override
    protected void tickAnchor() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        boolean hasPower = getPowerStatus() > 0.01f;
        // 原版 heat = lerpDelta(heat, efficiency>0 && canHeal ? 1:0, 0.08)喵
        heatForRender = approach(heatForRender, hasPower ? 1f : 0f, 0.08f);
        if (hasPower) {
            charge += heatForRender; // 原版 charge += heat * delta()，delta=1 喵
        }
        // phaseHeat：有硅才爬升（原版 optionalEfficiency）喵
        boolean hasPhase = getStoredCount() > 0;
        phaseHeat = approach(phaseHeat, hasPhase ? 1f : 0f, 0.1f);
        // 每 useTime=400 tick 吃一块硅（原版 timer(timerUse, useTime/timeScale)）喵
        if (hasPhase && hasPower && serverLevel.getGameTime() % (long) USE_TIME == 0) {
            removeOne();
        }
        // 充能满且供电：修范围内受损建筑喵
        if (charge >= RELOAD && hasPower) {
            charge = 0f;
            repairTargets.clear();
            boolean any = false;
            float realRange = RANGE + phaseHeat * PHASE_RANGE_BOOST;
            BlockPos center = hasAnchor() ? getAnchor() : worldPosition;
            for (BlockdustryBuildingEntity b : BlockdustryBuildings.all()) {
                if (b == null || b == this || b.isRemoved() || b.getLevel() != level) continue;
                if (!getTeam().canInteract(b.getTeam())) continue; // 只修自己队伍喵
                if (BlockHealthApi.getHpFraction(serverLevel, b.getBlockPos()) >= 1f) continue; // 未受损喵
                BlockPos bBase = b.hasAnchor() ? b.getAnchor() : b.getBlockPos();
                double dist = Math.sqrt(distSqCenter(center, bBase, b.getSize()));
                if (dist > realRange + 0.01) continue; // 范围内喵
                // 修复量 = maxHealth × (healPercent + phaseHeat×phaseBoost)/100 × efficiency 喵
                float maxHp = BlockHealthApi.getMaxHp(serverLevel, b.getBlockPos());
                float heal = maxHp * (HEAL_PERCENT + phaseHeat * PHASE_BOOST) / 100f * getPowerStatus();
                if (heal <= 0f) continue;
                BlockHealthApi.heal(serverLevel, b.getBlockPos(), heal);
                if (repairTargets.size() < MAX_TARGETS_SYNC) {
                    repairTargets.add(b.getBlockPos().immutable());
                }
                any = true;
            }
            if (any) {
                repairStartGameTime = serverLevel.getGameTime();
                syncClient();
            }
        }
        // heat 变化超阈值时同步（顶部呼吸动画）喵
        if (Math.abs(heatForRender - lastSyncedHeat) > 0.02f) {
            lastSyncedHeat = heatForRender;
            syncClient();
        }
    }

    // 两建筑中心（锚点 + 半边长）水平距离平方喵
    private static double distSqCenter(BlockPos a, BlockPos b, int sizeB) {
        double ax = a.getX() + 0.5, az = a.getZ() + 0.5;
        double bx = b.getX() + sizeB / 2.0, bz = b.getZ() + sizeB / 2.0;
        double dx = ax - bx, dz = az - bz;
        return dx * dx + dz * dz;
    }

    // 趋近：current += (target-current)×rate（Mindustry Mathf.lerpDelta, delta=1）喵
    private static float approach(float current, float target, float rate) {
        return current + (target - current) * Math.min(1f, rate);
    }

    private void syncClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // —— BlockdustryPowerNode ——
    @Override
    public BlockPos getPos() {
        return worldPosition;
    }

    @Override
    public float powerProduction() {
        return 0f;
    }

    @Override
    public float powerNeeded() {
        return POWER_NEEDED;
    }

    @Override
    public float powerCapacity() {
        return 0f;
    }

    @Override
    public float powerStored() {
        return 0f;
    }

    @Override
    public float getPowerStatus() {
        return powerStatus;
    }

    @Override
    public void setPowerStatus(float status) {
        this.powerStatus = status;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("bd_mender_charge", charge);
        tag.putFloat("bd_mender_phase", phaseHeat);
        tag.putFloat("bd_mender_heat", heatForRender);
        tag.putFloat("bd_mender_power", powerStatus);
        tag.putLong("bd_mender_repair_start", repairStartGameTime);
        ListTag list = new ListTag();
        for (BlockPos p : repairTargets) {
            list.add(NbtUtils.writeBlockPos(p));
        }
        tag.put("bd_mender_targets", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        charge = tag.getFloat("bd_mender_charge");
        phaseHeat = tag.getFloat("bd_mender_phase");
        heatForRender = tag.getFloat("bd_mender_heat");
        powerStatus = tag.getFloat("bd_mender_power");
        repairStartGameTime = tag.getLong("bd_mender_repair_start");
        repairTargets.clear();
        ListTag list = tag.getList("bd_mender_targets", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            repairTargets.add(new BlockPos((int) c.getLong("X"), (int) c.getLong("Y"), (int) c.getLong("Z")));
        }
    }
}
