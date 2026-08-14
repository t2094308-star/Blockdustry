package com.blockdustry.power;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.BlockdustryBuildingBlock;
import com.blockdustry.building.BlockdustryBuildingItem;

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

// 太阳能电池板自包含注册类（模板 LaserDrillRegistrar）：solar-panel（1×1）+ solar-panel-large（3×3），
// 均类 SolarGenerator。不并入共享 BlockdustryBlocks，主会话在 Blockdustry 构造器加一行
// SolarPanelRegistrar.register(modEventBus) 即挂载喵
public final class SolarPanelRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);

    // —— 1×1 太阳能板（Mindustry solar-panel，powerProduction=0.12/tick，铅×10 硅×8）——
    public static final DeferredBlock<Block> SOLAR_PANEL =
            BLOCKS.register("solar_panel", SolarPanelRegistrar::solarPanelBlock);
    public static final DeferredItem<BlockdustryBuildingItem> SOLAR_PANEL_ITEM =
            ITEMS.register("solar_panel", () -> new BlockdustryBuildingItem(SOLAR_PANEL.get(), new Item.Properties(), 1));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL_ENTITY =
            BLOCK_ENTITY_TYPES.register("solar_panel",
                    () -> BlockEntityType.Builder.of(SolarPanelBlockEntity::new, SOLAR_PANEL.get()).build(null));

    // —— 3×3 大型太阳能板（Mindustry solar-panel-large，size=3，powerProduction=1.6/tick，铅×60 硅×70 相织布×15）——
    public static final DeferredBlock<Block> SOLAR_PANEL_LARGE =
            BLOCKS.register("solar_panel_large", SolarPanelRegistrar::solarPanelLargeBlock);
    public static final DeferredItem<BlockdustryBuildingItem> SOLAR_PANEL_LARGE_ITEM =
            ITEMS.register("solar_panel_large", () -> new BlockdustryBuildingItem(SOLAR_PANEL_LARGE.get(), new Item.Properties(), 3));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarPanelLargeBlockEntity>> SOLAR_PANEL_LARGE_ENTITY =
            BLOCK_ENTITY_TYPES.register("solar_panel_large",
                    () -> BlockEntityType.Builder.of(SolarPanelLargeBlockEntity::new, SOLAR_PANEL_LARGE.get()).build(null));

    // 工厂方法：延迟取实体类型，避免注册期 unbound 喵
    private static BlockdustryBuildingBlock solarPanelBlock() {
        // 1×1：strength 3f（同火力发电机）；原版 solar-panel health 默认 40，未覆盖喵
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3f),
                () -> SOLAR_PANEL_ENTITY.get(),
                1);
    }

    private static BlockdustryBuildingBlock solarPanelLargeBlock() {
        // 3×3：strength 3f；原版 solar-panel-large health 默认 40，整组血 = 单格 × 9（主会话注册组血量）喵
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3f),
                () -> SOLAR_PANEL_LARGE_ENTITY.get(),
                3);
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private SolarPanelRegistrar() {}
}
