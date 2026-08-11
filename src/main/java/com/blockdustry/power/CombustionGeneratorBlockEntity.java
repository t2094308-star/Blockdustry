package com.blockdustry.power;

import com.blockdustry.building.BlockdustryBlocks;
import com.blockdustry.building.BlockdustryBuildingEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
// 燃烧发电机（Mindustry combustion-generator）：烧煤产电，powerProduction=1/tick，itemDuration=120 吃 1 煤喵
public class CombustionGeneratorBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    private static final int ITEM_DURATION = 120;
    private int fuelTime;
    private float powerStatus;

    // 放置构造：super 用注册表实体类型（drill 同款）喵
    public CombustionGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockdustryBlocks.COMBUSTION_GENERATOR_ENTITY.get(), pos, state);
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
