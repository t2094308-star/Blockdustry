package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.BlockdustryBlocks;
import com.blockdustry.network.QueryPowerPayload;
import com.blockdustry.network.QueryTeamPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

// 队伍调试棒：右键打开队伍 UI（对准方块或实体），替代 /blockdustry team 命令喵
@EventBusSubscriber(modid = Blockdustry.MODID, value = Dist.CLIENT)
public class BlockdustryDebugHandler {
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        // 事件在单机服务端线程也触发，只处理客户端侧喵
        if (!event.getLevel().isClientSide) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!mc.player.getItemInHand(event.getHand()).is(BlockdustryBlocks.DEBUG_STICK.get())) return;
        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult bhr) {
            BlockPos pos = bhr.getBlockPos();
            String name = mc.level.getBlockState(pos).getBlock().getName().getString();
            mc.setScreen(new BlockdustryTeamScreen(pos, -1, name));
            PacketDistributor.sendToServer(new QueryTeamPayload(pos, -1));
            // 电力建筑额外查询电量，供调试显示喵
            if (mc.level.getBlockEntity(pos) instanceof com.blockdustry.power.BlockdustryPowerNode) {
                PacketDistributor.sendToServer(new QueryPowerPayload(pos));
            }
        } else if (hit instanceof EntityHitResult ehr && ehr.getEntity() != null) {
            Entity entity = ehr.getEntity();
            String name = entity.getType().getDescription().getString();
            mc.setScreen(new BlockdustryTeamScreen(BlockPos.ZERO, entity.getId(), name));
            PacketDistributor.sendToServer(new QueryTeamPayload(BlockPos.ZERO, entity.getId()));
        }
        event.setCanceled(true);
    }
}
