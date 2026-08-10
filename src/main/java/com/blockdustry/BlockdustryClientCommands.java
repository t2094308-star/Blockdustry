package com.blockdustry;

import com.blockdustry.lib.client.BlockHealthCrackCache;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = Blockdustry.MODID, value = Dist.CLIENT)
public class BlockdustryClientCommands {
    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("blockhealth").then(Commands.literal("crack")
                .then(Commands.literal("on").executes(ctx -> setCrack(ctx.getSource(), true)))
                .then(Commands.literal("off").executes(ctx -> setCrack(ctx.getSource(), false)))
                .then(Commands.literal("toggle").executes(ctx -> setCrack(ctx.getSource(), !BlockHealthCrackCache.isEnabled())))
                .executes(ctx -> queryCrack(ctx.getSource()))));
    }

    private static int setCrack(CommandSourceStack source, boolean on) {
        BlockHealthCrackCache.setEnabled(on);
        source.sendSuccess(() -> Component.literal(on ? "血量裂纹已开启" : "血量裂纹已关闭"), false);
        return 1;
    }

    private static int queryCrack(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(BlockHealthCrackCache.isEnabled() ? "血量裂纹：开启" : "血量裂纹：关闭"), false);
        return 1;
    }
}
