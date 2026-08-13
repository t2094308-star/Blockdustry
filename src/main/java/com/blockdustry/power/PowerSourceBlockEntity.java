package com.blockdustry.power;

import java.util.ArrayList;
import java.util.List;

import com.blockdustry.building.BlockdustryBlocks;
import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 电力源（Mindustry sandbox power-source）：调试方块，无限产电，powerProduction=100/tick，无耗电无电池喵。
// 参考 CombustionGenerator 实现 BlockdustryPowerNode；额外维护「6 邻域相邻有电建筑」自动连接列表，
// 让电力源紧邻放置即可直接供电（无需 PowerNode 连线）喵
public class PowerSourceBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    public static final float POWER_PRODUCTION = 100f;

    // 相邻连接目标：每 tick 重扫 6 邻域（东/西/上/下/南/北），相邻建筑放置/拆除后下一 tick 自动生效喵
    private final List<BlockPos> links = new ArrayList<>();
    private float powerStatus;

    public PowerSourceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockdustryBlocks.POWER_SOURCE_ENTITY.get(), pos, state);
    }

    // 电力源不吃任何物品喵
    @Override
    public boolean acceptsItem(Item item) {
        return false;
    }

    @Override
    protected void tickAnchor() {
        // 重扫 6 邻域：把紧邻的同队有电建筑并入自己的供电网络（实现「直接相邻供电」）；
        // 队伍判定与 PowerNode.linkValid 一致（同队或 DERELICT）喵
        links.clear();
        for (Direction dir : Direction.values()) {
            BlockPos p = worldPosition.relative(dir);
            if (level.getBlockEntity(p) instanceof BlockdustryPowerNode other
                    && other != this
                    && getTeam().canInteract(other.getTeam())) {
                links.add(p);
            }
        }
    }

    // —— BlockdustryPowerNode ——
    @Override
    public BlockdustryTeam getTeam() {
        return super.getTeam();
    }

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

    @Override
    public List<BlockPos> getPowerLinks() {
        return links;
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
