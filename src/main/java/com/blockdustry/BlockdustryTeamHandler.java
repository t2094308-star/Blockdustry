package com.blockdustry;

import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

// 放置方块继承放置者队伍；刷怪蛋生成的生物由 EntityTypeMixin 继承使用者队伍喵
@EventBusSubscriber(modid = Blockdustry.MODID)
public class BlockdustryTeamHandler {
    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Entity placer = event.getEntity();
        if (placer == null) return;
        BlockdustryTeam team = BlockdustryTeams.getTeam(placer);
        BlockdustryTeams.setTeam(level, event.getPos(), team);
    }
}
