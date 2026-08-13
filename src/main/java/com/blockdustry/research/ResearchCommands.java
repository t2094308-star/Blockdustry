package com.blockdustry.research;

import com.blockdustry.Blockdustry;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

// 科技树调试/管理指令：/blockdustry research unlockall（一键解锁所有科技）喵
// 独立于共享 BlockdustryCommands：@EventBusSubscriber 自动注册，Brigadier 同名 literal 自动合并子树喵
@EventBusSubscriber(modid = Blockdustry.MODID)
public final class ResearchCommands {
    private ResearchCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("blockdustry")
                .then(Commands.literal("research")
                        .then(Commands.literal("unlockall")
                                .requires(s -> s.hasPermission(2))
                                .executes(ctx -> unlockAll(ctx.getSource())))));
    }

    // 服务端：解锁所有非默认节点 + 广播全量状态给所有玩家喵
    private static int unlockAll(CommandSourceStack source) {
        if (!(source.getLevel() instanceof ServerLevel level)) return 0;
        int count = ResearchManager.unlockAll(level);
        PacketDistributor.sendToAllPlayers(ResearchManager.buildState(level));
        source.sendSuccess(() -> Component.literal("已解锁 " + count + " 个科技节点"), false);
        return 1;
    }
}
