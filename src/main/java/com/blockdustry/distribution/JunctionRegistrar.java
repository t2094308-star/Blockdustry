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

// junction（1×1 双向直通并道）+ distributor（2×2 圆周均分）独立注册类。
// 模板 FuseArcRegistrar：独立 DeferredRegister + 工厂方法防前向引用 + register(IEventBus)。
// 不并入共享 BlockdustryBlocks，主会话只需在 Blockdustry 构造器加一行 JunctionRegistrar.register(modEventBus) 即挂载喵
public final class JunctionRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);

    // —— 方块 ——
    public static final DeferredBlock<Block> JUNCTION = BLOCKS.register("junction", JunctionRegistrar::junctionBlock);
    public static final DeferredBlock<Block> DISTRIBUTOR = BLOCKS.register("distributor", JunctionRegistrar::distributorBlock);

    // —— 方块物品（junction 1×1，distributor 2×2，均非旋转）——
    public static final DeferredItem<BlockdustryBuildingItem> JUNCTION_ITEM =
            ITEMS.register("junction", () -> new BlockdustryBuildingItem(JUNCTION.get(), new Item.Properties(), 1));
    public static final DeferredItem<BlockdustryBuildingItem> DISTRIBUTOR_ITEM =
            ITEMS.register("distributor", () -> new BlockdustryBuildingItem(DISTRIBUTOR.get(), new Item.Properties(), 2));

    // —— 方块实体类型 ——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<JunctionBlockEntity>> JUNCTION_ENTITY =
            BLOCK_ENTITY_TYPES.register("junction",
                    () -> BlockEntityType.Builder.of(JunctionBlockEntity::new, JUNCTION.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DistributorBlockEntity>> DISTRIBUTOR_ENTITY =
            BLOCK_ENTITY_TYPES.register("distributor",
                    () -> BlockEntityType.Builder.of(DistributorBlockEntity::new, DISTRIBUTOR.get()).build(null));

    // 工厂方法：延迟取实体类型，避免静态字段前向引用与注册期 unbound 喵
    private static BlockdustryBuildingBlock junctionBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2f),
                () -> JUNCTION_ENTITY.get(),
                1);
    }

    private static BlockdustryBuildingBlock distributorBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2f),
                () -> DISTRIBUTOR_ENTITY.get(),
                2);
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private JunctionRegistrar() {}
}
