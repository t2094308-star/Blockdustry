package com.blockdustry.building;

import com.blockdustry.item.BlockdustryItems;
import com.blockdustry.logistics.BlockdustryItemSource;
import com.blockdustry.power.BlockdustryPowerNode;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 相织布编织器（Mindustry phase-weaver，GenericCrafter size 2）：吃 4 钍 + 10 沙产 1 相织布，craftTime=120tick，
// 各物品独立容量 30（itemCapacity=30），耗电 5/s（consumePower(5f)）喵。
// 带 warmup 预热 + totalProgress 织机旋转累积（驱动 DrawWeave 织纹旋转/梭线扫描）+ craft 白色烟尘粒子（Fx.smeltsmoke）喵。
// 参考模板：SiliconSmelterBlockEntity（GenericCrafter 耗电工厂）喵
public class PhaseWeaverBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    private static final int CRAFT_TIME = 120;          // Mindustry phase-weaver craftTime = 120f tick 喵
    private static final int THORIUM_INPUT = 4;         // consumeItems(with(Items.thorium, 4, ...)) 喵
    private static final int SAND_INPUT = 10;           // consumeItems(with(..., Items.sand, 10)) 喵
    private static final int CAPACITY = 30;             // Mindustry itemCapacity = 30（各类型独立上限）喵
    private static final float WARMUP_SPEED = 0.019f;   // Mindustry GenericCrafter warmupSpeed 喵
    private static final float POWER_NEEDED = 5f;       // Mindustry phase-weaver consumePower(5f) 喵
    // Mindustry totalProgress 每秒 +warmup×60（60tps）；本 mod 服务端 20tps → 每 tick +warmup×3，速率与原版一致 60/s 喵
    private static final float TOTAL_PROGRESS_PER_TICK = 3f;

    private int thoriumCount;
    private int sandCount;
    private int phaseFabricCount;
    private float craftProgress;
    private float warmup;              // 0..1 预热，驱动织机梭线 alpha（DrawWeave Draw.alpha(warmup)）喵
    private float totalProgress;       // 织机旋转角累积（DrawWeave 旋转角 + Mathf.sin 扫描相位）喵
    private long smokeStartGameTime = -1; // 最近一次 craft 完成的服务端游戏时刻（Fx.smeltsmoke 起点）喵
    private long totalProgressSyncGameTime; // 客户端外推起点（最近一次同步时的 gameTime）喵
    private float lastSyncedWarmup = -1f;
    private float lastSyncedTotalProgress = -1f;
    private float powerStatus;         // 电网满足率 0..1（由 PowerGridManager 结算注入）喵

    public PhaseWeaverBlockEntity(BlockPos pos, BlockState state) {
        super(PhaseWeaverRegistrar.PHASE_WEAVER_ENTITY.get(), pos, state);
    }

    public PhaseWeaverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // —— 渲染/Jade 用 getter ——
    public float getWarmup() {
        return warmup;
    }

    public float getCraftProgress() {
        return craftProgress;
    }

    public float getTotalProgress() {
        return totalProgress;
    }

    public long getTotalProgressSyncGameTime() {
        return totalProgressSyncGameTime;
    }

    public long getSmokeStartGameTime() {
        return smokeStartGameTime;
    }

    public int getThoriumCount() {
        return thoriumCount;
    }

    public int getSandCount() {
        return sandCount;
    }

    public int getPhaseFabricCount() {
        return phaseFabricCount;
    }

    // 接收判定：只收钍/沙，且各自库存未满（各类型独立上限 CAPACITY）喵
    @Override
    public boolean acceptsItem(Item item) {
        if (item == BlockdustryItems.THORIUM.get()) return thoriumCount < CAPACITY;
        if (item == Items.SAND) return sandCount < CAPACITY;
        return false;
    }

    @Override
    public boolean acceptItem(BlockdustryItemSource source, Item item) {
        if (source == null || item == null) return false;
        if (!getTeam().canInteract(source.getTeam())) return false;
        // 非锚点格转发给锚点格（统一库存，craft 只在锚点格跑）喵
        if (!isAnchor()) {
            BlockEntity anchorBe = level.getBlockEntity(getAnchor());
            return anchorBe instanceof PhaseWeaverBlockEntity anchor && anchor.acceptItem(source, item);
        }
        return acceptsItem(item);
    }

    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        // 非锚点格转发给锚点格喵
        if (!isAnchor()) {
            BlockEntity anchorBe = level.getBlockEntity(getAnchor());
            return anchorBe instanceof PhaseWeaverBlockEntity anchor && anchor.handleItem(source, item);
        }
        if (!acceptItem(source, item)) return false;
        if (item == BlockdustryItems.THORIUM.get()) {
            thoriumCount++;
        } else if (item == Items.SAND) {
            sandCount++;
        }
        setChanged();
        return true;
    }

    // 生产：耗电 + 料足（钍4沙10）+ 输出未满 才推进；攒满 120tick 扣料产相织布 + 触发冒烟喵
    @Override
    protected void tickAnchor() {
        // 先把产出的相织布卸给相邻传送带喵
        if (phaseFabricCount > 0 && dumpItem(BlockdustryItems.PHASE_FABRIC.get())) {
            phaseFabricCount--;
            setChanged();
        }
        // 无电即停摆：powerStatus<=0.01 视为断电（SiliconSmelter 同款）喵
        boolean hasPower = getPowerStatus() > 0.01f;
        boolean producing = hasPower && thoriumCount >= THORIUM_INPUT
                && sandCount >= SAND_INPUT && phaseFabricCount < CAPACITY;
        // warmup 预热：可生产爬升、否则衰减（Mindustry approachDelta 0.019/tick）喵
        warmup = producing ? Math.min(1f, warmup + WARMUP_SPEED) : Math.max(0f, warmup - WARMUP_SPEED);
        // totalProgress 累计（DrawWeave 织纹旋转 + 梭线扫描相位），速率 = warmup×3/MC tick = warmup×60/s 喵
        totalProgress += warmup * TOTAL_PROGRESS_PER_TICK;
        if (producing) {
            craftProgress += 1f / CRAFT_TIME;
            if (craftProgress >= 1f) {
                craftProgress = 0f;
                thoriumCount -= THORIUM_INPUT;
                sandCount -= SAND_INPUT;
                phaseFabricCount++;
                // Fx.smeltsmoke：craft 完成时在建筑中心冒烟，记录起点并立即同步客户端喵
                smokeStartGameTime = level.getGameTime();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
                setChanged();
            }
        }
        // warmup/totalProgress 变化超阈值时同步客户端（织纹旋转/梭线/冒烟起点需要读到）喵
        if (Math.abs(warmup - lastSyncedWarmup) > 0.02f || Math.abs(totalProgress - lastSyncedTotalProgress) > 1.0f) {
            lastSyncedWarmup = warmup;
            lastSyncedTotalProgress = totalProgress;
            totalProgressSyncGameTime = level.getGameTime();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
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
        // 仅锚点格计入耗电：2×2 共 4 格 BE 都会进电网结算，非锚点格返回 0，避免耗电被计 4 次喵
        return isAnchor() ? POWER_NEEDED : 0f;
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
    public List<BlockPos> getPowerLinks() {
        // 非锚点格把自己并入锚点所在电网（SiliconSmelter 同款）喵
        if (isAnchor()) return List.of();
        BlockPos anchor = getAnchor();
        return anchor != null ? List.of(anchor) : List.of();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("bd_weaver_thorium", thoriumCount);
        tag.putInt("bd_weaver_sand", sandCount);
        tag.putInt("bd_weaver_fabric", phaseFabricCount);
        tag.putFloat("bd_weaver_progress", craftProgress);
        tag.putFloat("bd_weaver_warmup", warmup);
        tag.putFloat("bd_weaver_total_progress", totalProgress);
        tag.putLong("bd_weaver_smoke_start", smokeStartGameTime);
        tag.putFloat("bd_weaver_power", powerStatus);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        thoriumCount = tag.getInt("bd_weaver_thorium");
        sandCount = tag.getInt("bd_weaver_sand");
        phaseFabricCount = tag.getInt("bd_weaver_fabric");
        craftProgress = tag.getFloat("bd_weaver_progress");
        warmup = tag.getFloat("bd_weaver_warmup");
        totalProgress = tag.getFloat("bd_weaver_total_progress");
        smokeStartGameTime = tag.getLong("bd_weaver_smoke_start");
        powerStatus = tag.getFloat("bd_weaver_power");
        // 客户端收到更新/加载时重置外推起点，渲染器从此刻按 warmup×3 外推织机旋转喵。
        // 服务端磁盘加载也设置一次（渲染不在服务端跑，无害）；客户端 chunk/更新包加载都走这里喵
        if (level != null) {
            totalProgressSyncGameTime = level.getGameTime();
        }
    }
}
