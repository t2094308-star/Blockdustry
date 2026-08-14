package com.blockdustry.production;

import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.logistics.BlockdustryItemSource;
import com.blockdustry.power.BlockdustryPowerNode;

import java.util.List;

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

// 窖炉（Mindustry kiln，GenericCrafter，size 2）：吃 1 铅 + 1 沙产 1 钢化玻璃，craftTime=30tick，
// 各物品独立容量 10（Mindustry Building.items 每类型上限 itemCapacity），耗电 0.6/s（Mindustry consumePower(0.60f)）。
// 带 warmup 预热（驱动火焰动画开关）与 craft 白色烟尘粒子（Fx.smeltsmoke 等效）喵
public class KilnBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    private static final int CRAFT_TIME = 30;           // Mindustry kiln craftTime = 30f tick 喵
    private static final int CAPACITY = 10;             // GenericCrafter 默认 itemCapacity = 10（各类型独立上限）喵
    private static final int LEAD_INPUT = 1;            // 每窑消耗铅 1 喵
    private static final int SAND_INPUT = 1;            // 每窑消耗沙 1 喵
    private static final float WARMUP_SPEED = 0.019f;   // Mindustry GenericCrafter warmupSpeed 喵
    private static final float POWER_NEEDED = 0.60f;    // Mindustry kiln consumePower(0.60f) 喵

    private int leadCount;
    private int sandCount;
    private int metaglassCount;
    private float craftProgress;
    private float warmup;           // 0..1 预热，驱动火焰渲染喵
    private float lastSyncedWarmup = -1f; // 同步游标，warmup 变化超过阈值才发包喵
    private float powerStatus;      // 电网满足率 0..1（由 PowerGridManager 结算注入）喵

    public KilnBlockEntity(BlockPos pos, BlockState state) {
        super(KilnRegistrar.KILN_ENTITY.get(), pos, state);
    }

    public KilnBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 渲染/调试读 warmup 喵
    public float getWarmup() {
        return warmup;
    }

    // Jade/调试读取用 getter 喵
    public int getLeadCount() {
        return leadCount;
    }

    public int getSandCount() {
        return sandCount;
    }

    public int getMetaglassCount() {
        return metaglassCount;
    }

    public float getCraftProgress() {
        return craftProgress;
    }

    // 接收判定：仅收铅/沙，且各自库存未满（各类型独立上限 CAPACITY）喵
    @Override
    public boolean acceptsItem(Item item) {
        if (item == com.blockdustry.building.BlockdustryBlocks.LEAD.get()) return leadCount < CAPACITY;
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
            return anchorBe instanceof KilnBlockEntity anchor && anchor.acceptItem(source, item);
        }
        return acceptsItem(item);
    }

    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        // 非锚点格转发给锚点格喵
        if (!isAnchor()) {
            BlockEntity anchorBe = level.getBlockEntity(getAnchor());
            return anchorBe instanceof KilnBlockEntity anchor && anchor.handleItem(source, item);
        }
        if (!acceptItem(source, item)) return false;
        if (item == com.blockdustry.building.BlockdustryBlocks.LEAD.get()) {
            leadCount++;
        } else if (item == Items.SAND) {
            sandCount++;
        }
        setChanged();
        return true;
    }

    @Override
    protected void tickAnchor() {
        // 先把产出的钢化玻璃卸给相邻传送带喵
        if (metaglassCount > 0 && dumpItem(com.blockdustry.item.BlockdustryItems.METAGLASS.get())) {
            metaglassCount--;
        }
        // 无电即停摆：powerStatus<=0.01 视为断电，与缺料同样处理喵
        boolean hasPower = getPowerStatus() > 0.01f;
        boolean producing = hasPower && leadCount >= LEAD_INPUT && sandCount >= SAND_INPUT
                && metaglassCount < CAPACITY;
        // warmup 预热：可生产爬升、否则衰减（Mindustry approachDelta 0.019/tick）喵
        warmup = producing ? Math.min(1f, warmup + WARMUP_SPEED) : Math.max(0f, warmup - WARMUP_SPEED);
        if (producing) {
            craftProgress += 1f / CRAFT_TIME;
            if (craftProgress >= 1f) {
                craftProgress = 0;
                leadCount -= LEAD_INPUT;
                sandCount -= SAND_INPUT;
                if (!dumpItem(com.blockdustry.item.BlockdustryItems.METAGLASS.get())) {
                    metaglassCount++;
                }
                spawnCraftParticles();
                setChanged();
            }
        }
        // warmup 变化超过阈值时同步客户端（渲染动画需要读到 warmup；同步块在 producing 块外，
        // 缺料/断电时 warmup 衰减也会同步 → 客户端火焰熄灭，参考 SiliconSmelter 同款写法）喵
        if (Math.abs(warmup - lastSyncedWarmup) > 0.02f) {
            lastSyncedWarmup = warmup;
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    // craft 完成：白色烟尘向外散（Mindustry Fx.smeltsmoke 等效：6 片白色方块 15tick 内向外飞散）喵
    private void spawnCraftParticles() {
        if (level instanceof ServerLevel serverLevel) {
            double x = worldPosition.getX() + 1.0; // 2×2 中心喵
            double y = worldPosition.getY() + 0.5;
            double z = worldPosition.getZ() + 1.0;
            serverLevel.sendParticles(new DustParticleOptions(new Vector3f(1f, 1f, 1f), 1.5f),
                    x, y, z, 6, 0.5, 0.3, 0.5, 0.12);
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
        // 非锚点格把自己并入锚点所在电网：无论 PowerNode/电力源连到窖炉哪一格，整座窖炉都在同一网喵
        if (isAnchor()) return List.of();
        BlockPos anchor = getAnchor();
        return anchor != null ? List.of(anchor) : List.of();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("bd_lead", leadCount);
        tag.putInt("bd_sand", sandCount);
        tag.putInt("bd_metaglass", metaglassCount);
        tag.putFloat("bd_progress", craftProgress);
        tag.putFloat("bd_warmup", warmup);
        tag.putFloat("bd_power_status", powerStatus);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        leadCount = tag.getInt("bd_lead");
        sandCount = tag.getInt("bd_sand");
        metaglassCount = tag.getInt("bd_metaglass");
        craftProgress = tag.getFloat("bd_progress");
        warmup = tag.getFloat("bd_warmup");
        powerStatus = tag.getFloat("bd_power_status");
    }
}
