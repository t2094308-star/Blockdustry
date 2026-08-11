package com.blockdustry;

import com.blockdustry.building.BlockdustryBlocks;
import com.blockdustry.client.BlockdustryBulletRenderer;
import com.blockdustry.client.DrillBlockEntityRenderer;
import com.blockdustry.entities.BlockdustryEntities;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

// 仅客户端加载的类，不会在专用服务器上实例化喵
@Mod(value = Blockdustry.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Blockdustry.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BlockdustryClient {
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // 验证客户端加载成功的标记日志喵
        Blockdustry.LOGGER.info("方块工业: 客户端加载成功");
    }

    // 注册方块实体渲染器与炮弹渲染器喵
    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockdustryBlocks.DRILL_ENTITY.get(), DrillBlockEntityRenderer::new);
        event.registerEntityRenderer(BlockdustryEntities.BULLET.get(), BlockdustryBulletRenderer::new);
    }
}
