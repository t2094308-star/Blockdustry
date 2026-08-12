package com.blockdustry.entities;

import com.blockdustry.Blockdustry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// 方块工业实体类型注册喵
public final class BlockdustryEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Blockdustry.MODID);

    // Mindustry 炮弹实体（updateInterval=1：位置包每 tick 一发，配合客户端自模拟平滑飞行）喵
    public static final DeferredHolder<EntityType<?>, EntityType<BlockdustryBulletEntity>> BULLET =
            ENTITY_TYPES.register("bullet",
                    () -> EntityType.Builder.<BlockdustryBulletEntity>of(BlockdustryBulletEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .updateInterval(1)
                            .build("bullet"));

    // Mindustry dagger 地面单位（单位工厂产出，队伍附件 + 索敌攻击）喵
    public static final DeferredHolder<EntityType<?>, EntityType<DaggerUnitEntity>> DAGGER =
            ENTITY_TYPES.register("dagger",
                    () -> EntityType.Builder.<DaggerUnitEntity>of(DaggerUnitEntity::new, MobCategory.MISC)
                            .sized(0.6f, 0.6f)
                            .build("dagger"));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }

    private BlockdustryEntities() {}
}
