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

// 垂直提升机（elevator）独立注册类：不并入共享 BlockdustryBlocks，主会话按
// docs/子agent/T14_立体物流初步.md 注册清单激活（合并进 BlockdustryBlocks 或直接 register 本类）喵
public final class ElevatorBlocks {
    // 方块注册表喵
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    // 物品注册表（方块物品）喵
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    // 方块实体类型注册表喵
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);

    public static final DeferredBlock<Block> ELEVATOR =
            BLOCKS.register("elevator", ElevatorBlocks::elevatorBlock);
    public static final DeferredItem<BlockdustryBuildingItem> ELEVATOR_ITEM =
            ITEMS.register("elevator", () -> new BlockdustryBuildingItem(ELEVATOR.get(), new Item.Properties(), 1));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElevatorBlockEntity>> ELEVATOR_ENTITY =
            BLOCK_ENTITY_TYPES.register("elevator",
                    () -> BlockEntityType.Builder.of(ElevatorBlockEntity::new, ELEVATOR.get()).build(null));

    // 工厂方法：延迟取实体类型，避免注册期 unbound 喵
    private static ElevatorBlock elevatorBlock() {
        return new ElevatorBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2f),
                () -> ELEVATOR_ENTITY.get());
    }

    // 注册到 mod 事件总线喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private ElevatorBlocks() {}
}
