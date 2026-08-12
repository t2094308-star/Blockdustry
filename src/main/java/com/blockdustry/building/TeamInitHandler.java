package com.blockdustry.building;

import com.blockdustry.Blockdustry;
import com.blockdustry.BlockdustryTeams;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

// 玩家登录：无主(DERELICT)玩家赋默认队 SHARDED（忠于 Mindustry 玩家必有队伍，核心/资源栏按队伍归属）喵
@EventBusSubscriber(modid = Blockdustry.MODID)
public class TeamInitHandler {
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (BlockdustryTeams.getTeam(player) == BlockdustryTeam.DERELICT) {
                BlockdustryTeams.setTeam(player, BlockdustryTeam.SHARDED);
            }
        }
    }
}
