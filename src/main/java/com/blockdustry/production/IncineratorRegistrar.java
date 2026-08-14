package com.blockdustry.production;

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

// 焚化炉（Mindustry incinerator，Incinerator 类，size 1）独立注册类（模板 FuseArcRegistrar/KilnRegistrar）：
// 方块 + 方块物品 + 方块实体喵。
// 数据忠于原版 Blocks.java L1324-1329：incinerator = size 1、health=90（显式设置）、耗电 0.5/s、
// cost 石墨×5 铅×15、吞噬物品销毁（Incinerator 类，非 GenericCrafter！不产任何物品）喵。
// 不并入共享 BlockdustryBlocks，主会话只需在 Blockdustry 构造器加一行 IncineratorRegistrar.register(modEventBus) 即挂载喵
public final class IncineratorRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);

    // —— 方块 ——
    public static final DeferredBlock<Block> INCINERATOR = BLOCKS.register("incinerator", IncineratorRegistrar::incineratorBlock);

    // —— 方块物品（Mindustry size 1 → 1×1 占地）——
    public static final DeferredItem<BlockdustryBuildingItem> INCINERATOR_ITEM =
            ITEMS.register("incinerator", () -> new BlockdustryBuildingItem(INCINERATOR.get(), new Item.Properties(), 1));

    // —— 方块实体类型 ——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IncineratorBlockEntity>> INCINERATOR_ENTITY =
            BLOCK_ENTITY_TYPES.register("incinerator",
                    () -> BlockEntityType.Builder.of(IncineratorBlockEntity::new, INCINERATOR.get()).build(null));

    // 工厂方法：size 1；strength 8 → 单格血量 10+10×8=90（Mindustry health=90 显式设置）喵
    private static BlockdustryBuildingBlock incineratorBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(8f),
                () -> INCINERATOR_ENTITY.get(),
                1);
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private IncineratorRegistrar() {}
}
