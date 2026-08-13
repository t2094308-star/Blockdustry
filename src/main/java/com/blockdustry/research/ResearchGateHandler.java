package com.blockdustry.research;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.BlockdustryBuildingBlock;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

// 研究门控：未解锁建筑禁止放置（服务端权威兜底，Mindustry 只在 UI 层过滤，MC 联机必须服务端拦）喵
// 与 BlockdustryTeamHandler（同事件）各自订阅不冲突喵
@EventBusSubscriber(modid = Blockdustry.MODID)
public final class ResearchGateHandler {
    private ResearchGateHandler() {}

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;   // 只服务端兜底喵
        Block block = event.getPlacedBlock().getBlock();
        if (!(block instanceof BlockdustryBuildingBlock)) return;      // 只拦本 mod 建筑喵
        if (!(event.getEntity() instanceof ServerPlayer player)) return; // 非玩家放置（命令/活塞）不拦喵
        if (player.getAbilities().instabuild) return;                 // 创造模式默认绕过（Mindustry sandbox 全解锁），可加 config 开关喵
        ResearchNode node = ResearchTree.get().nodeForBlock(block);
        if (node == null || node.defaultUnlocked) return;             // 无节点或默认解锁直接放行喵
        if (!ResearchManager.isUnlocked(level, node.id)) {
            event.setCanceled(true);                                  // 取消放置（回滚 BlockSnapshot）喵
            player.sendSystemMessage(Component.translatable(
                    "blockdustry.research.locked", block.getName()));
        }
    }
}
