package com.blockdustry;

import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class BlockdustryTeams {
    private BlockdustryTeams() {}

    public static BlockdustryTeam getTeam(ServerLevel level, BlockPos pos) {
        var map = level.getExistingDataOrNull(BlockdustryAttachments.BLOCK_TEAM.get());
        return map != null ? map.getOrDefault(pos, BlockdustryTeam.DERELICT) : BlockdustryTeam.DERELICT;
    }

    public static void setTeam(ServerLevel level, BlockPos pos, BlockdustryTeam team) {
        if (level.isClientSide) return;
        level.getData(BlockdustryAttachments.BLOCK_TEAM.get()).put(pos, team);
        // 触发客户端方块更新，让 Jade 重新拉取 serverData（队伍变更即时反馈到 Jade 面板）喵
        level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
    }

    public static void removeTeam(ServerLevel level, BlockPos pos) {
        var map = level.getExistingDataOrNull(BlockdustryAttachments.BLOCK_TEAM.get());
        if (map != null) map.remove(pos);
    }

    public static BlockdustryTeam getTeam(Entity entity) {
        BlockdustryTeam team = entity.getExistingDataOrNull(BlockdustryAttachments.ENTITY_TEAM.get());
        return team != null ? team : BlockdustryTeam.DERELICT;
    }

    public static void setTeam(Entity entity, BlockdustryTeam team) {
        entity.setData(BlockdustryAttachments.ENTITY_TEAM.get(), team);
    }

    public static boolean isEnemy(BlockdustryTeam a, BlockdustryTeam b) {
        return a.isEnemy(b);
    }

    // 索敌判断：DERELICT 攻击者攻击所有非 DERELICT 目标；有队伍攻击者按 isEnemy；DERELICT 目标永不被攻击喵
    public static boolean isHostile(BlockdustryTeam attacker, BlockdustryTeam target) {
        if (target == BlockdustryTeam.DERELICT) return false;
        return attacker == BlockdustryTeam.DERELICT || attacker.isEnemy(target);
    }

    public static boolean canInteract(BlockdustryTeam a, BlockdustryTeam b) {
        return a.canInteract(b);
    }
}
