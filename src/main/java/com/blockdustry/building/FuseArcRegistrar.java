package com.blockdustry.building;

import com.blockdustry.Blockdustry;
import com.blockdustry.entities.ArcBeamEntity;
import com.blockdustry.entities.FireBulletEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// fuse/arc 炮塔的独立注册类：方块 + 方块物品 + 方块实体 + 炮弹实体。
// 刻意不并入 BlockdustryBlocks/BlockdustryEntities（任务约束不碰共享注册文件），
// 主会话只需在 Blockdustry 构造器加一行 FuseArcRegistrar.register(modEventBus) 即挂载喵
public final class FuseArcRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Blockdustry.MODID);

    // —— 方块 ——
    public static final DeferredBlock<Block> FUSE = BLOCKS.register("fuse", FuseArcRegistrar::fuseBlock);
    public static final DeferredBlock<Block> ARC = BLOCKS.register("arc", FuseArcRegistrar::arcBlock);

    // —— 方块物品（fuse 3×3，arc 1×1）——
    public static final DeferredItem<BlockdustryBuildingItem> FUSE_ITEM =
            ITEMS.register("fuse", () -> new BlockdustryBuildingItem(FUSE.get(), new Item.Properties(), 3));
    public static final DeferredItem<BlockdustryBuildingItem> ARC_ITEM =
            ITEMS.register("arc", () -> new BlockdustryBuildingItem(ARC.get(), new Item.Properties(), 1));

    // —— 方块实体类型 ——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FuseBlockEntity>> FUSE_ENTITY =
            BLOCK_ENTITY_TYPES.register("fuse",
                    () -> BlockEntityType.Builder.of(FuseBlockEntity::new, FUSE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcBlockEntity>> ARC_ENTITY =
            BLOCK_ENTITY_TYPES.register("arc",
                    () -> BlockEntityType.Builder.of(ArcBlockEntity::new, ARC.get()).build(null));

    // —— 炮弹/电弧实体 ——
    public static final DeferredHolder<EntityType<?>, EntityType<FireBulletEntity>> FIRE_BULLET =
            ENTITY_TYPES.register("fire_bullet",
                    () -> EntityType.Builder.<FireBulletEntity>of(FireBulletEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .updateInterval(1)
                            .build("fire_bullet"));
    public static final DeferredHolder<EntityType<?>, EntityType<ArcBeamEntity>> ARC_BEAM =
            ENTITY_TYPES.register("arc_beam",
                    () -> EntityType.Builder.<ArcBeamEntity>of(ArcBeamEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .updateInterval(1)
                            .build("arc_beam"));

    // 工厂方法：延迟取实体类型，避免静态字段前向引用与注册期 unbound 喵
    private static BlockdustryBuildingBlock fuseBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(4f),
                () -> FUSE_ENTITY.get(),
                3);
    }

    private static BlockdustryBuildingBlock arcBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(4f),
                () -> ARC_ENTITY.get(),
                1);
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        ENTITY_TYPES.register(bus);
    }

    private FuseArcRegistrar() {}
}
