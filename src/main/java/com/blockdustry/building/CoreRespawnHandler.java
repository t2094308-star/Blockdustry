package com.blockdustry.building;

import com.blockdustry.Blockdustry;
import com.blockdustry.BlockdustryTeams;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

// 玩家死亡：把重生点设到其队伍核心，实现「绑定队伍后死亡在核心重生」喵
@EventBusSubscriber(modid = Blockdustry.MODID)
public class CoreRespawnHandler {
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {
            BlockdustryTeam team = BlockdustryTeams.getTeam(player);
            if (team == BlockdustryTeam.DERELICT) return; // 无主玩家不回核心喵
            for (BlockdustryBuildingEntity b : BlockdustryBuildings.all()) {
                if (b instanceof CoreBlockEntity core && core.getTeam() == team) {
                    BlockPos pos = core.hasAnchor() ? core.getAnchor() : core.getBlockPos();
                    player.setRespawnPosition(player.level().dimension(), pos.above(), player.getYRot(), true, false);
                    return;
                }
            }
        }
    }
}
