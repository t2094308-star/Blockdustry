package com.blockdustry.distribution;

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

// 传送带桥独立注册类（模板 SorterRegistrar/FuseArcRegistrar）：方块 + 方块物品 + 方块实体喵。
// Mindustry itemBridge（BufferedItemBridge，注册名 bridge-conveyor，size 1）喵。
// 不并入共享 BlockdustryBlocks，主会话只需在 Blockdustry 构造器加一行 BridgeRegistrar.register(modEventBus) 即挂载喵
public final class BridgeRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);

    // —— 方块（Mindustry size 1）喵 ——
    public static final DeferredBlock<Block> BRIDGE = BLOCKS.register("bridge_conveyor", BridgeRegistrar::bridgeBlock);

    // —— 方块物品（1×1）喵 ——
    public static final DeferredItem<BlockdustryBuildingItem> BRIDGE_ITEM =
            ITEMS.register("bridge_conveyor", () -> new BlockdustryBuildingItem(BRIDGE.get(), new Item.Properties(), 1));

    // —— 方块实体类型喵 ——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemBridgeBlockEntity>> BRIDGE_ENTITY =
            BLOCK_ENTITY_TYPES.register("bridge_conveyor",
                    () -> BlockEntityType.Builder.of((pos, state) -> new ItemBridgeBlockEntity(pos, state), BRIDGE.get()).build(null));

    // 工厂方法：延迟取实体类型，避免注册期 unbound 喵
    private static BlockdustryBuildingBlock bridgeBlock() {
        return new ItemBridgeBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2f),
                () -> BRIDGE_ENTITY.get());
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private BridgeRegistrar() {}
}
