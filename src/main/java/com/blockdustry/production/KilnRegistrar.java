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

// 窖炉（Mindustry kiln，GenericCrafter，size 2）独立注册类（模板 FuseArcRegistrar/ContainerRegistrar）：
// 方块 + 方块物品 + 方块实体喵。
// 数据忠于原版 Blocks.java L1103-1116：kiln = size 2、craftTime 30、吃 1 铅+1 沙产 1 钢化玻璃、
// 耗电 0.6/s、cost 铜×60 石墨×30 铅×30、health = size²×40 = 160（requirements 各物品 healthScaling=0）喵。
// 不并入共享 BlockdustryBlocks，主会话只需在 Blockdustry 构造器加一行 KilnRegistrar.register(modEventBus) 即挂载喵
public final class KilnRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);

    // —— 方块 ——
    public static final DeferredBlock<Block> KILN = BLOCKS.register("kiln", KilnRegistrar::kilnBlock);

    // —— 方块物品（Mindustry size 2 → 2×2 占地）——
    public static final DeferredItem<BlockdustryBuildingItem> KILN_ITEM =
            ITEMS.register("kiln", () -> new BlockdustryBuildingItem(KILN.get(), new Item.Properties(), 2));

    // —— 方块实体类型 ——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KilnBlockEntity>> KILN_ENTITY =
            BLOCK_ENTITY_TYPES.register("kiln",
                    () -> BlockEntityType.Builder.of(KilnBlockEntity::new, KILN.get()).build(null));

    // 工厂方法：size 2；strength 3 → 单格血量 10+10×3=40，组血 40×4=160（Mindustry health=size²×40=160）喵
    private static BlockdustryBuildingBlock kilnBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3f),
                () -> KILN_ENTITY.get(),
                2);
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private KilnRegistrar() {}
}
