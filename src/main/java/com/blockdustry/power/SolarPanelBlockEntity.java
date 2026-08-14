package com.blockdustry.power;

import com.blockdustry.building.BlockdustryBuildingEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 太阳能板（Mindustry solar-panel 1×1，类 SolarGenerator）：被动产电，powerProduction=0.12f/tick，
// 无燃料无人工。原版还受环境光 Attribute.light + solarMultiplier（昼夜）影响，Blockdustry 简化为恒定产电喵
public class SolarPanelBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    public static final float POWER_PRODUCTION = 0.12f; // Mindustry solar-panel powerProduction（每 tick）喵
    private float powerStatus;

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(SolarPanelRegistrar.SOLAR_PANEL_ENTITY.get(), pos, state);
    }

    // 太阳能板不吃任何物品喵
    @Override
    public boolean acceptsItem(Item item) {
        return false;
    }

    // 被动产电：无需 tick 逻辑（无燃料/无人工/无状态变化）喵
    @Override
    protected void tickAnchor() {
    }

    // —— BlockdustryPowerNode ——
    @Override
    public BlockPos getPos() {
        return worldPosition;
    }

    @Override
    public float powerProduction() {
        return POWER_PRODUCTION;
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
