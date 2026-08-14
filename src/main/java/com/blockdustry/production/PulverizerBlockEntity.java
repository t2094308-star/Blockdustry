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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.joml.Vector3f;

// 粉碎机（Mindustry pulverizer，GenericCrafter，size 1）：吃 1 废料产 1 沙，craftTime=40tick，
// 各物品独立容量 10（Mindustry Building.items 每类型上限 itemCapacity），耗电 0.5/s（Mindustry consumePower(0.50f)）。
// 带 warmup 预热（驱动转盘旋转动画）与 craft/持续灰色尘粒（Fx.pulverize / Fx.pulverizeSmall 等效）喵
public class PulverizerBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    private static final int CRAFT_TIME = 40;           // Mindustry pulverizer craftTime = 40f tick 喵
    private static final int CAPACITY = 10;             // GenericCrafter 默认 itemCapacity = 10（各类型独立上限）喵
    private static final int SCRAP_INPUT = 1;           // 每窑消耗废料 1 喵
    private static final float WARMUP_SPEED = 0.019f;   // Mindustry GenericCrafter warmupSpeed 喵
    private static final float POWER_NEEDED = 0.50f;    // Mindustry pulverizer consumePower(0.50f) 喵
    private static final float UPDATE_EFFECT_CHANCE = 0.04f; // GenericCrafter updateEffectChance 喵

    private int scrapCount;
    private int sandCount;
    private float craftProgress;
    private float warmup;           // 0..1 预热，驱动转盘旋转渲染喵
    private float lastSyncedWarmup = -1f; // 同步游标，warmup 变化超过阈值才发包喵
    private float powerStatus;      // 电网满足率 0..1（由 PowerGrid 结算注入）喵

    public PulverizerBlockEntity(BlockPos pos, BlockState state) {
        super(PulverizerRegistrar.PULVERIZER_ENTITY.get(), pos, state);
    }

    public PulverizerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 渲染/调试读 warmup 喵
    public float getWarmup() {
        return warmup;
    }

    // Jade/调试读取用 getter 喵
    public int getScrapCount() {
        return scrapCount;
    }

    public int getSandCount() {
        return sandCount;
    }

    public float getCraftProgress() {
        return craftProgress;
    }

    // 接收判定：仅收废料，且库存未满（独立上限 CAPACITY）喵
    @Override
    public boolean acceptsItem(Item item) {
        if (item == com.blockdustry.item.BlockdustryItems.SCRAP.get()) return scrapCount < CAPACITY;
        return false;
    }

    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        if (!acceptItem(source, item)) return false;
        if (item == com.blockdustry.item.BlockdustryItems.SCRAP.get()) {
            scrapCount++;
        }
        setChanged();
        return true;
    }

    @Override
    protected void tickAnchor() {
        // 先把产出的沙卸给相邻传送带喵
        if (sandCount > 0 && dumpItem(Items.SAND)) {
            sandCount--;
        }
        // 无电即停摆：powerStatus<=0.01 视为断电，与缺料同样处理喵
        boolean hasPower = getPowerStatus() > 0.01f;
        boolean producing = hasPower && scrapCount >= SCRAP_INPUT && sandCount < CAPACITY;
        // warmup 预热：可生产爬升、否则衰减（Mindustry approachDelta 0.019/tick）喵
        warmup = producing ? Math.min(1f, warmup + WARMUP_SPEED) : Math.max(0f, warmup - WARMUP_SPEED);
        // 持续粉碎小尘粒（Fx.pulverizeSmall：运行中 4%/tick 概率、3 个灰方尘点，Mindustry updateEffectChance=0.04）喵
        if (producing && level.random.nextFloat() < UPDATE_EFFECT_CHANCE) {
            spawnSmallParticles();
        }
        if (producing) {
            craftProgress += 1f / CRAFT_TIME;
            if (craftProgress >= 1f) {
                craftProgress = 0;
                scrapCount -= SCRAP_INPUT;
                if (!dumpItem(Items.SAND)) {
                    sandCount++;
                }
                spawnCraftParticles();
                setChanged();
            }
        }
        // warmup 变化超过阈值时同步客户端（渲染转盘旋转需要读到 warmup）喵
        if (Math.abs(warmup - lastSyncedWarmup) > 0.02f) {
            lastSyncedWarmup = warmup;
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    // craft 完成：灰色尘点向外散（Mindustry Fx.pulverize 等效：5 个 stoneGray #8f8f8f 方块、半径 3+fin×8 单位、旋转 45°）喵
    private void spawnCraftParticles() {
        if (level instanceof ServerLevel serverLevel) {
            double x = worldPosition.getX() + 0.5;
            double y = worldPosition.getY() + 0.5;
            double z = worldPosition.getZ() + 0.5;
            serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.56f, 0.56f, 0.56f), 1.2f),
                    x, y, z, 5, 0.5, 0.25, 0.5, 0.1);
        }
    }

    // 持续粉碎小尘粒（Mindustry Fx.pulverizeSmall 等效：3 个 stoneGray、半径 fin×5 单位、旋转 45°）喵
    private void spawnSmallParticles() {
        if (level instanceof ServerLevel serverLevel) {
            double x = worldPosition.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.5;
            double y = worldPosition.getY() + 0.6;
            double z = worldPosition.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.5;
            serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.56f, 0.56f, 0.56f), 0.8f),
                    x, y, z, 3, 0.25, 0.1, 0.25, 0.05);
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
        // size 1 单格：锚点即自身，始终返回额定耗电喵
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
        return List.of();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("bd_scrap", scrapCount);
        tag.putInt("bd_sand", sandCount);
        tag.putFloat("bd_progress", craftProgress);
        tag.putFloat("bd_warmup", warmup);
        tag.putFloat("bd_power_status", powerStatus);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        scrapCount = tag.getInt("bd_scrap");
        sandCount = tag.getInt("bd_sand");
        craftProgress = tag.getFloat("bd_progress");
        warmup = tag.getFloat("bd_warmup");
        powerStatus = tag.getFloat("bd_power_status");
    }
}
