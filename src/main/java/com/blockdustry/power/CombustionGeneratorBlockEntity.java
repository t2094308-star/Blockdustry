package com.blockdustry.power;

import com.blockdustry.building.BlockdustryBlocks;
import com.blockdustry.building.BlockdustryBuildingEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
// 燃烧发电机（Mindustry combustion-generator）：烧煤产电，powerProduction=1/tick，itemDuration=120 吃 1 煤。
// 带 warmup 预热（DrawWarmupRegion 染色开关）与周期火花粒子喵
public class CombustionGeneratorBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    private static final int ITEM_DURATION = 120;
    private static final float WARMUP_SPEED = 0.05f; // Mindustry ConsumeGenerator warmupSpeed 喵
    private int fuelTime;
    private float powerStatus;
    private float warmup; // 0..1 预热，驱动顶面染色与火花粒子喵
    private float lastSyncedWarmup = -1f; // 同步游标，warmup 变化超过阈值才发包喵

    // 放置构造：super 用注册表实体类型（drill 同款）喵
    public CombustionGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockdustryBlocks.COMBUSTION_GENERATOR_ENTITY.get(), pos, state);
    }

    public float getWarmup() {
        return warmup;
    }

    // 只收煤喵
    @Override
    public boolean acceptsItem(Item item) {
        return item == Items.COAL && getStoredCount() < getCapacity();
    }

    @Override
    protected void tickAnchor() {
        if (fuelTime <= 0) {
            if (getStoredCount() > 0 && getStoredItem() == Items.COAL) {
                removeOne();
                fuelTime = ITEM_DURATION;
            }
        } else {
            fuelTime--;
        }
        // warmup 预热：有燃料爬升、否则衰减（Mindustry lerpDelta 0.05/tick）喵
        warmup = fuelTime > 0 ? Math.min(1f, warmup + WARMUP_SPEED) : Math.max(0f, warmup - WARMUP_SPEED);
        // 周期火花粒子（Mindustry generateEffect 0.01/tick 太稀疏，提至 0.02，FLAME 映射橙色火花）喵
        if (fuelTime > 0 && warmup > 0 && level instanceof ServerLevel serverLevel
                && level.random.nextFloat() < 0.02f) {
            double x = worldPosition.getX() + 0.5 + level.random.nextGaussian() * 1.5;
            double y = worldPosition.getY() + 0.5;
            double z = worldPosition.getZ() + 0.5 + level.random.nextGaussian() * 1.5;
            serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0, 0.05, 0, 0);
        }
        // warmup 变化超过阈值时同步客户端（渲染染色需要读到 warmup）喵
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
        tag.putFloat("bd_warmup", warmup);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        warmup = tag.getFloat("bd_warmup");
    }

    @Override
    public BlockPos getPos() {
        return worldPosition;
    }

    @Override
    public float powerProduction() {
        return fuelTime > 0 ? 1f : 0f;
    }

    @Override
    public float powerNeeded() {
        return 0f;
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
}
