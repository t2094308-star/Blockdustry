package com.blockdustry;

import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.lib.BlockHealthApi;
import com.blockdustry.team.BlockdustryTeam;
import com.blockdustry.tick.BlockdustryTicks;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

// /blockhealth 调试命令，用于实测方块血量 API 喵
@EventBusSubscriber(modid = Blockdustry.MODID)
public class BlockdustryCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("blockhealth")
                .requires(s -> s.hasPermission(2))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(ctx -> query(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos"))))
                .then(Commands.literal("damage")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0f))
                                        .executes(ctx -> damage(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos"), FloatArgumentType.getFloat(ctx, "amount"))))))
                .then(Commands.literal("heal")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0f))
                                        .executes(ctx -> heal(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos"), FloatArgumentType.getFloat(ctx, "amount"))))))
                .then(Commands.literal("reset")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(ctx -> reset(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos"))))));

        dispatcher.register(Commands.literal("blockdustry")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("team")
                        .then(Commands.literal("get")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> teamGet(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos")))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("team", StringArgumentType.word())
                                                .executes(ctx -> teamSet(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos"), StringArgumentType.getString(ctx, "team"))))))
                        .then(Commands.literal("player")
                                .then(Commands.argument("team", StringArgumentType.word())
                                        .executes(ctx -> teamPlayer(ctx.getSource(), StringArgumentType.getString(ctx, "team"))))))
                .then(Commands.literal("tick")
                        .executes(ctx -> tickStatus(ctx.getSource())))
                .then(Commands.literal("building")
                        .then(Commands.literal("get")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> buildingGet(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos")))))));
    }

    private static ServerLevel serverLevel(CommandSourceStack source) {
        return source.getLevel() instanceof ServerLevel sl ? sl : null;
    }

    private static int query(CommandSourceStack source, BlockPos pos) {
        ServerLevel level = serverLevel(source);
        if (level == null) return 0;
        float max = BlockHealthApi.getMaxHp(level, pos);
        float hp = BlockHealthApi.getHp(level, pos);
        float fraction = BlockHealthApi.getHpFraction(level, pos);
        source.sendSuccess(() -> Component.literal(
                String.format("方块血量 @ %s: hp=%.1f/%.1f (%.0f%%)", pos.toShortString(), hp, max, fraction * 100)), false);
        return 1;
    }

    private static int damage(CommandSourceStack source, BlockPos pos, float amount) {
        ServerLevel level = serverLevel(source);
        if (level == null) return 0;
        boolean broken = BlockHealthApi.damage(level, pos, amount, source.getEntity());
        source.sendSuccess(() -> Component.literal(
                String.format("对 %s 造成 %.1f 伤害，%s", pos.toShortString(), amount, broken ? "方块被击破" : "方块未击破")), false);
        return 1;
    }

    private static int heal(CommandSourceStack source, BlockPos pos, float amount) {
        ServerLevel level = serverLevel(source);
        if (level == null) return 0;
        BlockHealthApi.heal(level, pos, amount);
        source.sendSuccess(() -> Component.literal(
                String.format("治疗 %s %.1f，现血量 %.1f", pos.toShortString(), amount, BlockHealthApi.getHp(level, pos))), false);
        return 1;
    }

    private static int reset(CommandSourceStack source, BlockPos pos) {
        ServerLevel level = serverLevel(source);
        if (level == null) return 0;
        BlockHealthApi.remove(level, pos);
        source.sendSuccess(() -> Component.literal(String.format("已重置 %s 的血量记录", pos.toShortString())), false);
        return 1;
    }

    private static int teamGet(CommandSourceStack source, BlockPos pos) {
        ServerLevel level = serverLevel(source);
        if (level == null) return 0;
        BlockdustryTeam team = BlockdustryTeams.getTeam(level, pos);
        BlockdustryTeam mine = source.getEntity() != null ? BlockdustryTeams.getTeam(source.getEntity()) : BlockdustryTeam.DERELICT;
        source.sendSuccess(() -> Component.literal(
                String.format("%s 的队伍：%s（%s）", pos.toShortString(), team, BlockdustryTeams.isEnemy(mine, team) ? "敌对" : "非敌对")), false);
        return 1;
    }

    private static int teamSet(CommandSourceStack source, BlockPos pos, String name) {
        ServerLevel level = serverLevel(source);
        if (level == null) return 0;
        try {
            BlockdustryTeam team = BlockdustryTeam.valueOf(name.toUpperCase());
            BlockdustryTeams.setTeam(level, pos, team);
            source.sendSuccess(() -> Component.literal(String.format("%s 的队伍设为 %s", pos.toShortString(), team)), false);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("无效队伍名：" + name));
        }
        return 1;
    }

    private static int teamPlayer(CommandSourceStack source, String name) {
        if (source.getEntity() == null) return 0;
        try {
            BlockdustryTeam team = BlockdustryTeam.valueOf(name.toUpperCase());
            BlockdustryTeams.setTeam(source.getEntity(), team);
            source.sendSuccess(() -> Component.literal("你的队伍设为 " + team), false);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("无效队伍名：" + name));
        }
        return 1;
    }

    private static int tickStatus(CommandSourceStack source) {
        int interval = BlockdustryTicks.interval();
        source.sendSuccess(() -> Component.literal(
                String.format("模组 tick 间隔：%d 游戏 tick（每秒 %.1f 次），模组 tick 计数：%d（游戏 tick：%d）",
                        interval, 20.0 / interval, BlockdustryTicks.tickCount(),
                        source.getServer().getTickCount())), false);
        return 1;
    }

    private static int buildingGet(CommandSourceStack source, BlockPos pos) {
        ServerLevel level = serverLevel(source);
        if (level == null) return 0;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BlockdustryBuildingEntity b)) {
            source.sendFailure(Component.literal(pos.toShortString() + " 不是方块工业建筑"));
            return 0;
        }
        String inv = b.getStoredItem() == null
                ? "空"
                : b.getStoredItem().getDescription().getString() + " " + b.getStoredCount() + "/" + b.getCapacity();
        source.sendSuccess(() -> Component.literal(
                String.format("建筑：%s @ %s%n队伍：%s%n库存：%s%n模组 tick：%d",
                        be.getBlockState().getBlock().getName().getString(), pos.toShortString(),
                        b.getTeam(), inv, BlockdustryTicks.tickCount())), false);
        return 1;
    }
}
