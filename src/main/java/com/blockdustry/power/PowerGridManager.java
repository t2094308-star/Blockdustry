package com.blockdustry.power;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.building.BlockdustryBuildings;
import com.blockdustry.tick.BlockdustryTicks;

import net.minecraft.core.BlockPos;

// 电网管理器：每模组 tick 用 Union-Find 重建连通（PowerNode.links），每网结算功率喵
public final class PowerGridManager {
    private static final List<BlockdustryPowerNode> ALL = new ArrayList<>();
    private static boolean hooked;

    private PowerGridManager() {}

    // 幂等挂到模组新 tick 喵
    public static void hook() {
        if (!hooked) {
            hooked = true;
            BlockdustryTicks.register(PowerGridManager::tick);
        }
    }

    private static void tick() {
        // 收集所有有电建筑喵
        ALL.clear();
        for (BlockdustryBuildingEntity b : BlockdustryBuildings.all()) {
            if (b instanceof BlockdustryPowerNode p) ALL.add(p);
        }
        int n = ALL.size();
        if (n == 0) return;

        // Union-Find：pos → 下标，按 PowerNode.links 合并喵
        Map<BlockPos, Integer> index = new HashMap<>();
        for (int i = 0; i < n; i++) {
            index.put(ALL.get(i).getPos(), i);
        }
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        for (int i = 0; i < n; i++) {
            for (BlockPos target : ALL.get(i).getPowerLinks()) {
                Integer j = index.get(target);
                if (j != null) union(parent, i, j);
            }
        }

        // 每连通分量一个电网，结算喵
        Map<Integer, PowerGrid> grids = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int root = find(parent, i);
            PowerGrid g = grids.computeIfAbsent(root, k -> new PowerGrid());
            g.add(ALL.get(i));
        }
        for (PowerGrid g : grids.values()) {
            g.update();
        }
    }

    private static int find(int[] parent, int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }

    private static void union(int[] parent, int a, int b) {
        int ra = find(parent, a);
        int rb = find(parent, b);
        if (ra != rb) parent[ra] = rb;
    }
}
