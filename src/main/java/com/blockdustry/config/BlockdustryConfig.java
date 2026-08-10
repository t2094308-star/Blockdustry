package com.blockdustry.config;

import net.neoforged.neoforge.common.ModConfigSpec;

// 方块工业全局配置（COMMON）喵
public final class BlockdustryConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 模组 tick 间隔（游戏 tick 数）：1 = 每游戏 tick 刷新（1秒20次，最快最耗性能），20 = 每20游戏 tick 刷新（1秒1次，最省）喵
    public static final ModConfigSpec.IntValue TICK_INTERVAL =
            BUILDER.comment("模组 tick 间隔（游戏 tick 数），范围 1~20，默认 1（每游戏 tick 刷新一次，与原版同步）喵")
                    .defineInRange("tick.interval", 1, 1, 20);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private BlockdustryConfig() {}
}
