package com.blockdustry.power;

import com.blockdustry.building.BlockdustryBlocks;
import com.blockdustry.building.BlockdustryBuildingEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 电池（Mindustry battery）：纯缓冲存电，capacity=4000，充放电由电网结算喵
public class BatteryBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    private static final float CAPACITY = 4000f;
    private float powerStatus;

    // 放置构造：super 用注册表实体类型（drill 同款）喵
    public BatteryBlockEntity(BlockPos pos, BlockState state) {
        super(BlockdustryBlocks.BATTERY_ENTITY.get(), pos, state);
    }

    // 电池不收物品喵
    @Override
    public boolean acceptsItem(Item item) {
        return false;
    }

    @Override
    protected void tickAnchor() {
        // 电池不 tick，充放电由电网结算喵
    }

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
        return 0f;
    }

    @Override
    public float powerCapacity() {
        return CAPACITY;
    }

    @Override
    public float powerStored() {
        return powerStatus * CAPACITY;
    }

    @Override
    public float getPowerStatus() {
        return powerStatus;
    }

    @Override
    public void setPowerStatus(float status) {
        this.powerStatus = Math.max(0f, Math.min(1f, status));
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("bd_power_status", powerStatus);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        powerStatus = tag.getFloat("bd_power_status");
    }
}
