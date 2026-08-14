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

// 相织布编织器（Mindustry phase-weaver，GenericCrafter，size 2）独立注册类（模板 FuseArcRegistrar/KilnRegistrar）喵。
// 数据忠于原版 Blocks.java L1136-1152：size 2、craftTime 120、吃钍×4+沙×10 产相织布×1、耗电 5/s、
// cost 硅×130 铅×120 钍×75、health = size²×40 = 160、drawer = DrawMulti(DrawRegion(-bottom), DrawWeave, DrawDefault)喵。
// 不并入共享 BlockdustryBlocks，主会话只需在 Blockdustry 构造器加一行 PhaseWeaverRegistrar.register(modEventBus) 即挂载喵
public final class PhaseWeaverRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);

    // —— 方块 ——
    public static final DeferredBlock<Block> PHASE_WEAVER =
            BLOCKS.register("phase_weaver", PhaseWeaverRegistrar::phaseWeaverBlock);

    // —— 方块物品（Mindustry size 2 → 2×2 占地）——
    public static final DeferredItem<BlockdustryBuildingItem> PHASE_WEAVER_ITEM =
            ITEMS.register("phase_weaver", () -> new BlockdustryBuildingItem(PHASE_WEAVER.get(), new Item.Properties(), 2));

    // —— 方块实体类型 ——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PhaseWeaverBlockEntity>> PHASE_WEAVER_ENTITY =
            BLOCK_ENTITY_TYPES.register("phase_weaver",
                    () -> BlockEntityType.Builder.of(PhaseWeaverBlockEntity::new, PHASE_WEAVER.get()).build(null));

    // 工厂方法：size 2；strength 3 → 单格血量 10+10×3=40，组血 40×4=160（Mindustry health=size²×40=160）喵
    private static BlockdustryBuildingBlock phaseWeaverBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3f),
                () -> PHASE_WEAVER_ENTITY.get(),
                2);
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private PhaseWeaverRegistrar() {}
}
