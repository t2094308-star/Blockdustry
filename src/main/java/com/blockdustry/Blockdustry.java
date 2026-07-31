package com.blockdustry;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// 方块工业 (Blockdustry) 主类，modId 需与 neoforge.mods.toml 中一致喵
@Mod(Blockdustry.MODID)
public class Blockdustry {
    // 统一的 mod id 常量喵
    public static final String MODID = "blockdustry";
    // slf4j 日志器喵
    public static final Logger LOGGER = LogUtils.getLogger();

    // FML 会自动向构造函数注入 IEventBus 等参数喵
    public Blockdustry(IEventBus modEventBus) {
        // 注册 commonSetup 到 mod 事件总线喵
        modEventBus.addListener(this::commonSetup);
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
