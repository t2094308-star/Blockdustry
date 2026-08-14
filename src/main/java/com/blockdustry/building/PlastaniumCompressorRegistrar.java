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

// 塑钢压缩机（Mindustry plastanium-compressor）独立注册类（模板 FuseArcRegistrar/ContainerRegistrar）：
// 方块 + 方块物品 + 方块实体 + 石油占位物品喵。
// 数据忠于原版 Blocks.java L1118-1134：size 2、需求 silicon 80/lead 115/graphite 60/titanium 80、
// health 320、craftTime 60、产塑钢 1、耗钛 2、耗电 3、耗油 0.25/s、liquidCapacity 60喵。
// 不并入共享 BlockdustryBlocks，主会话只需在 Blockdustry 构造器加一行
// PlastaniumCompressorRegistrar.register(modEventBus) 即挂载喵
public final class PlastaniumCompressorRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);

    // —— 方块 ——
    public static final DeferredBlock<Block> PLASTANIUM_COMPRESSOR =
            BLOCKS.register("plastanium_compressor", PlastaniumCompressorRegistrar::plastaniumCompressorBlock);

    // —— 方块物品（Mindustry size 2 → 2×2 占地）——
    public static final DeferredItem<BlockdustryBuildingItem> PLASTANIUM_COMPRESSOR_ITEM =
            ITEMS.register("plastanium_compressor",
                    () -> new BlockdustryBuildingItem(PLASTANIUM_COMPRESSOR.get(), new Item.Properties(), 2));

    // —— 方块实体类型 ——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlastaniumCompressorBlockEntity>> PLASTANIUM_COMPRESSOR_ENTITY =
            BLOCK_ENTITY_TYPES.register("plastanium_compressor",
                    () -> BlockEntityType.Builder.of(PlastaniumCompressorBlockEntity::new, PLASTANIUM_COMPRESSOR.get()).build(null));

    // —— 石油占位物品 ——
    // Blockdustry 暂无液体系统：原版 consumeLiquid(Liquids.oil, 0.25f) 的油先以物品形式供料（1 物品 = 1 油单位，
    // 耗油速率 0.25/s 忠实原版），待液体系统接入后替换为真液体喵
    public static final DeferredItem<Item> OIL = ITEMS.register("oil", () -> new Item(new Item.Properties()));

    // 工厂方法：延迟取实体类型，避免注册期 unbound 喵。
    // strength 7.0 → 单格血 10+10×7=80，组血 80×4=320 = Mindustry health 320（BlockHealthApi 公式 10+10×hardness）喵
    private static BlockdustryBuildingBlock plastaniumCompressorBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(7f),
                () -> PLASTANIUM_COMPRESSOR_ENTITY.get(),
                2);
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private PlastaniumCompressorRegistrar() {}
}
