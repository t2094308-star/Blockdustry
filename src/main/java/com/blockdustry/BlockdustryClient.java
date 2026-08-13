package com.blockdustry;

import com.blockdustry.building.BlockdustryBlocks;
import com.blockdustry.client.BlockdustryBulletRenderer;
import com.blockdustry.client.ConveyorBlockEntityRenderer;
import com.blockdustry.client.CombustionGeneratorBlockEntityRenderer;
import com.blockdustry.client.CoreBlockEntityRenderer;
import com.blockdustry.client.DaggerUnitRenderer;
import com.blockdustry.client.DrillBlockEntityRenderer;
import com.blockdustry.client.FlakBulletRenderer;
import com.blockdustry.client.PowerNodeBlockEntityRenderer;
import com.blockdustry.client.ScatterBlockEntityRenderer;
import com.blockdustry.client.TurretBlockEntityRenderer;
import com.blockdustry.client.UnitFactoryBlockEntityRenderer;
import com.blockdustry.client.ArcBeamRenderer;
import com.blockdustry.client.ArcBlockEntityRenderer;
import com.blockdustry.client.ElevatorBlockEntityRenderer;
import com.blockdustry.client.FireBulletRenderer;
import com.blockdustry.client.FuseBlockEntityRenderer;
import com.blockdustry.client.freecam.FreecamHandler;
import com.blockdustry.entities.BlockdustryEntities;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

// 仅客户端加载的类，不会在专用服务器上实例化喵
@Mod(value = Blockdustry.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Blockdustry.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BlockdustryClient {
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // 验证客户端加载成功的标记日志喵
        Blockdustry.LOGGER.info("方块工业: 客户端加载成功");
    }

    // 注册快捷键（灵魂出窍默认 F4）喵
    @SubscribeEvent
    static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(FreecamHandler.KEY_FREECAM);
    }

    // 注册方块实体渲染器与炮弹渲染器喵
    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockdustryBlocks.DRILL_ENTITY.get(), DrillBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockdustryBlocks.CONVEYOR_ENTITY.get(), ConveyorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockdustryBlocks.POWER_NODE_ENTITY.get(), PowerNodeBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockdustryBlocks.TURRET_ENTITY.get(), TurretBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockdustryBlocks.SCATTER_ENTITY.get(), ScatterBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockdustryBlocks.COMBUSTION_GENERATOR_ENTITY.get(), CombustionGeneratorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockdustryBlocks.CORE_ENTITY.get(), CoreBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockdustryBlocks.UNIT_FACTORY_ENTITY.get(), UnitFactoryBlockEntityRenderer::new);
        event.registerEntityRenderer(BlockdustryEntities.BULLET.get(), BlockdustryBulletRenderer::new);
        event.registerEntityRenderer(BlockdustryEntities.DAGGER.get(), DaggerUnitRenderer::new);
        event.registerEntityRenderer(BlockdustryEntities.FLAK.get(), FlakBulletRenderer::new);
        // 火焰炮/电弧/提升机渲染器（FuseArcRegistrar/ElevatorBlocks 注册的实体类型）喵
        event.registerBlockEntityRenderer(com.blockdustry.building.FuseArcRegistrar.FUSE_ENTITY.get(), FuseBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(com.blockdustry.building.FuseArcRegistrar.ARC_ENTITY.get(), ArcBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(com.blockdustry.building.ElevatorBlocks.ELEVATOR_ENTITY.get(), ElevatorBlockEntityRenderer::new);
        event.registerEntityRenderer(com.blockdustry.building.FuseArcRegistrar.FIRE_BULLET.get(), FireBulletRenderer::new);
        event.registerEntityRenderer(com.blockdustry.building.FuseArcRegistrar.ARC_BEAM.get(), ArcBeamRenderer::new);
    }
}
