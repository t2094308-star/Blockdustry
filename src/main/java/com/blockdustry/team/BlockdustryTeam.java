package com.blockdustry.team;

// Mindustry 原作队伍（core/src/mindustry/game/Team.java），derelict 为无主/中立喵
public enum BlockdustryTeam {
    DERELICT,   // 无主/中立：与所有队伍可交互、不敌对喵
    SHARDED,    // 玩家初始阵营（蓝）喵
    CRUX,       // 敌对（红）喵
    MALIS,      // 敌对（紫）喵
    GREEN,
    BLUE,
    NEOPLASTIC;

    // 原作 Team.canInteract：同队或对方为 derelict 即可交互喵
    public boolean canInteract(BlockdustryTeam other) {
        return this == other || other == DERELICT;
    }

    // 原作敌我语义：非 derelict 的异队互相敌对喵
    public boolean isEnemy(BlockdustryTeam other) {
        return this != other && this != DERELICT && other != DERELICT;
    }

    // 按名字解析，未知名（如旧存档 "NEUTRAL"）回落到 derelict 喵
    public static BlockdustryTeam byName(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return DERELICT;
        }
    }
}
