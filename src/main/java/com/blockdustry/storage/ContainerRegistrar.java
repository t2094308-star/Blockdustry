package com.blockdustry.storage;

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

// container（Mindustry StorageBlock，size 2）独立注册类（模板 FuseArcRegistrar）：方块 + 方块物品 + 方块实体喵。
// 数据忠于原版 Blocks.java L3225-3230：container = size 2、itemCapacity 300、scaledHealth 55（→总血 220）、
// 配方 Category.effect 钛×100、无消费（纯存储、无电力/耗料）喵。
// 不并入共享 BlockdustryBlocks，主会话只需在 Blockdustry 构造器加一行 ContainerRegistrar.register(modEventBus) 即挂载喵
public final class ContainerRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);

    // —— 方块 ——
    public static final DeferredBlock<Block> CONTAINER = BLOCKS.register("container", ContainerRegistrar::containerBlock);

    // —— 方块物品（Mindustry size 2 → 2×2 占地）——
    public static final DeferredItem<BlockdustryBuildingItem> CONTAINER_ITEM =
            ITEMS.register("container", () -> new BlockdustryBuildingItem(CONTAINER.get(), new Item.Properties(), 2));

    // —— 方块实体类型 ——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ContainerBlockEntity>> CONTAINER_ENTITY =
            BLOCK_ENTITY_TYPES.register("container",
                    () -> BlockEntityType.Builder.of(ContainerBlockEntity::new, CONTAINER.get()).build(null));

    // 工厂方法：size 2；strength 4.5 → 单格血量 10+10×4.5=55，组血 55×4=220（Mindustry size²×scaledHealth=4×55）喵
    private static BlockdustryBuildingBlock containerBlock() {
        return new ContainerBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(4.5f),
                () -> CONTAINER_ENTITY.get(),
                2);
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private ContainerRegistrar() {}
}
