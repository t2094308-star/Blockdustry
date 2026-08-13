package com.blockdustry.client;

import com.blockdustry.Blockdustry;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

// 科技树快捷键注册（MOD bus，客户端）：独立于 BlockdustryClient，避免改共享注册文件喵
// 若主会话偏好统一管理，也可把 ResearchScreenHandler.KEY_RESEARCH 挪进 BlockdustryClient.registerKeyMappings，本类可删喵
@EventBusSubscriber(modid = Blockdustry.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ResearchKeyRegistrar {
    private ResearchKeyRegistrar() {}

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(ResearchScreenHandler.KEY_RESEARCH);
    }
}
