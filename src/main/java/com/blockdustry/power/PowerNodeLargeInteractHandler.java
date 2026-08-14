package com.blockdustry.power;

import com.blockdustry.Blockdustry;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

// 大型电力节点右键交互（Mindustry 两阶段，与 1×1 PowerNodeInteractHandler 同逻辑）：
// 空手右键大型节点选中，再右键目标建筑连接/断开。用独立选择 key 避免与 1×1 冲突喵
@EventBusSubscriber(modid = Blockdustry.MODID)
public class PowerNodeLargeInteractHandler {
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return; // 只服务端处理，避免单机重复喵
        ServerPlayer player = (ServerPlayer) event.getEntity();
        if (player == null) return;
        if (!player.getMainHandItem().isEmpty()) return; // 空手喵
        BlockPos clicked = event.getPos();
        BlockEntity be = player.serverLevel().getBlockEntity(clicked);
        if (be instanceof PowerNodeLargeBlockEntity node) {
            // 右键大型节点：选中，进入连接模式喵
            player.getPersistentData().putLong("bd_selected_node_large", clicked.asLong());
            player.sendSystemMessage(Component.literal(
                    "已选中大型电力节点 " + clicked.toShortString() + "，右键目标建筑以连接/断开"));
            event.setCanceled(true);
        } else {
            // 右键其他：若已选中大型节点 → 对该目标 toggle 连接/断开喵
            long sel = player.getPersistentData().getLong("bd_selected_node_large");
            if (sel != 0L) {
                BlockPos selPos = BlockPos.of(sel);
                if (player.serverLevel().getBlockEntity(selPos) instanceof PowerNodeLargeBlockEntity node) {
                    node.toggleLink(clicked);
                    event.setCanceled(true);
                }
            }
        }
    }
}
