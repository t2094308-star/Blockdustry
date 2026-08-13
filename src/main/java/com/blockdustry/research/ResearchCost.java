package com.blockdustry.research;

// 研究成本公式（忠实 Mindustry Block.researchRequirements）喵
// 单物品研究量 = round10(60*mult + 建造量^1.11 * 20 * mult * 物品倍率) 喵
public final class ResearchCost {
    private ResearchCost() {}

    // 四舍五入到 10 的整数（Mindustry Mathf.round(value, 10)）喵
    public static int round10(double v) {
        return (int) Math.round(v / 10.0) * 10;
    }

    // 公式核心：mult=累计成本倍率，buildAmount=建造配方物量，perItem=按物品倍率（默认 1）喵
    public static int researchCost(float mult, int buildAmount, float perItem) {
        double raw = 60.0 * mult + Math.pow(buildAmount, 1.11) * 20.0 * mult * perItem;
        return round10(raw);
    }

    // 便捷重载：无按物品倍率（=1）喵
    public static int researchCost(float mult, int buildAmount) {
        return researchCost(mult, buildAmount, 1f);
    }
}
