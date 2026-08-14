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
import com.blockdustry.client.model.DaggerModel;
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

    // 注册实体模型层定义（dagger 3D 模型）喵
    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DaggerModel.LAYER_LOCATION, DaggerModel::createBodyLayer);
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
        event.registerBlockEntityRenderer(com.blockdustry.distribution.SorterRegistrar.SORTER_ENTITY.get(), com.blockdustry.client.SorterBlockEntityRenderer::new);
        // 批1B 存储+桥梁渲染器喵
        event.registerBlockEntityRenderer(com.blockdustry.storage.ContainerRegistrar.CONTAINER_ENTITY.get(), com.blockdustry.client.ContainerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(com.blockdustry.distribution.BridgeRegistrar.BRIDGE_ENTITY.get(), com.blockdustry.client.ItemBridgeBlockEntityRenderer::new);
        // 批1C/1D 钻机与生产渲染器喵
        event.registerBlockEntityRenderer(com.blockdustry.building.PneumaticDrillRegistrar.PNEUMATIC_DRILL_ENTITY.get(), com.blockdustry.client.PneumaticDrillBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(com.blockdustry.building.LaserDrillRegistrar.LASER_DRILL_ENTITY.get(), com.blockdustry.client.LaserDrillBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(com.blockdustry.building.BlastDrillRegistrar.BLAST_DRILL_ENTITY.get(), com.blockdustry.client.BlastDrillBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(com.blockdustry.building.SiliconSmelterRegistrar.SILICON_SMELTER_ENTITY.get(), com.blockdustry.client.SiliconSmelterBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(com.blockdustry.production.KilnRegistrar.KILN_ENTITY.get(), com.blockdustry.client.KilnBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(com.blockdustry.building.PlastaniumCompressorRegistrar.PLASTANIUM_COMPRESSOR_ENTITY.get(), com.blockdustry.client.PlastaniumCompressorBlockEntityRenderer::new);
        // 批1E 电力渲染器喵
        event.registerBlockEntityRenderer(com.blockdustry.power.PowerNodeLargeRegistrar.POWER_NODE_LARGE_ENTITY.get(), com.blockdustry.client.PowerNodeLargeBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(com.blockdustry.power.BatteryLargeRegistrar.BATTERY_LARGE_ENTITY.get(), com.blockdustry.client.BatteryLargeBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(com.blockdustry.building.DiodeSurgeTowerRegistrar.DIODE_ENTITY.get(), com.blockdustry.client.DiodeBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(com.blockdustry.building.DiodeSurgeTowerRegistrar.SURGE_TOWER_ENTITY.get(), com.blockdustry.client.SurgeTowerBlockEntityRenderer::new);
        // 批1F 门渲染器喵
        event.registerBlockEntityRenderer(com.blockdustry.defense.DefenseRegistrar.DOOR_ENTITY.get(), com.blockdustry.client.DoorBlockEntityRenderer::new);
        // 批1C 生产渲染器喵
        event.registerBlockEntityRenderer(com.blockdustry.production.PulverizerRegistrar.PULVERIZER_ENTITY.get(), com.blockdustry.client.PulverizerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(com.blockdustry.production.IncineratorRegistrar.INCINERATOR_ENTITY.get(), com.blockdustry.client.IncineratorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(com.blockdustry.building.PhaseWeaverRegistrar.PHASE_WEAVER_ENTITY.get(), com.blockdustry.client.PhaseWeaverBlockEntityRenderer::new);
        // 批2B 防御场渲染器喵
        event.registerBlockEntityRenderer(com.blockdustry.building.MenderRegistrar.MENDER_ENTITY.get(), com.blockdustry.client.MenderBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(com.blockdustry.building.ForceProjectorRegistrar.FORCE_PROJECTOR_ENTITY.get(), com.blockdustry.client.ForceProjectorBlockEntityRenderer::new);
        // 批2A surge 受击放电闪电实体渲染器喵
        event.registerEntityRenderer(com.blockdustry.defense.AdvancedWallRegistrar.WALL_LIGHTNING.get(), com.blockdustry.client.WallLightningRenderer::new);
        event.registerEntityRenderer(com.blockdustry.building.FuseArcRegistrar.FIRE_BULLET.get(), FireBulletRenderer::new);
        event.registerEntityRenderer(com.blockdustry.building.FuseArcRegistrar.ARC_BEAM.get(), ArcBeamRenderer::new);
    }
}
