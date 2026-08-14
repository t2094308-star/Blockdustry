package com.blockdustry.power;

import java.util.List;

import com.blockdustry.building.BlockdustryBuildingEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 大型太阳能板（Mindustry solar-panel-large 3×3，类 SolarGenerator）：被动产电，powerProduction=1.6f/tick，
// 无燃料无人工。原版还受环境光影响，Blockdustry 简化为恒定产电喵
public class SolarPanelLargeBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    public static final float POWER_PRODUCTION = 1.6f; // Mindustry solar-panel-large powerProduction（每 tick）喵
    private float powerStatus;

    public SolarPanelLargeBlockEntity(BlockPos pos, BlockState state) {
        super(SolarPanelRegistrar.SOLAR_PANEL_LARGE_ENTITY.get(), pos, state);
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
        // 仅锚点格计入产电：3×3 共 9 格 BE 都会进电网结算，非锚点格返回 0，避免产电被计 9 次喵
        return isAnchor() ? POWER_PRODUCTION : 0f;
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

    @Override
    public List<BlockPos> getPowerLinks() {
        // 非锚点格把自己并入锚点所在电网：无论 PowerNode/电力源连到大型板哪一格，整座板都在同一网喵
        if (isAnchor()) return List.of();
        BlockPos anchor = getAnchor();
        return anchor != null ? List.of(anchor) : List.of();
    }
}
