package com.blockdustry.distribution;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.BlockdustryBuildingBlock;
import com.blockdustry.building.BlockdustryBuildingItem;
import com.blockdustry.building.SorterBlock;
import com.blockdustry.building.SorterBlockEntity;

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

// sorter + invertedSorter 独立注册类（模板 FuseArcRegistrar）：方块 + 方块物品 + 方块实体喵。
// 两个方块共用同一 BE 类型（SorterBlockEntity 按方块状态区分 invert，Mindustry 也是同一 SorterBuild 类）喵。
// 不并入共享 BlockdustryBlocks，主会话只需在 Blockdustry 构造器加一行 SorterRegistrar.register(modEventBus) 即挂载喵
public final class SorterRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);

    // —— 方块 ——
    public static final DeferredBlock<Block> SORTER = BLOCKS.register("sorter", () -> sorterBlock(false));
    public static final DeferredBlock<Block> INVERTED_SORTER = BLOCKS.register("inverted_sorter", () -> sorterBlock(true));

    // —— 方块物品（两者皆 1×1，Mindustry size 1）——
    public static final DeferredItem<BlockdustryBuildingItem> SORTER_ITEM =
            ITEMS.register("sorter", () -> new BlockdustryBuildingItem(SORTER.get(), new Item.Properties(), 1));
    public static final DeferredItem<BlockdustryBuildingItem> INVERTED_SORTER_ITEM =
            ITEMS.register("inverted_sorter", () -> new BlockdustryBuildingItem(INVERTED_SORTER.get(), new Item.Properties(), 1));

    // —— 方块实体类型（sorter/inverted-sorter 共用）——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SorterBlockEntity>> SORTER_ENTITY =
            BLOCK_ENTITY_TYPES.register("sorter",
                    () -> BlockEntityType.Builder.of(SorterBlockEntity::new, SORTER.get(), INVERTED_SORTER.get()).build(null));

    // 工厂方法：延迟取实体类型，避免注册期 unbound 喵
    private static BlockdustryBuildingBlock sorterBlock(boolean invert) {
        return new SorterBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2f),
                () -> SORTER_ENTITY.get(),
                1,
                invert);
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private SorterRegistrar() {}
}
