package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

// 科技树快捷键（J，Mindustry research 也是 J）：按下打开/关闭研究面板喵
// KeyMapping 注册由 ResearchKeyRegistrar（MOD bus）完成；本类只在 FORGE bus 检测按键喵
@EventBusSubscriber(modid = Blockdustry.MODID, value = Dist.CLIENT)
public final class ResearchScreenHandler {
    public static final KeyMapping KEY_RESEARCH = new KeyMapping(
            "key.blockdustry.research",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_J,
            "key.categories.blockdustry");

    private ResearchScreenHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        while (KEY_RESEARCH.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new ResearchScreen());
            }
        }
    }
}
