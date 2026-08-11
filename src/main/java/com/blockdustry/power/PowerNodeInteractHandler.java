package com.blockdustry.power;

import com.blockdustry.Blockdustry;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

// PowerNode 右键交互：空手右键节点，若准星指向合法有电建筑则连接/断开喵
@EventBusSubscriber(modid = Blockdustry.MODID)
public class PowerNodeInteractHandler {
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide() != LogicalSide.SERVER) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();
        if (player == null) return;
        BlockEntity be = player.serverLevel().getBlockEntity(event.getPos());
        if (!(be instanceof PowerNodeBlockEntity node)) return;
        if (!player.getMainHandItem().isEmpty()) return; // 空手才连接喵
        HitResult hit = player.pick(5.0, 0f, false);
        if (hit instanceof BlockHitResult bhr) {
            node.toggleLink(bhr.getBlockPos());
        }
        event.setCanceled(true);
    }
}
