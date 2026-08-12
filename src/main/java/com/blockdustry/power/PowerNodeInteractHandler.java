package com.blockdustry.power;

import com.blockdustry.Blockdustry;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

// PowerNode 右键交互（Mindustry 两阶段）：空手右键节点选中，再右键目标建筑连接/断开喵
@EventBusSubscriber(modid = Blockdustry.MODID)
public class PowerNodeInteractHandler {
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return; // 只服务端处理，避免单机重复喵
        ServerPlayer player = (ServerPlayer) event.getEntity();
        if (player == null) return;
        if (!player.getMainHandItem().isEmpty()) return; // 空手喵
        BlockPos clicked = event.getPos();
        BlockEntity be = player.serverLevel().getBlockEntity(clicked);
        if (be instanceof PowerNodeBlockEntity node) {
            // 右键节点：选中，进入连接模式喵
            player.getPersistentData().putLong("bd_selected_node", clicked.asLong());
            player.sendSystemMessage(Component.literal(
                    "已选中电力节点 " + clicked.toShortString() + "，右键目标建筑以连接/断开"));
        } else {
            // 右键其他：若已选中节点 → 对该目标 toggle 连接/断开喵
            long sel = player.getPersistentData().getLong("bd_selected_node");
            if (sel != 0L) {
                BlockPos selPos = BlockPos.of(sel);
                if (player.serverLevel().getBlockEntity(selPos) instanceof PowerNodeBlockEntity node) {
                    node.toggleLink(clicked);
                }
            }
        }
        event.setCanceled(true);
    }
}
