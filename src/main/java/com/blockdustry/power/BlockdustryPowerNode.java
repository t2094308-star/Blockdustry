package com.blockdustry.power;

import java.util.List;

import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;

// 电力节点接口：建筑声明产电/耗电/电池能力，PowerNode 额外提供连接列表喵
public interface BlockdustryPowerNode {
    // 每 tick 产电喵
    float powerProduction();

    // 每 tick 耗电喵
    float powerNeeded();

    // 电池容量（0=无电池）喵
    float powerCapacity();

    // 当前电量（电池）喵
    float powerStored();

    // 满足率/电量百分比 0~1 喵
    float getPowerStatus();

    void setPowerStatus(float status);

    // 由实现类继承自 BlockdustryBuildingEntity（BlockdustryItemSource）提供喵
    default BlockdustryTeam getTeam() {
        return null;
    }

    default BlockPos getPos() {
        return null;
    }

    // PowerNode 专属：连接的目标列表（其余建筑默认空）喵
    default List<BlockPos> getPowerLinks() {
        return List.of();
    }
}
