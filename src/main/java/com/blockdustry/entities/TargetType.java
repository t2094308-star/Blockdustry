package com.blockdustry.entities;

import net.minecraft.world.entity.LivingEntity;

// 目标标签（Mindustry 对空/对地语义的简化）：单位按「陆/空/海」标注，炮台按「对地/对空」过滤索敌喵
// 陆(GROUND) 与 海(SEA) 归为地面可锁定目标；空(AIR) 仅对空炮台锁定喵
public enum TargetType {
    GROUND,
    AIR,
    SEA;

    // 炮台过滤判定：filter 为炮台标签（对地=GROUND / 对空=AIR / 对海=SEA），unit 为目标单位标签喵
    // 对地炮：不锁定空（陆+海都打）；对空炮：只打空；对海炮：只打海（预留）喵
    public boolean canTarget(TargetType unit) {
        return switch (this) {
            case GROUND -> unit != AIR;
            case AIR -> unit == AIR;
            case SEA -> unit == SEA;
        };
    }

    // 任意活体的标签：Blockdustry 单位按各自 getTargetType()，其余活体（原版生物）按地面处理喵
    // 注意：无队伍的原版生物本身不会被索敌（DERELICT 目标永不敌对），此默认值只兜底喵
    public static TargetType of(LivingEntity entity) {
        if (entity instanceof DaggerUnitEntity unit) {
            return unit.getTargetType();
        }
        return GROUND;
    }
}
