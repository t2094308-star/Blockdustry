package com.blockdustry.power;

import java.util.ArrayList;
import java.util.List;

import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 大型电池（Mindustry battery-large）：3×3、consumePowerBuffered(50000f)，纯缓冲存电，充放电由电网结算喵。
// 多格电网连通性：锚点格 getPowerLinks() 返回本组全部格，保证节点连到任意格整组入网；
// 仅锚点格报容量 50000（非锚点格容量 0 防重复计数），电网按整组一份电池结算喵
public class BatteryLargeBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    private static final float CAPACITY = 50000f; // 原版 Blocks.java battery-large consumePowerBuffered(50000f) 喵
    private float powerStatus;

    // 放置构造：super 用注册表实体类型（drill 同款）喵
    public BatteryLargeBlockEntity(BlockPos pos, BlockState state) {
        super(BatteryLargeRegistrar.BATTERY_LARGE_ENTITY.get(), pos, state);
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
    public BlockdustryTeam getTeam() {
        return super.getTeam();
    }

    @Override
    public float powerProduction() {
        return 0f;
    }

    @Override
    public float powerNeeded() {
        return 0f;
    }

    // 仅锚点格报容量（整组一份电池），非锚点格 0 防重复计数喵
    @Override
    public float powerCapacity() {
        return isAnchor() ? CAPACITY : 0f;
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

    // 锚点格返回本组全部格位置，让 Union-Find 把整组连成同一分量（节点连到任意格整组入网）喵
    @Override
    public List<BlockPos> getPowerLinks() {
        if (!isAnchor() || getSize() <= 1) return List.of();
        List<BlockPos> out = new ArrayList<>();
        BlockPos base = hasAnchor() ? getAnchor() : worldPosition;
        int size = getSize();
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                out.add(base.offset(dx, 0, dz));
            }
        }
        return out;
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
