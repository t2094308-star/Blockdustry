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

// 爆破钻头独立注册类（模板 FuseArcRegistrar/SorterRegistrar）：方块 + 方块物品 + 方块实体喵。
// 不并入共享 BlockdustryBlocks，主会话只需在 Blockdustry 构造器加一行 BlastDrillRegistrar.register(modEventBus) 即挂载喵
public final class BlastDrillRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);

    // —— 方块（Mindustry blast-drill：原版 size=4，4×4 占地）——
    public static final DeferredBlock<Block> BLAST_DRILL = BLOCKS.register("blast_drill", BlastDrillRegistrar::blastDrillBlock);

    // —— 方块物品（4×4 多格放置 + 矿下放置检查，见 BlastDrillBuildingItem）——
    public static final DeferredItem<BlastDrillBuildingItem> BLAST_DRILL_ITEM =
            ITEMS.register("blast_drill", () -> new BlastDrillBuildingItem(BLAST_DRILL.get(), new Item.Properties()));

    // —— 方块实体类型 ——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlastDrillBlockEntity>> BLAST_DRILL_ENTITY =
            BLOCK_ENTITY_TYPES.register("blast_drill",
                    () -> BlockEntityType.Builder.of(BlastDrillBlockEntity::new, BLAST_DRILL.get()).build(null));

    // 工厂方法：延迟取实体类型，避免静态字段前向引用与注册期 unbound 喵
    private static BlastDrillBlock blastDrillBlock() {
        return new BlastDrillBlock(
                // strength 3.8 → 单格 10+10×3.8=48、组血 48×16=768 = 原版（钍需求 healthScaling 0.2 → 16×40×1.2=768）喵
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3.8f),
                () -> BLAST_DRILL_ENTITY.get());
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private BlastDrillRegistrar() {}
}
