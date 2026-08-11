package com.blockdustry.power;

import java.util.ArrayList;
import java.util.List;

// 电网：一个连通集合，每 tick 结算总产/总耗/电池补差/覆盖率分配（Mindustry PowerGraph）喵
public class PowerGrid {
    private final List<BlockdustryPowerNode> members = new ArrayList<>();
    private float lastStatus = 0f;

    public void add(BlockdustryPowerNode node) {
        if (!members.contains(node)) members.add(node);
    }

    public List<BlockdustryPowerNode> members() {
        return members;
    }

    public float getStatus() {
        return lastStatus;
    }

    // 每 tick 结算：总产/总耗、电池放电补差/盈余充电、按覆盖率赋 status 喵
    public void update() {
        float produced = 0f;
        float needed = 0f;
        for (BlockdustryPowerNode n : members) {
            produced += n.powerProduction();
            needed += n.powerNeeded();
        }
        // 需求 > 产出 → 电池放电补差喵
        if (needed > produced) {
            float deficit = needed - produced;
            float used = useBatteries(deficit);
            produced += used;
        } else if (produced > needed) {
            // 产出 > 需求 → 盈余充电池喵
            float excess = produced - needed;
            produced -= chargeBatteries(excess);
        }
        float coverage = (needed <= 0f) ? 1f : Math.min(1f, produced / Math.max(needed, 1e-6f));
        lastStatus = coverage;
        for (BlockdustryPowerNode n : members) {
            n.setPowerStatus(coverage);
        }
    }

    // 电池放电：等比例从所有电池扣电，返回实际放出喵
    private float useBatteries(float needed) {
        float stored = 0f;
        for (BlockdustryPowerNode n : members) {
            if (n.powerCapacity() > 0f) stored += n.powerStored();
        }
        if (stored <= 0f) return 0f;
        float used = Math.min(stored, needed);
        float percent = Math.min(1f, needed / stored);
        for (BlockdustryPowerNode n : members) {
            if (n.powerCapacity() > 0f) {
                n.setPowerStatus(Math.max(0f, n.getPowerStatus() - percent));
            }
        }
        return used;
    }

    // 电池充电：盈余按剩余容量均匀充进电池，返回实际充入喵
    private float chargeBatteries(float excess) {
        float capacity = 0f;
        for (BlockdustryPowerNode n : members) {
            if (n.powerCapacity() > 0f) capacity += (1f - n.getPowerStatus()) * n.powerCapacity();
        }
        if (capacity <= 0f) return 0f;
        float chargedPercent = Math.min(excess / capacity, 1f);
        for (BlockdustryPowerNode n : members) {
            if (n.powerCapacity() > 0f) {
                n.setPowerStatus(Math.min(1f, n.getPowerStatus() + (1f - n.getPowerStatus()) * chargedPercent));
            }
        }
        return Math.min(excess, capacity);
    }
}
