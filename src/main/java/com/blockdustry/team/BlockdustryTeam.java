package com.blockdustry.team;

// Mindustry 原作队伍（core/src/mindustry/game/Team.java），derelict 为无主/中立；color 忠于原作 Team 构造喵
public enum BlockdustryTeam {
    DERELICT(0xFF6f6f6f),   // 无主/中立：与所有队伍可交互、不敌对（灰）喵
    SHARDED(0xFF5b7cfa),    // 玩家初始阵营（蓝）喵
    CRUX(0xFFcc4f4f),       // 敌对（红）喵
    MALIS(0xFF7b5b9e),      // 敌对（紫）喵
    GREEN(0xFF49d13c),
    BLUE(0xFF59cbe8),
    NEOPLASTIC(0xFFf2a7a0);

    private final int color; // ARGB，渲染用喵

    BlockdustryTeam(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

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
