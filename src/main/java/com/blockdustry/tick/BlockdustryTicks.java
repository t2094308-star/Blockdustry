package com.blockdustry.tick;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.blockdustry.Blockdustry;
import com.blockdustry.config.BlockdustryConfig;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

// 模组全局 tick 引擎：本模组所有基于时间的系统统一挂在此处喵
// 间隔见 config tick.interval（1~20，默认 1 = 每游戏 tick 刷新一次，与原版同步）喵
@EventBusSubscriber(modid = Blockdustry.MODID)
public final class BlockdustryTicks {
    // 回调列表（并发安全，支持运行时注册/注销）喵
    private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();
    // 模组 tick 总计数（从服务器启动起）喵
    private static long tickCount;
    // 距上次模组 tick 经过的游戏 tick 数喵
    private static int elapsed;

    private BlockdustryTicks() {}

    // 每个游戏 tick 触发，按间隔到达时结算一次模组 tick 喵
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        int interval = BlockdustryConfig.TICK_INTERVAL.get();
        elapsed++;
        if (elapsed >= interval) {
            elapsed = 0;
            runTick();
        }
    }

    // 执行一次模组 tick：计数 + 遍历回调喵
    private static void runTick() {
        tickCount++;
        for (Runnable listener : LISTENERS) {
            try {
                listener.run();
            } catch (Throwable t) {
                Blockdustry.LOGGER.error("模组 tick 回调抛异常喵", t);
            }
        }
    }

    // 注册一个每模组 tick 执行的回调喵
    public static void register(Runnable listener) {
        LISTENERS.add(listener);
    }

    // 注销回调喵
    public static void unregister(Runnable listener) {
        LISTENERS.remove(listener);
    }

    // 当前模组 tick 计数喵
    public static long tickCount() {
        return tickCount;
    }

    // 当前间隔（游戏 tick 数）喵
    public static int interval() {
        return BlockdustryConfig.TICK_INTERVAL.get();
    }
}
