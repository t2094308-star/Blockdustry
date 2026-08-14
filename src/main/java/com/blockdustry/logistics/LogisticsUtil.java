package com.blockdustry.logistics;

import com.blockdustry.building.SorterBlockEntity;
import com.blockdustry.distribution.GateBlockEntity;

// Mindustry instantTransfer 统一判定 + 瞬时传递递归深度兜底喵。
// 已核实原版：仅 Sorter 与 OverflowGate（overflow/underflow 共用基类）置 instantTransfer=true，
// Router 不是（Router.updateTile 用 instanceof Router 特判正因它非 instantTransfer）喵。
// sorter/gate 与 gate/sorter 等「瞬时→瞬时」直连必须在双方 acceptItem 里都拒收，否则同一 tick
// acceptItem/handleItem 同步递归 → StackOverflow / 服务端卡死；本工具供两者共用同一谓词喵
public final class LogisticsUtil {
    // 瞬时谓词：Sorter | Gate（排除 Router）；兼容任意对象（BlockEntity / ItemSink / ItemSource 接口）喵
    public static boolean isInstant(Object o) {
        return o instanceof SorterBlockEntity || o instanceof GateBlockEntity;
    }

    // 瞬时传递递归深度兜底：即便谓词漏判，也能在深度超限时安全终止递归（防御性，避免卡死）喵
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final int MAX_DEPTH = 16;

    // 进入一次瞬时移交：超过深度上限返回 false（拒绝继续转发，安全终止递归链）喵
    public static boolean enterTransfer() {
        int d = DEPTH.get();
        if (d >= MAX_DEPTH) return false;
        DEPTH.set(d + 1);
        return true;
    }

    // 离开一次瞬时移交（须与 enterTransfer 配对，放 finally）喵
    public static void exitTransfer() {
        int d = DEPTH.get();
        if (d > 0) DEPTH.set(d - 1);
    }

    private LogisticsUtil() {}
}
