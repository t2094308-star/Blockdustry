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

// 气动钻头（Mindustry pneumatic-drill）独立注册类：方块 + 方块物品 + 方块实体喵。
// 模板 FuseArcRegistrar：刻意不并入共享 BlockdustryBlocks，主会话只需在 Blockdustry 构造器
// 加一行 PneumaticDrillRegistrar.register(modEventBus) 即挂载喵
public final class PneumaticDrillRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);

    // —— 方块（size 2，忠实 Mindustry pneumatic-drill 占地）——
    public static final DeferredBlock<Block> PNEUMATIC_DRILL = BLOCKS.register("pneumatic_drill", PneumaticDrillRegistrar::drillBlock);

    // —— 方块物品（2×2 多格放置）——
    public static final DeferredItem<BlockdustryBuildingItem> PNEUMATIC_DRILL_ITEM =
            ITEMS.register("pneumatic_drill", () -> new BlockdustryBuildingItem(PNEUMATIC_DRILL.get(), new Item.Properties(), 2));

    // —— 方块实体类型 ——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PneumaticDrillBlockEntity>> PNEUMATIC_DRILL_ENTITY =
            BLOCK_ENTITY_TYPES.register("pneumatic_drill",
                    () -> BlockEntityType.Builder.of(PneumaticDrillBlockEntity::new, PNEUMATIC_DRILL.get()).build(null));

    // 工厂方法：延迟取实体类型，避免注册期 unbound 喵
    private static BlockdustryBuildingBlock drillBlock() {
        // 原版 pneumatic-drill 无血量覆盖（与 mechanical 同级），沿用 mechanical 强度 3f 喵
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3f),
                () -> PNEUMATIC_DRILL_ENTITY.get(),
                2);
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private PneumaticDrillRegistrar() {}
}
