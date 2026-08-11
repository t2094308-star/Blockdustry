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
import com.blockdustry.config.BlockdustryConfig;
import com.blockdustry.entities.BlockdustryEntities;

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
        // 注册炮弹等实体类型喵
        BlockdustryEntities.register(modEventBus);
        // 建筑管理器挂模组新 tick 喵
        BlockdustryBuildings.hook();
        // 注册本类响应游戏事件（服务端启动等）喵
        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // 验证 mod 加载成功的标记日志喵
        LOGGER.info("方块工业 (blockdustry) 加载成功");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("方块工业: 服务器已启动");
    }
}
