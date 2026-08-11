package com.blockdustry.building;

import com.blockdustry.Blockdustry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// 建筑方块注册：方块 + 方块物品 + 方块实体 + 独立创造栏 tab 喵
public final class BlockdustryBlocks {
    // 方块注册表喵
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    // 物品注册表（方块物品）喵
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    // 方块实体类型注册表喵
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);
    // 创造物品栏标签页注册表喵
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Blockdustry.MODID);

    // 演示建筑 1：采集机（Mindustry mechanical-drill），方法间接引用避免前向引用喵
    public static final DeferredBlock<Block> DRILL = BLOCKS.register("drill", BlockdustryBlocks::drillBlock);
    // 演示建筑 2：炮塔（Mindustry duo）喵
    public static final DeferredBlock<Block> TURRET = BLOCKS.register("turret", BlockdustryBlocks::turretBlock);

    // 方块物品（多格放置：drill 2×2，turret 1×1）喵
    public static final DeferredItem<BlockdustryBuildingItem> DRILL_ITEM =
            ITEMS.register("drill", () -> new BlockdustryBuildingItem(DRILL.get(), new Item.Properties(), 2));
    public static final DeferredItem<BlockdustryBuildingItem> TURRET_ITEM =
            ITEMS.register("turret", () -> new BlockdustryBuildingItem(TURRET.get(), new Item.Properties(), 1));
    // 队伍调试棒（debug 物品，右键打开队伍 UI）喵
    public static final DeferredItem<Item> DEBUG_STICK =
            ITEMS.register("debug_stick", () -> new Item(new Item.Properties()));

    // 方块实体类型（实体类由 demo 子任务实现）喵
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DrillBlockEntity>> DRILL_ENTITY =
            BLOCK_ENTITY_TYPES.register("drill",
                    () -> BlockEntityType.Builder.of(DrillBlockEntity::new, DRILL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TurretBlockEntity>> TURRET_ENTITY =
            BLOCK_ENTITY_TYPES.register("turret",
                    () -> BlockEntityType.Builder.of(TurretBlockEntity::new, TURRET.get()).build(null));

    // 独立「方块工业」创造栏 tab 喵
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            CREATIVE_TABS.register("blockdustry",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.blockdustry"))
                            .icon(() -> new ItemStack((ItemLike) DRILL_ITEM.get()))
                            .displayItems((params, output) -> {
                                output.accept(DRILL_ITEM);
                                output.accept(TURRET_ITEM);
                            })
                            .build());

    // 工厂方法：延迟取实体类型，避免静态字段前向引用与注册期 unbound 喵
    private static BlockdustryBuildingBlock drillBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3f),
                () -> DRILL_ENTITY.get(),
                2);
    }

    private static BlockdustryBuildingBlock turretBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(4f),
                () -> TURRET_ENTITY.get(),
                1);
    }

    // 注册到 mod 事件总线喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        CREATIVE_TABS.register(bus);
    }

    private BlockdustryBlocks() {}
}
