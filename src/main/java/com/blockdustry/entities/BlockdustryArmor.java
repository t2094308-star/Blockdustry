package com.blockdustry.entities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.BlockdustryBuildingEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

// Mindustry 装甲机制静态工具：固定减伤，最终伤害 = max(1, 原始伤害 - 装甲)喵
public final class BlockdustryArmor {
    // 实体类型 → 装甲值注册表（默认 0，未注册实体无装甲）喵
    private static final Map<EntityType<?>, Float> ENTITY_ARMOR = new ConcurrentHashMap<>();
    // 默认演示装甲是否已装入（dagger 装甲 3）喵
    private static boolean defaultsLoaded;

    private BlockdustryArmor() {}

    // 取活体/任意实体的装甲值：注册表命中返回，否则 0 喵
    public static float getArmor(Entity entity) {
        if (entity == null) return 0f;
        ensureDefaults();
        return ENTITY_ARMOR.getOrDefault(entity.getType(), 0f);
    }

    // 取建筑的装甲值（读建筑实体自身字段，默认 0）喵
    public static float getArmor(BlockdustryBuildingEntity building) {
        return building == null ? 0f : building.getArmor();
    }

    // 注册实体装甲（Mindustry 单位各有 armor；dagger 演示用）喵
    public static void setEntityArmor(EntityType<?> type, float armor) {
        if (type != null) ENTITY_ARMOR.put(type, Math.max(0f, armor));
    }

    // Mindustry 伤害折算：固定减伤，最少保留 1 点伤害（防无限无敌）喵
    public static float reduceDamage(float raw, float armor) {
        return Math.max(1f, raw - Math.max(0f, armor));
    }

    // 对实体折算（命中活体/单位用）喵
    public static float applyToEntity(Entity target, float raw) {
        return reduceDamage(raw, getArmor(target));
    }

    // 对建筑折算（命中建筑用，BlockHealthApi 无 armor 参数故调用前折算）喵
    public static float applyToBuilding(BlockdustryBuildingEntity target, float raw) {
        return reduceDamage(raw, getArmor(target));
    }

    // 惰性装入演示默认值：dagger 装甲 3（Mindustry 原版 armor=0，给 3 演示固定减伤）喵
    // 仅在运行时首次 getArmor 才解析实体类型，无注册顺序问题喵
    private static void ensureDefaults() {
        if (defaultsLoaded) return;
        defaultsLoaded = true;
        try {
            setEntityArmor(BlockdustryEntities.DAGGER.get(), 3f);
        } catch (Exception e) {
            Blockdustry.LOGGER.warn("BlockdustryArmor: dagger 默认装甲注册被跳过（实体尚未注册）", e);
        }
    }
}
