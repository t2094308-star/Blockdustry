package com.blockdustry.building;

import com.blockdustry.Blockdustry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// 二极管 + 涌电塔独立注册类（模板 FuseArcRegistrar/SorterRegistrar）喵。
// 不并入共享 BlockdustryBlocks，主会话只需在 Blockdustry 构造器加一行 DiodeSurgeTowerRegistrar.register(modEventBus) 即挂载喵。
// 数据（原版 Blocks.java）：
//   diode = PowerDiode("diode")：1×1 单向二极管，需求 硅10+塑钢5+钢化玻璃10 喵
//   surgeTower = PowerNode("surge-tower")：size=2、maxNodes=2、laserRange=40f，需求 钛7+铅10+硅15+涌电合金15 喵
public final class DiodeSurgeTowerRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);

    // —— 方块 ——
    public static final DeferredBlock<Block> DIODE = BLOCKS.register("diode", DiodeSurgeTowerRegistrar::diodeBlock);
    public static final DeferredBlock<Block> SURGE_TOWER = BLOCKS.register("surge_tower", DiodeSurgeTowerRegistrar::surgeTowerBlock);

    // —— 方块物品（diode 1×1，surge-tower 2×2）——
    public static final DeferredItem<BlockdustryBuildingItem> DIODE_ITEM =
            ITEMS.register("diode", () -> new BlockdustryBuildingItem(DIODE.get(), new Item.Properties(), 1));
    public static final DeferredItem<BlockdustryBuildingItem> SURGE_TOWER_ITEM =
            ITEMS.register("surge_tower", () -> new BlockdustryBuildingItem(SURGE_TOWER.get(), new Item.Properties(), 2));

    // —— 方块实体类型 ——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DiodeBlockEntity>> DIODE_ENTITY =
            BLOCK_ENTITY_TYPES.register("diode",
                    () -> BlockEntityType.Builder.of(DiodeBlockEntity::new, DIODE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SurgeTowerBlockEntity>> SURGE_TOWER_ENTITY =
            BLOCK_ENTITY_TYPES.register("surge_tower",
                    () -> BlockEntityType.Builder.of(SurgeTowerBlockEntity::new, SURGE_TOWER.get()).build(null));

    // 工厂方法：延迟取实体类型，避免静态字段前向引用与注册期 unbound 喵
    private static DiodeBlock diodeBlock() {
        return new DiodeBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2f),
                () -> DIODE_ENTITY.get());
    }

    private static SurgeTowerBlock surgeTowerBlock() {
        return new SurgeTowerBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2f),
                () -> SURGE_TOWER_ENTITY.get());
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private DiodeSurgeTowerRegistrar() {}
}
