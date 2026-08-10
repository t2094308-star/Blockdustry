package com.blockdustry;

import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class BlockdustryTeams {
    private BlockdustryTeams() {}

    public static BlockdustryTeam getTeam(ServerLevel level, BlockPos pos) {
        var map = level.getExistingDataOrNull(BlockdustryAttachments.BLOCK_TEAM.get());
        return map != null ? map.getOrDefault(pos, BlockdustryTeam.NEUTRAL) : BlockdustryTeam.NEUTRAL;
    }

    public static void setTeam(ServerLevel level, BlockPos pos, BlockdustryTeam team) {
        if (level.isClientSide) return;
        level.getData(BlockdustryAttachments.BLOCK_TEAM.get()).put(pos, team);
    }

    public static void removeTeam(ServerLevel level, BlockPos pos) {
        var map = level.getExistingDataOrNull(BlockdustryAttachments.BLOCK_TEAM.get());
        if (map != null) map.remove(pos);
    }

    public static BlockdustryTeam getTeam(Entity entity) {
        BlockdustryTeam team = entity.getExistingDataOrNull(BlockdustryAttachments.ENTITY_TEAM.get());
        return team != null ? team : BlockdustryTeam.NEUTRAL;
    }

    public static void setTeam(Entity entity, BlockdustryTeam team) {
        entity.setData(BlockdustryAttachments.ENTITY_TEAM.get(), team);
    }

    public static boolean isEnemy(BlockdustryTeam a, BlockdustryTeam b) {
        return a.isEnemy(b);
    }

    public static boolean canInteract(BlockdustryTeam a, BlockdustryTeam b) {
        return a.canInteract(b);
    }
}
