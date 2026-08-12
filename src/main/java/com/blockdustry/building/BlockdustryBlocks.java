package com.blockdustry.building;

import com.blockdustry.Blockdustry;
import com.blockdustry.power.BatteryBlockEntity;
import com.blockdustry.power.CombustionGeneratorBlockEntity;
import com.blockdustry.power.PowerNodeBlockEntity;

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

    // 物流：传送带 + Router（方法间接引用避免前向引用）喵
    public static final DeferredBlock<Block> CONVEYOR = BLOCKS.register("conveyor", BlockdustryBlocks::conveyorBlock);
    public static final DeferredBlock<Block> ROUTER = BLOCKS.register("router", BlockdustryBlocks::routerBlock);
    public static final DeferredItem<BlockdustryBuildingItem> CONVEYOR_ITEM =
            ITEMS.register("conveyor", () -> new BlockdustryBuildingItem(CONVEYOR.get(), new Item.Properties(), 1));
    public static final DeferredItem<BlockdustryBuildingItem> ROUTER_ITEM =
            ITEMS.register("router", () -> new BlockdustryBuildingItem(ROUTER.get(), new Item.Properties(), 1));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConveyorBlockEntity>> CONVEYOR_ENTITY =
            BLOCK_ENTITY_TYPES.register("conveyor",
                    () -> BlockEntityType.Builder.of(ConveyorBlockEntity::new, CONVEYOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RouterBlockEntity>> ROUTER_ENTITY =
            BLOCK_ENTITY_TYPES.register("router",
                    () -> BlockEntityType.Builder.of(RouterBlockEntity::new, ROUTER.get()).build(null));

    // 石墨物品（Mindustry 原料，MC 无此物品）喵
    public static final DeferredItem<Item> GRAPHITE = ITEMS.register("graphite", () -> new Item(new Item.Properties()));
    // 单位配方原料（Mindustry 忠于原作）：硅 + 铅喵
    public static final DeferredItem<Item> SILICON = ITEMS.register("silicon", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> LEAD = ITEMS.register("lead", () -> new Item(new Item.Properties()));

    // 加工工厂：石墨压缩机（2×2，吃 2 煤产 1 石墨）喵
    public static final DeferredBlock<Block> GRAPHITE_PRESS = BLOCKS.register("graphite_press", BlockdustryBlocks::graphitePressBlock);
    public static final DeferredItem<BlockdustryBuildingItem> GRAPHITE_PRESS_ITEM =
            ITEMS.register("graphite_press", () -> new BlockdustryBuildingItem(GRAPHITE_PRESS.get(), new Item.Properties(), 2));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GraphitePressBlockEntity>> GRAPHITE_PRESS_ENTITY =
            BLOCK_ENTITY_TYPES.register("graphite_press",
                    () -> BlockEntityType.Builder.of(GraphitePressBlockEntity::new, GRAPHITE_PRESS.get()).build(null));

    // 电力：电力节点 + 燃烧发电机 + 电池（方法间接引用避免前向引用）喵
    public static final DeferredBlock<Block> POWER_NODE = BLOCKS.register("power_node", BlockdustryBlocks::powerNodeBlock);
    public static final DeferredBlock<Block> COMBUSTION_GENERATOR = BLOCKS.register("combustion_generator", BlockdustryBlocks::combustionGeneratorBlock);
    public static final DeferredBlock<Block> BATTERY = BLOCKS.register("battery", BlockdustryBlocks::batteryBlock);
    public static final DeferredBlock<Block> CORE = BLOCKS.register("core", BlockdustryBlocks::coreBlock);
    public static final DeferredBlock<Block> UNIT_FACTORY = BLOCKS.register("unit_factory", BlockdustryBlocks::unitFactoryBlock);
    public static final DeferredItem<BlockdustryBuildingItem> POWER_NODE_ITEM =
            ITEMS.register("power_node", () -> new BlockdustryBuildingItem(POWER_NODE.get(), new Item.Properties(), 1));
    public static final DeferredItem<BlockdustryBuildingItem> COMBUSTION_GENERATOR_ITEM =
            ITEMS.register("combustion_generator", () -> new BlockdustryBuildingItem(COMBUSTION_GENERATOR.get(), new Item.Properties(), 1));
    public static final DeferredItem<BlockdustryBuildingItem> BATTERY_ITEM =
            ITEMS.register("battery", () -> new BlockdustryBuildingItem(BATTERY.get(), new Item.Properties(), 1));
    public static final DeferredItem<BlockdustryBuildingItem> CORE_ITEM =
            ITEMS.register("core", () -> new BlockdustryBuildingItem(CORE.get(), new Item.Properties(), 3));
    public static final DeferredItem<BlockdustryBuildingItem> UNIT_FACTORY_ITEM =
            ITEMS.register("unit_factory", () -> new BlockdustryBuildingItem(UNIT_FACTORY.get(), new Item.Properties(), 3));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PowerNodeBlockEntity>> POWER_NODE_ENTITY =
            BLOCK_ENTITY_TYPES.register("power_node",
                    () -> BlockEntityType.Builder.of((pos, state) -> new PowerNodeBlockEntity(pos, state), POWER_NODE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CombustionGeneratorBlockEntity>> COMBUSTION_GENERATOR_ENTITY =
            BLOCK_ENTITY_TYPES.register("combustion_generator",
                    () -> BlockEntityType.Builder.of((pos, state) -> new CombustionGeneratorBlockEntity(pos, state), COMBUSTION_GENERATOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BatteryBlockEntity>> BATTERY_ENTITY =
            BLOCK_ENTITY_TYPES.register("battery",
                    () -> BlockEntityType.Builder.of((pos, state) -> new BatteryBlockEntity(pos, state), BATTERY.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CoreBlockEntity>> CORE_ENTITY =
            BLOCK_ENTITY_TYPES.register("core",
                    () -> BlockEntityType.Builder.of((pos, state) -> new CoreBlockEntity(pos, state), CORE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UnitFactoryBlockEntity>> UNIT_FACTORY_ENTITY =
            BLOCK_ENTITY_TYPES.register("unit_factory",
                    () -> BlockEntityType.Builder.of((pos, state) -> new UnitFactoryBlockEntity(pos, state), UNIT_FACTORY.get()).build(null));

    // 沙盒调试：物品源（无限凭空产煤/石墨等，Mindustry item-source）喵
    public static final DeferredBlock<Block> ITEM_SOURCE = BLOCKS.register("item_source", BlockdustryBlocks::itemSourceBlock);
    public static final DeferredItem<BlockdustryBuildingItem> ITEM_SOURCE_ITEM =
            ITEMS.register("item_source", () -> new BlockdustryBuildingItem(ITEM_SOURCE.get(), new Item.Properties(), 1));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemSourceBlockEntity>> ITEM_SOURCE_ENTITY =
            BLOCK_ENTITY_TYPES.register("item_source",
                    () -> BlockEntityType.Builder.of((pos, state) -> new ItemSourceBlockEntity(pos, state), ITEM_SOURCE.get()).build(null));

    // 独立「方块工业」创造栏 tab 喵
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            CREATIVE_TABS.register("blockdustry",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.blockdustry"))
                            .icon(() -> new ItemStack((ItemLike) DRILL_ITEM.get()))
                            .displayItems((params, output) -> {
                                output.accept(DRILL_ITEM);
                                output.accept(TURRET_ITEM);
                                output.accept(CONVEYOR_ITEM);
                                output.accept(ROUTER_ITEM);
                                output.accept(GRAPHITE_PRESS_ITEM);
                                output.accept(GRAPHITE);
                                output.accept(SILICON);
                                output.accept(LEAD);
                                output.accept(POWER_NODE_ITEM);
                                output.accept(COMBUSTION_GENERATOR_ITEM);
                                output.accept(BATTERY_ITEM);
                                output.accept(CORE_ITEM);
                                output.accept(UNIT_FACTORY_ITEM);
                                output.accept(ITEM_SOURCE_ITEM);
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

    private static BlockdustryBuildingBlock conveyorBlock() {
        return new ConveyorBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2f),
                () -> CONVEYOR_ENTITY.get());
    }

    private static BlockdustryBuildingBlock routerBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2f),
                () -> ROUTER_ENTITY.get(),
                1);
    }

    private static BlockdustryBuildingBlock graphitePressBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(4f),
                () -> GRAPHITE_PRESS_ENTITY.get(),
                2);
    }

    private static BlockdustryBuildingBlock powerNodeBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2f),
                () -> POWER_NODE_ENTITY.get(),
                1);
    }

    private static BlockdustryBuildingBlock combustionGeneratorBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3f),
                () -> COMBUSTION_GENERATOR_ENTITY.get(),
                1);
    }

    private static BlockdustryBuildingBlock batteryBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3f),
                () -> BATTERY_ENTITY.get(),
                1);
    }

    private static BlockdustryBuildingBlock coreBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(12f),
                () -> CORE_ENTITY.get(),
                3);
    }

    private static BlockdustryBuildingBlock unitFactoryBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(5f),
                () -> UNIT_FACTORY_ENTITY.get(),
                3);
    }

    // 物品源方块工厂：1×1，易碎（调试方块），实体类型延迟解析喵
    private static BlockdustryBuildingBlock itemSourceBlock() {
        return new ItemSourceBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(0.5f),
                () -> ITEM_SOURCE_ENTITY.get(),
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
