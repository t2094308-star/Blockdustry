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

// laser-drill 激光钻头独立注册类（模板 FuseArcRegistrar/SorterRegistrar）：方块 + 方块物品 + 方块实体喵。
// 不并入共享 BlockdustryBlocks，主会话只需在 Blockdustry 构造器加一行 LaserDrillRegistrar.register(modEventBus) 即挂载喵
public final class LaserDrillRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);

    // —— 方块 ——
    public static final DeferredBlock<Block> LASER_DRILL = BLOCKS.register("laser_drill", LaserDrillRegistrar::laserDrillBlock);

    // —— 方块物品（Mindustry laser-drill size 3）——
    public static final DeferredItem<BlockdustryBuildingItem> LASER_DRILL_ITEM =
            ITEMS.register("laser_drill", () -> new LaserDrillBuildingItem(LASER_DRILL.get(), new Item.Properties(), 3));

    // —— 方块实体类型 ——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LaserDrillBlockEntity>> LASER_DRILL_ENTITY =
            BLOCK_ENTITY_TYPES.register("laser_drill",
                    () -> BlockEntityType.Builder.of(LaserDrillBlockEntity::new, LASER_DRILL.get()).build(null));

    // 工厂方法：延迟取实体类型，避免注册期 unbound 喵
    private static BlockdustryBuildingBlock laserDrillBlock() {
        return new LaserDrillBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3f),
                () -> LASER_DRILL_ENTITY.get(),
                3);
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private LaserDrillRegistrar() {}
}
