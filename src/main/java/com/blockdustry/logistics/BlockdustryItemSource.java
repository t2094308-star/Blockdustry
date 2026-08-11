package com.blockdustry.logistics;

import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;

// 物品发送方：暴露队伍与来源格坐标（用于接收方方向/同队判定）喵
public interface BlockdustryItemSource {
    BlockdustryTeam getTeam();

    BlockPos getPos();
}
