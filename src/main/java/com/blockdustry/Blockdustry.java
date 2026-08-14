package com.blockdustry;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import com.blockdustry.building.BlockdustryBlocks;
import com.blockdustry.building.BlockdustryBuildings;
import com.blockdustry.building.ElevatorBlocks;
import com.blockdustry.building.FuseArcRegistrar;
import com.blockdustry.distribution.GateRegistrar;
import com.blockdustry.distribution.JunctionRegistrar;
import com.blockdustry.distribution.SorterRegistrar;
import com.blockdustry.config.BlockdustryConfig;
import com.blockdustry.entities.BlockdustryEntities;
import com.blockdustry.entities.DaggerUnitEntity;
import com.blockdustry.item.BlockdustryItems;
import com.blockdustry.power.PowerGridManager;
import com.blockdustry.possession.TurretPossessManager;

import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

// 方块工业 (Blockdustry) 主类，modId 需与 neoforge.mods.toml 中一致喵
@Mod(Blockdustry.MODID)
public class Blockdustry {
    // 统一的 mod id 常量喵
    public static final String MODID = "blockdustry";
    // slf4j 日志器喵
    public static final Logger LOGGER = LogUtils.getLogger();

    // FML 会自动向构造函数注入 IEventBus、ModContainer 等参数喵
    public Blockdustry(IEventBus modEventBus, ModContainer container) {
        // 注册 commonSetup 到 mod 事件总线喵
        modEventBus.addListener(this::commonSetup);
        // 注册 COMMON 配置（tick 间隔等）喵
        container.registerConfig(ModConfig.Type.COMMON, BlockdustryConfig.SPEC);
        // 注册队伍附件类型喵
        BlockdustryAttachments.ATTACHMENT_TYPES.register(modEventBus);
        // 注册建筑方块与方块实体喵
        BlockdustryBlocks.register(modEventBus);
        // 注册 Mindustry 材料物品（独立注册类）喵
        BlockdustryItems.register(modEventBus);
        // 火焰炮/电弧自包含注册（FuseArcRegistrar）与垂直提升机（ElevatorBlocks）喵
        FuseArcRegistrar.register(modEventBus);
        ElevatorBlocks.register(modEventBus);
        // 批1A 物流扩展：junction/distributor、sorter/invertedSorter、overflowGate/underflowGate 喵
        JunctionRegistrar.register(modEventBus);
        SorterRegistrar.register(modEventBus);
        GateRegistrar.register(modEventBus);
        // 批1B 存储+桥梁：container、bridge-conveyor 喵
        com.blockdustry.storage.ContainerRegistrar.register(modEventBus);
        com.blockdustry.distribution.BridgeRegistrar.register(modEventBus);
        // 批1C/1D 生产与钻机：气动/激光/爆破钻头 + 硅冶炼/窑炉/塑钢压缩机 喵
        com.blockdustry.building.PneumaticDrillRegistrar.register(modEventBus);
        com.blockdustry.building.LaserDrillRegistrar.register(modEventBus);
        com.blockdustry.building.BlastDrillRegistrar.register(modEventBus);
        com.blockdustry.building.SiliconSmelterRegistrar.register(modEventBus);
        com.blockdustry.production.KilnRegistrar.register(modEventBus);
        com.blockdustry.building.PlastaniumCompressorRegistrar.register(modEventBus);
        // 批1E/1F/2A/2B：太阳能/大型节点电池/二极管涌电塔、墙体与门、硫化物/粉碎/焚烧/相织布、修理器/力墙 喵
        com.blockdustry.power.SolarPanelRegistrar.register(modEventBus);
        com.blockdustry.power.PowerNodeLargeRegistrar.register(modEventBus);
        com.blockdustry.power.BatteryLargeRegistrar.register(modEventBus);
        com.blockdustry.building.DiodeSurgeTowerRegistrar.register(modEventBus);
        com.blockdustry.defense.DefenseRegistrar.register(modEventBus);
        com.blockdustry.production.PyratiteMixerRegistrar.register(modEventBus);
        com.blockdustry.production.PulverizerRegistrar.register(modEventBus);
        com.blockdustry.production.IncineratorRegistrar.register(modEventBus);
        com.blockdustry.building.PhaseWeaverRegistrar.register(modEventBus);
        com.blockdustry.defense.AdvancedWallRegistrar.register(modEventBus);
        com.blockdustry.building.MenderRegistrar.register(modEventBus);
        com.blockdustry.building.ForceProjectorRegistrar.register(modEventBus);
        // 注册炮弹等实体类型喵
        BlockdustryEntities.register(modEventBus);
        // 注册实体属性（dagger 等）喵
        modEventBus.addListener(Blockdustry::registerAttributes);
        // 建筑管理器挂模组新 tick 喵
        BlockdustryBuildings.hook();
        // 电网管理器挂模组新 tick 喵
        PowerGridManager.hook();
        // 炮台附身管理器挂模组新 tick 喵
        TurretPossessManager.hook();
        // 注册本类响应游戏事件（服务端启动等）喵
        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // 多格建筑整组共享血量：注册各多格建筑的组总血（commonSetup 双端执行，客户端 tooltip/挖掘进度也用）喵
        BlockdustryBlocks.registerBlockHealthDefaults();
        // 验证 mod 加载成功的标记日志喵
        LOGGER.info("方块工业 (blockdustry) 加载成功");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("方块工业: 服务器已启动");
    }

    // 注册实体属性（Mindustry dagger：血量150/速度0.3/攻击10）喵
    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(BlockdustryEntities.DAGGER.get(), DaggerUnitEntity.createAttributes().build());
    }
}
