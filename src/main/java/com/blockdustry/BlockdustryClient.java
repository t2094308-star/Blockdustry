package com.blockdustry;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

// 仅客户端加载的类，不会在专用服务器上实例化喵
@Mod(value = Blockdustry.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Blockdustry.MODID, value = Dist.CLIENT)
public class BlockdustryClient {
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // 验证客户端加载成功的标记日志喵
        Blockdustry.LOGGER.info("方块工业: 客户端加载成功");
    }
}
