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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.joml.Vector3f;

// 焚化炉（Mindustry incinerator，Incinerator 类，size 1）：吞噬并销毁任何被接收的物品，
// 不产出任何物品、无库存。heat 预热（approachDelta 0.04/tick 逼近 efficiency），
// 只有 heat>0.5 且有电时才接收物品（Mindustry acceptItem: heat>0.5 && enabled）；
// 每接收 30% 概率在中心喷灰色烟团（Fx.fuelburn 等效）。耗电 0.5/s 喵
public class IncineratorBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    private static final float HEAT_SPEED = 0.04f;       // Mindustry IncineratorBuild: approachDelta(heat, efficiency, 0.04) 喵
    private static final float POWER_NEEDED = 0.50f;     // Mindustry incinerator consumePower(0.50f) 喵
    private static final float ACCEPT_HEAT = 0.5f;       // Mindustry acceptItem: heat > 0.5f 喵
    private static final float FUELBURN_CHANCE = 0.30f;  // Mindustry handleItem: Mathf.chance(0.3) 喵

    private float heat;             // 0..1 预热，驱动火焰渲染与接收判定喵
    private float lastSyncedHeat = -1f; // 同步游标，heat 变化超过阈值才发包喵
    private float powerStatus;      // 电网满足率 0..1（由 PowerGrid 结算注入）喵

    public IncineratorBlockEntity(BlockPos pos, BlockState state) {
        super(IncineratorRegistrar.INCINERATOR_ENTITY.get(), pos, state);
    }

    public IncineratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 渲染/调试读 heat 喵
    public float getHeat() {
        return heat;
    }

    public boolean isBurning() {
        return heat > ACCEPT_HEAT;
    }

    // 接收判定：任何物品，只要热（heat>0.5）且通电（Mindustry acceptItem: heat>0.5 && enabled）喵
    @Override
    public boolean acceptsItem(Item item) {
        return heat > ACCEPT_HEAT && getPowerStatus() > 0.01f;
    }

    // 真正移交：不存储，直接销毁（Mindustry handleItem 只播特效，物品被吞噬）喵
    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        if (!acceptItem(source, item)) return false;
        // Mindustry handleItem: 30% 概率 fuelburn 特效，物品直接销毁喵
        if (level.random.nextFloat() < FUELBURN_CHANCE) {
            spawnFuelBurn();
        }
        return true;
    }

    @Override
    protected void tickAnchor() {
        // Mindustry IncineratorBuild.updateTile: heat = approachDelta(heat, efficiency, 0.04)
        // efficiency = enabled × powerStatus（本 mod 无逻辑系统，enabled 恒真 → 目标 = 通电 ? 1 : 0）喵
        float target = getPowerStatus() > 0.01f ? 1f : 0f;
        heat = heat < target ? Math.min(target, heat + HEAT_SPEED) : Math.max(target, heat - HEAT_SPEED);
        // heat 变化超过阈值时同步客户端（渲染火焰需要读到 heat）喵
        if (Math.abs(heat - lastSyncedHeat) > 0.02f) {
            lastSyncedHeat = heat;
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    // 吞噬烟灰（Mindustry Fx.fuelburn 等效：5 个灰圆点、半径 fin×9 单位、颜色 lightGray→gray）喵
    private void spawnFuelBurn() {
        if (level instanceof ServerLevel serverLevel) {
            double x = worldPosition.getX() + 0.5;
            double y = worldPosition.getY() + 0.4;
            double z = worldPosition.getZ() + 0.5;
            serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.60f, 0.60f, 0.60f), 1.0f),
                    x, y, z, 5, 0.45, 0.25, 0.45, 0.08);
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
        tag.putFloat("bd_heat", heat);
        tag.putFloat("bd_power_status", powerStatus);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        heat = tag.getFloat("bd_heat");
        powerStatus = tag.getFloat("bd_power_status");
    }
}
