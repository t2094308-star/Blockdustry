package com.blockdustry.building;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.blockdustry.Blockdustry;
import com.blockdustry.tick.BlockdustryTicks;

// 建筑管理器：维护所有已加载建筑，随模组新 tick 统一驱动喵
public final class BlockdustryBuildings {
    // 已加载建筑集合（并发安全）喵
    private static final Set<BlockdustryBuildingEntity> BUILDINGS =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static boolean hooked;

    private BlockdustryBuildings() {}

    public static void register(BlockdustryBuildingEntity b) {
        BUILDINGS.add(b);
    }

    public static void unregister(BlockdustryBuildingEntity b) {
        BUILDINGS.remove(b);
    }

    // 所有已加载建筑（只读遍历用）喵
    public static Set<BlockdustryBuildingEntity> all() {
        return BUILDINGS;
    }

    // 每个模组 tick 驱动所有建筑喵
    private static void tickAll() {
        for (BlockdustryBuildingEntity b : BUILDINGS) {
            try {
                b.tick();
            } catch (Throwable t) {
                Blockdustry.LOGGER.error("建筑 tick 异常 @ " + b.getBlockPos(), t);
            }
        }
    }

    // 把 tickAll 挂到模组新 tick（幂等，只挂一次）喵
    public static void hook() {
        if (!hooked) {
            hooked = true;
            BlockdustryTicks.register(BlockdustryBuildings::tickAll);
        }
    }
}
