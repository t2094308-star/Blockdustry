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

// 粉碎机（Mindustry pulverizer，GenericCrafter，size 1）独立注册类（模板 FuseArcRegistrar/KilnRegistrar）：
// 方块 + 方块物品 + 方块实体喵。
// 数据忠于原版 Blocks.java L1293-1309：pulverizer = size 1、craftTime 40、吃 1 废料产 1 沙、
// 耗电 0.5/s、cost 铜×30 铅×25、health = size²×40 = 40（requirements 各物品 healthScaling=0）喵。
// 不并入共享 BlockdustryBlocks，主会话只需在 Blockdustry 构造器加一行 PulverizerRegistrar.register(modEventBus) 即挂载喵
public final class PulverizerRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);

    // —— 方块 ——
    public static final DeferredBlock<Block> PULVERIZER = BLOCKS.register("pulverizer", PulverizerRegistrar::pulverizerBlock);

    // —— 方块物品（Mindustry size 1 → 1×1 占地）——
    public static final DeferredItem<BlockdustryBuildingItem> PULVERIZER_ITEM =
            ITEMS.register("pulverizer", () -> new BlockdustryBuildingItem(PULVERIZER.get(), new Item.Properties(), 1));

    // —— 方块实体类型 ——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PulverizerBlockEntity>> PULVERIZER_ENTITY =
            BLOCK_ENTITY_TYPES.register("pulverizer",
                    () -> BlockEntityType.Builder.of(PulverizerBlockEntity::new, PULVERIZER.get()).build(null));

    // 工厂方法：size 1；strength 3 → 单格血量 10+10×3=40（Mindustry health=size²×40=40）喵
    private static BlockdustryBuildingBlock pulverizerBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3f),
                () -> PULVERIZER_ENTITY.get(),
                1);
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private PulverizerRegistrar() {}
}
