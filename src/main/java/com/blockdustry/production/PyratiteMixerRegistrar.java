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

// 硫化物混合器（Mindustry pyratite-mixer，GenericCrafter，size 2）独立注册类（模板 FuseArcRegistrar/KilnRegistrar）：
// 方块 + 方块物品 + 方块实体喵。
// 数据忠于原版 Blocks.java L1189-1202：pyratite-mixer = size 2、craftTime 80（默认）、吃 1 煤 + 2 铅 + 2 沙产 1 硫化物、
// 耗电 0.20/s、cost 铜×50 铅×25、health=size²×40=160（copper/lead 的 healthScaling=0）、envEnabled|=space、ambientSound=loopMachineSpin（本 mod 无环境音系统）喵。
// 动画核查：drawer=DrawDefault（静态贴图）、craftEffect/updateEffect=Fx.none、无 emitLight —— 原版无任何动画/粒子/光效，故不新建渲染器（忠实原版，不自编特效）喵。
// 不并入共享 BlockdustryBlocks，主会话只需在 Blockdustry 构造器加一行 PyratiteMixerRegistrar.register(modEventBus) 即挂载喵
public final class PyratiteMixerRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);

    // —— 方块 ——
    public static final DeferredBlock<Block> PYRATITE_MIXER = BLOCKS.register("pyratite_mixer", PyratiteMixerRegistrar::pyratiteMixerBlock);

    // —— 方块物品（Mindustry size 2 → 2×2 占地）——
    public static final DeferredItem<BlockdustryBuildingItem> PYRATITE_MIXER_ITEM =
            ITEMS.register("pyratite_mixer", () -> new BlockdustryBuildingItem(PYRATITE_MIXER.get(), new Item.Properties(), 2));

    // —— 方块实体类型 ——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PyratiteMixerBlockEntity>> PYRATITE_MIXER_ENTITY =
            BLOCK_ENTITY_TYPES.register("pyratite_mixer",
                    () -> BlockEntityType.Builder.of((pos, state) -> new PyratiteMixerBlockEntity(pos, state), PYRATITE_MIXER.get()).build(null));

    // 工厂方法：size 2；strength 3 → 单格血量 10+10×3=40，组血 40×4=160（Mindustry health=size²×40=160）喵
    private static BlockdustryBuildingBlock pyratiteMixerBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3f),
                () -> PYRATITE_MIXER_ENTITY.get(),
                2);
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private PyratiteMixerRegistrar() {}
}
