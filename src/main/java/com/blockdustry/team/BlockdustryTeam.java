package com.blockdustry.team;

// Mindustry 式队伍：NEUTRAL 中立，其余非中立队伍两两敌对喵
public enum BlockdustryTeam {
    NEUTRAL,
    SHARDED,
    CRUX,
    MALIS,
    GREEN,
    BLUE;

    public boolean isEnemy(BlockdustryTeam other) {
        return this != other && this != NEUTRAL && other != NEUTRAL;
    }

    public boolean canInteract(BlockdustryTeam other) {
        return this == other || other == NEUTRAL;
    }
}
