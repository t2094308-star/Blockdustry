package com.blockdustry.power;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.SurgeTowerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

// 涌电塔右键交互（Mindustry PowerNode 两阶段，与 PowerNodeInteractHandler 同模式）喵。
// 空手右键涌电塔选中，再右键目标建筑连接/断开；非锚点格自动重定向到锚点格喵
@EventBusSubscriber(modid = Blockdustry.MODID)
public class SurgeTowerInteractHandler {
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();
        if (player == null) return;
        if (!player.getMainHandItem().isEmpty()) return; // 空手喵
        BlockPos clicked = event.getPos();
        BlockEntity be = player.serverLevel().getBlockEntity(clicked);
        if (be instanceof SurgeTowerBlockEntity st) {
            // 非锚点格重定向到锚点格（links 存锚点）喵
            SurgeTowerBlockEntity node = st.isAnchor() ? st : anchorOf(player.serverLevel(), st);
            if (node == null) return;
            player.getPersistentData().putLong("bd_selected_surge", node.getBlockPos().asLong());
            player.sendSystemMessage(Component.literal(
                    "已选中涌电塔 " + node.getBlockPos().toShortString() + "，右键目标建筑以连接/断开"));
        } else {
            // 右键其他：若已选中涌电塔 → 对该目标 toggle 连接/断开喵
            long sel = player.getPersistentData().getLong("bd_selected_surge");
            if (sel != 0L) {
                BlockPos selPos = BlockPos.of(sel);
                if (player.serverLevel().getBlockEntity(selPos) instanceof SurgeTowerBlockEntity node) {
                    node.toggleLink(clicked);
                }
            }
        }
        event.setCanceled(true);
    }

    // 取锚点格实体（跨格安全）喵
    private static SurgeTowerBlockEntity anchorOf(net.minecraft.server.level.ServerLevel level, SurgeTowerBlockEntity st) {
        BlockPos a = st.getAnchor();
        if (a == null) return null;
        BlockEntity be = level.getBlockEntity(a);
        return be instanceof SurgeTowerBlockEntity anchor ? anchor : null;
    }
}
