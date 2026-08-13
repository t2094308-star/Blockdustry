package com.blockdustry.building;

import com.blockdustry.Blockdustry;
import com.blockdustry.item.BlockdustryItems;
import com.blockdustry.lib.BlockHealthApi;
import com.blockdustry.power.BatteryBlockEntity;
import com.blockdustry.power.CombustionGeneratorBlockEntity;
import com.blockdustry.power.PowerNodeBlockEntity;
import com.blockdustry.power.PowerSourceBlockEntity;

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
    // 对空炮塔（Mindustry scatter，分裂）喵
    public static final DeferredBlock<Block> SCATTER = BLOCKS.register("scatter", BlockdustryBlocks::scatterBlock);

    // 方块物品（多格放置：drill 2×2，turret 1×1）喵
    public static final DeferredItem<BlockdustryBuildingItem> DRILL_ITEM =
            ITEMS.register("drill", () -> new BlockdustryBuildingItem(DRILL.get(), new Item.Properties(), 2));
    public static final DeferredItem<BlockdustryBuildingItem> TURRET_ITEM =
            ITEMS.register("turret", () -> new BlockdustryBuildingItem(TURRET.get(), new Item.Properties(), 1));
    // 对空炮塔（Mindustry scatter，分裂）：原作 2×2，四象限跨格模型 + 2×2 专属基座贴图喵
    public static final DeferredItem<BlockdustryBuildingItem> SCATTER_ITEM =
            ITEMS.register("scatter", () -> new BlockdustryBuildingItem(SCATTER.get(), new Item.Properties(), 2));
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
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ScatterBlockEntity>> SCATTER_ENTITY =
            BLOCK_ENTITY_TYPES.register("scatter",
                    () -> BlockEntityType.Builder.of((pos, state) -> new ScatterBlockEntity(pos, state), SCATTER.get()).build(null));

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
    // debug 电力源（Mindustry sandbox power-source，无限产电）喵
    public static final DeferredBlock<Block> POWER_SOURCE = BLOCKS.register("power_source", BlockdustryBlocks::powerSourceBlock);
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
    public static final DeferredItem<BlockdustryBuildingItem> POWER_SOURCE_ITEM =
            ITEMS.register("power_source", () -> new BlockdustryBuildingItem(POWER_SOURCE.get(), new Item.Properties(), 1));
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
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PowerSourceBlockEntity>> POWER_SOURCE_ENTITY =
            BLOCK_ENTITY_TYPES.register("power_source",
                    () -> BlockEntityType.Builder.of((pos, state) -> new PowerSourceBlockEntity(pos, state), POWER_SOURCE.get()).build(null));

    // 沙盒调试：物品源（无限凭空产煤/石墨等，Mindustry item-source）喵
    public static final DeferredBlock<Block> ITEM_SOURCE = BLOCKS.register("item_source", BlockdustryBlocks::itemSourceBlock);
    public static final DeferredItem<BlockdustryBuildingItem> ITEM_SOURCE_ITEM =
            ITEMS.register("item_source", () -> new BlockdustryBuildingItem(ITEM_SOURCE.get(), new Item.Properties(), 1));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemSourceBlockEntity>> ITEM_SOURCE_ENTITY =
            BLOCK_ENTITY_TYPES.register("item_source",
                    () -> BlockEntityType.Builder.of((pos, state) -> new ItemSourceBlockEntity(pos, state), ITEM_SOURCE.get()).build(null));

    // ===== 创造栏 tab：按 Mindustry Category 分类（production+crafting 合一为「锻造」）喵 =====

    // 锻造/生产（Mindustry crafting + production）：采集机 + 石墨压缩机喵
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CRAFTING_TAB =
            CREATIVE_TABS.register("crafting",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.blockdustry.crafting"))
                            .icon(() -> new ItemStack((ItemLike) DRILL_ITEM.get()))
                            .displayItems((params, output) -> {
                                output.accept(DRILL_ITEM);
                                output.accept(GRAPHITE_PRESS_ITEM);
                            })
                            .build());

    // 物流/运输（Mindustry distribution）：传送带 + 路由器 + 垂直提升机喵
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DISTRIBUTION_TAB =
            CREATIVE_TABS.register("distribution",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.blockdustry.distribution"))
                            .icon(() -> new ItemStack((ItemLike) CONVEYOR_ITEM.get()))
                            .displayItems((params, output) -> {
                                output.accept(CONVEYOR_ITEM);
                                output.accept(ROUTER_ITEM);
                                output.accept(ElevatorBlocks.ELEVATOR_ITEM);
                            })
                            .build());

    // 电力（Mindustry power）：电力节点 + 燃烧发电机 + 电池 + 电力源（沙盒）喵
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> POWER_TAB =
            CREATIVE_TABS.register("power",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.blockdustry.power"))
                            .icon(() -> new ItemStack((ItemLike) POWER_NODE_ITEM.get()))
                            .displayItems((params, output) -> {
                                output.accept(POWER_NODE_ITEM);
                                output.accept(COMBUSTION_GENERATOR_ITEM);
                                output.accept(BATTERY_ITEM);
                                output.accept(POWER_SOURCE_ITEM);
                            })
                            .build());

    // 存储（Mindustry effect 中的 core）：核心喵
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> STORAGE_TAB =
            CREATIVE_TABS.register("storage",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.blockdustry.storage"))
                            .icon(() -> new ItemStack((ItemLike) CORE_ITEM.get()))
                            .displayItems((params, output) -> {
                                output.accept(CORE_ITEM);
                            })
                            .build());

    // 防御/炮塔（Mindustry turret + defense）：双管 + 分裂 + 熔毁 + 电弧喵
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DEFENSE_TAB =
            CREATIVE_TABS.register("defense",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.blockdustry.defense"))
                            .icon(() -> new ItemStack((ItemLike) TURRET_ITEM.get()))
                            .displayItems((params, output) -> {
                                output.accept(TURRET_ITEM);
                                output.accept(SCATTER_ITEM);
                                output.accept(FuseArcRegistrar.FUSE_ITEM);
                                output.accept(FuseArcRegistrar.ARC_ITEM);
                            })
                            .build());

    // 单位（Mindustry units）：地面单位工厂喵
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> UNITS_TAB =
            CREATIVE_TABS.register("units",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.blockdustry.units"))
                            .icon(() -> new ItemStack((ItemLike) UNIT_FACTORY_ITEM.get()))
                            .displayItems((params, output) -> {
                                output.accept(UNIT_FACTORY_ITEM);
                            })
                            .build());

    // 物品/材料（MC 煤已有故不重复放）：石墨 + 硅 + 铅 + 全部迁移的 Mindustry 新材料喵
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEMS_TAB =
            CREATIVE_TABS.register("items",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.blockdustry.items"))
                            .icon(() -> new ItemStack((ItemLike) GRAPHITE.get()))
                            .displayItems((params, output) -> {
                                output.accept(GRAPHITE);
                                output.accept(SILICON);
                                output.accept(LEAD);
                                output.accept(BlockdustryItems.COPPER);
                                output.accept(BlockdustryItems.METAGLASS);
                                output.accept(BlockdustryItems.TITANIUM);
                                output.accept(BlockdustryItems.THORIUM);
                                output.accept(BlockdustryItems.PLASTANIUM);
                                output.accept(BlockdustryItems.PHASE_FABRIC);
                                output.accept(BlockdustryItems.SURGE_ALLOY);
                                output.accept(BlockdustryItems.SPORE_POD);
                                output.accept(BlockdustryItems.BLAST_COMPOUND);
                                output.accept(BlockdustryItems.PYRATITE);
                                output.accept(BlockdustryItems.SCRAP);
                            })
                            .build());

    // 调试/沙盒（Mindustry sandboxOnly）：物品源 + 队伍调试棒喵
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DEBUG_TAB =
            CREATIVE_TABS.register("debug",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.blockdustry.debug"))
                            .icon(() -> new ItemStack((ItemLike) ITEM_SOURCE_ITEM.get()))
                            .displayItems((params, output) -> {
                                output.accept(ITEM_SOURCE_ITEM);
                                output.accept(DEBUG_STICK);
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

    private static BlockdustryBuildingBlock scatterBlock() {
        // 2×2：Mindustry scatter 占地 2×2（与 duo 1×1 不同）喵
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(4f),
                () -> SCATTER_ENTITY.get(),
                2);
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
        // 核心：3×3 占地、视觉/碰撞 3 格高（BER 画 3×3×3 立方体，模型置空），第 4 参 height=3 给碰撞箱用喵
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(12f),
                () -> CORE_ENTITY.get(),
                3,
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

    // 电力源方块工厂：1×1 调试方块，无限产电喵
    private static BlockdustryBuildingBlock powerSourceBlock() {
        return new PowerSourceBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2f),
                () -> POWER_SOURCE_ENTITY.get(),
                1);
    }

    // 注册到 mod 事件总线喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        CREATIVE_TABS.register(bus);
    }

    // 多格建筑整组共享血量（T10 Level 3）：注册「整组总血」（格数 × 单格血量公式），
    // 保持与原「每格独立血」的总血一致，commonSetup 时由主类调用喵
    public static void registerBlockHealthDefaults() {
        registerGroupMaxHp(DRILL.get(), 2);
        registerGroupMaxHp(GRAPHITE_PRESS.get(), 2);
        registerGroupMaxHp(SCATTER.get(), 2);
        registerGroupMaxHp(CORE.get(), 3);
        registerGroupMaxHp(UNIT_FACTORY.get(), 3);
    }

    // 按「单格血量公式 × 格数」注册组总血喵
    private static void registerGroupMaxHp(Block block, int size) {
        float perCell = BlockHealthApi.getMaxHpForState(block.defaultBlockState(), null, null);
        if (perCell > 0f) {
            BlockHealthApi.setDefaultMaxHp(block, perCell * size * size);
        }
    }

    private BlockdustryBlocks() {}
}
