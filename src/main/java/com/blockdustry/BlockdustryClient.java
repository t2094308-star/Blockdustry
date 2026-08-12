package com.blockdustry;

import com.blockdustry.building.BlockdustryBlocks;
import com.blockdustry.client.BlockdustryBulletRenderer;
import com.blockdustry.client.ConveyorBlockEntityRenderer;
import com.blockdustry.client.CombustionGeneratorBlockEntityRenderer;
import com.blockdustry.client.CoreBlockEntityRenderer;
import com.blockdustry.client.DaggerUnitRenderer;
import com.blockdustry.client.DrillBlockEntityRenderer;
import com.blockdustry.client.PowerNodeBlockEntityRenderer;
import com.blockdustry.client.TurretBlockEntityRenderer;
import com.blockdustry.client.UnitFactoryBlockEntityRenderer;
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
        event.registerBlockEntityRenderer(BlockdustryBlocks.CONVEYOR_ENTITY.get(), ConveyorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockdustryBlocks.POWER_NODE_ENTITY.get(), PowerNodeBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockdustryBlocks.TURRET_ENTITY.get(), TurretBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockdustryBlocks.COMBUSTION_GENERATOR_ENTITY.get(), CombustionGeneratorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockdustryBlocks.CORE_ENTITY.get(), CoreBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockdustryBlocks.UNIT_FACTORY_ENTITY.get(), UnitFactoryBlockEntityRenderer::new);
        event.registerEntityRenderer(BlockdustryEntities.BULLET.get(), BlockdustryBulletRenderer::new);
        event.registerEntityRenderer(BlockdustryEntities.DAGGER.get(), DaggerUnitRenderer::new);
    }
}
