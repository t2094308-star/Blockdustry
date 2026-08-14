package com.blockdustry.distribution;

import com.blockdustry.Blockdustry;
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

// overflowGate/underflowGate 独立注册类（size 1）：方块 + 方块物品 + 方块实体。
// 双块共用同一 GateBlockEntity 类型（Mindustry 只有 OverflowGate 一个类，underflow=OverflowGate{invert=true}）喵。
// 刻意不并入 BlockdustryBlocks（任务约束不碰共享注册文件），主会话只需在 Blockdustry 构造器加一行
// GateRegistrar.register(modEventBus) 即挂载，并把两个物品 accept 进物流 tab、ResearchNodes 加 2 节点喵。
public final class GateRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);

    // —— 方块（Mindustry overflow-gate / underflow-gate，size 1）——
    public static final DeferredBlock<Block> OVERFLOW_GATE = BLOCKS.register("overflow_gate", GateRegistrar::overflowGateBlock);
    public static final DeferredBlock<Block> UNDERFLOW_GATE = BLOCKS.register("underflow_gate", GateRegistrar::underflowGateBlock);

    // —— 方块物品（1×1）——
    public static final DeferredItem<BlockdustryBuildingItem> OVERFLOW_GATE_ITEM =
            ITEMS.register("overflow_gate", () -> new BlockdustryBuildingItem(OVERFLOW_GATE.get(), new Item.Properties(), 1));
    public static final DeferredItem<BlockdustryBuildingItem> UNDERFLOW_GATE_ITEM =
            ITEMS.register("underflow_gate", () -> new BlockdustryBuildingItem(UNDERFLOW_GATE.get(), new Item.Properties(), 1));

    // —— 方块实体：一个类型覆盖两个块（与 Mindustry 单类双实例一致）——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GateBlockEntity>> GATE_ENTITY =
            BLOCK_ENTITY_TYPES.register("gate",
                    () -> BlockEntityType.Builder.of(GateBlockEntity::new, OVERFLOW_GATE.get(), UNDERFLOW_GATE.get()).build(null));

    // 工厂方法：延迟取实体类型，避免注册期 unbound 喵
    private static GateBlock overflowGateBlock() {
        return new GateBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2f),
                () -> GATE_ENTITY.get(),
                false);
    }

    private static GateBlock underflowGateBlock() {
        return new GateBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2f),
                () -> GATE_ENTITY.get(),
                true);
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private GateRegistrar() {}
}
