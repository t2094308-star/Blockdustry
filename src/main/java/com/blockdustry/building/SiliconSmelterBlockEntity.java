package com.blockdustry.building;

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

// 硅冶炼厂（Mindustry silicon-smelter，GenericCrafter size 2）：吃 1 煤 + 2 沙产 1 硅，craftTime=40tick，耗电 0.5。
// 带 warmup 预热（驱动火焰动画）、craft 冒烟特效（Fx.smeltsmoke 15tick 白方烟团）与 dumpItem 输出喵。
// 参考模板：GraphitePressBlockEntity（GenericCrafter 先例）与 UnitFactoryBlockEntity（耗电工厂模式）喵
public class SiliconSmelterBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    private static final int CRAFT_TIME = 40;          // Mindustry silicon-smelter craftTime=40f 喵
    private static final int COAL_PER_CRAFT = 1;       // consumeItems(with(Items.coal, 1, ...)) 喵
    private static final int SAND_PER_CRAFT = 2;       // consumeItems(with(..., Items.sand, 2)) 喵
    private static final int CAPACITY = 10;            // Mindustry GenericCrafter itemCapacity 默认 10 喵
    private static final float WARMUP_SPEED = 0.019f;  // Mindustry GenericCrafter warmupSpeed 喵
    private static final float POWER_NEEDED = 0.50f;   // Mindustry consumePower(0.50f) 喵

    private int coalCount;
    private int sandCount;
    private int siliconCount;
    private float craftProgress;
    private float warmup;              // 0..1 预热，驱动火焰动画（DrawFlame warmup）喵
    private long smokeStartGameTime = -1; // 最近一次 craft 完成的服务端游戏时刻（Fx.smeltsmoke 起点）喵
    private float lastSyncedWarmup = -1f; // 同步游标，warmup 变化超过阈值才发包喵
    private float powerStatus;         // 电网满足率 0..1（由 PowerGridManager 结算注入）喵

    public SiliconSmelterBlockEntity(BlockPos pos, BlockState state) {
        super(SiliconSmelterRegistrar.SILICON_SMELTER_ENTITY.get(), pos, state);
    }

    public SiliconSmelterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // —— 渲染/Jade 用 getter ——
    public float getWarmup() {
        return warmup;
    }

    public float getCraftProgress() {
        return craftProgress;
    }

    public long getSmokeStartGameTime() {
        return smokeStartGameTime;
    }

    public int getCoalCount() {
        return coalCount;
    }

    public int getSandCount() {
        return sandCount;
    }

    public int getSiliconCount() {
        return siliconCount;
    }

    // 接收判定：只收煤/沙，且各自库存未满喵
    @Override
    public boolean acceptsItem(Item item) {
        if (item == Items.COAL) return coalCount < CAPACITY;
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
            return anchorBe instanceof SiliconSmelterBlockEntity anchor && anchor.acceptItem(source, item);
        }
        return acceptsItem(item);
    }

    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        // 非锚点格转发给锚点格喵
        if (!isAnchor()) {
            BlockEntity anchorBe = level.getBlockEntity(getAnchor());
            return anchorBe instanceof SiliconSmelterBlockEntity anchor && anchor.handleItem(source, item);
        }
        if (!acceptItem(source, item)) return false;
        if (item == Items.COAL) {
            coalCount++;
        } else if (item == Items.SAND) {
            sandCount++;
        }
        setChanged();
        return true;
    }

    // 生产：耗电 + 料足（煤1沙2）+ 输出未满 才推进；攒满 40tick 扣料产硅 + 触发冒烟喵
    @Override
    protected void tickAnchor() {
        // 先把产出的硅卸给相邻传送带喵
        if (siliconCount > 0 && dumpItem(BlockdustryBlocks.SILICON.get())) {
            siliconCount--;
            setChanged();
        }
        // 无电即停摆：powerStatus<=0.01 视为断电（UnitFactory 同款）喵
        boolean hasPower = getPowerStatus() > 0.01f;
        boolean producing = hasPower && coalCount >= COAL_PER_CRAFT
                && sandCount >= SAND_PER_CRAFT && siliconCount < CAPACITY;
        // warmup 预热：可生产爬升、否则衰减（Mindustry approachDelta 0.019/tick）喵
        warmup = producing ? Math.min(1f, warmup + WARMUP_SPEED) : Math.max(0f, warmup - WARMUP_SPEED);
        if (producing) {
            craftProgress += 1f / CRAFT_TIME;
            if (craftProgress >= 1f) {
                craftProgress = 0f;
                coalCount -= COAL_PER_CRAFT;
                sandCount -= SAND_PER_CRAFT;
                siliconCount++;
                // Fx.smeltsmoke：craft 完成时在建筑中心冒烟，记录起点并立即同步客户端喵
                smokeStartGameTime = level.getGameTime();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
                setChanged();
            }
        }
        // warmup 变化超过阈值时同步客户端（火焰动画 + 冒烟起点需要读到 warmup）喵
        if (Math.abs(warmup - lastSyncedWarmup) > 0.02f) {
            lastSyncedWarmup = warmup;
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
        // 非锚点格把自己并入锚点所在电网（UnitFactory 同款）喵
        if (isAnchor()) return List.of();
        BlockPos anchor = getAnchor();
        return anchor != null ? List.of(anchor) : List.of();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("bd_smelter_coal", coalCount);
        tag.putInt("bd_smelter_sand", sandCount);
        tag.putInt("bd_smelter_silicon", siliconCount);
        tag.putFloat("bd_smelter_progress", craftProgress);
        tag.putFloat("bd_smelter_warmup", warmup);
        tag.putLong("bd_smelter_smoke_start", smokeStartGameTime);
        tag.putFloat("bd_smelter_power", powerStatus);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        coalCount = tag.getInt("bd_smelter_coal");
        sandCount = tag.getInt("bd_smelter_sand");
        siliconCount = tag.getInt("bd_smelter_silicon");
        craftProgress = tag.getFloat("bd_smelter_progress");
        warmup = tag.getFloat("bd_smelter_warmup");
        smokeStartGameTime = tag.getLong("bd_smelter_smoke_start");
        powerStatus = tag.getFloat("bd_smelter_power");
    }
}
