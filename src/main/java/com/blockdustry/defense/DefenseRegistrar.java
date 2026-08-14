package com.blockdustry.defense;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.BlockdustryBuildingBlock;
import com.blockdustry.building.BlockdustryBuildingItem;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
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

// 批1F 墙体I：钛墙/大型钛墙（Wall）+ 门/大门（Door）自包含注册类（模板 FuseArcRegistrar/ContainerRegistrar）喵。
// 数据忠于原版 Blocks.java：
//   titanium-wall       L1718-1721: Wall, size1, 钛×6,       health = 110×4 = 440
//   titanium-wall-large L1723-1727: Wall, size2, 钛×24,      health = 110×4×4 = 1760
//   door                L1785-1788: Door, size1, 钛×6+硅×4,  health = 100×4 = 400, openfx=dooropen / closefx=doorclose
//   door-large          L1790-1796: Door, size2, 钛×24+硅×16, health = 100×4×4 = 1600, openfx=dooropenlarge / closefx=doorcloselarge
// strength = (单格血-10)/10（BlockHealth 公式 10+10×硬度）：wall 440→43、door 400→39 喵。
// 门音效：Mindustry Sounds.door（assets/sounds/block/door.ogg），注册 SoundEvent + sounds.json 喵。
// 不并入共享注册文件，主会话只需在 Blockdustry 构造器加一行 DefenseRegistrar.register(modEventBus) 即挂载喵
public final class DefenseRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, Blockdustry.MODID);

    // —— 方块 ——
    public static final DeferredBlock<Block> TITANIUM_WALL = BLOCKS.register("titanium_wall", DefenseRegistrar::titaniumWallBlock);
    public static final DeferredBlock<Block> TITANIUM_WALL_LARGE = BLOCKS.register("titanium_wall_large", DefenseRegistrar::titaniumWallLargeBlock);
    public static final DeferredBlock<Block> DOOR = BLOCKS.register("door", DefenseRegistrar::doorBlock);
    public static final DeferredBlock<Block> DOOR_LARGE = BLOCKS.register("door_large", DefenseRegistrar::doorLargeBlock);
    // —— 批1F 铜墙/废墙（T43 并入；strength 31→单格血320、23→240）——
    public static final DeferredBlock<Block> COPPER_WALL = BLOCKS.register("copper_wall", () -> wall(31f, 1));
    public static final DeferredBlock<Block> COPPER_WALL_LARGE = BLOCKS.register("copper_wall_large", () -> wall(31f, 2));
    public static final DeferredBlock<Block> SCRAP_WALL = BLOCKS.register("scrap_wall", () -> wall(23f, 1));
    public static final DeferredBlock<Block> SCRAP_WALL_LARGE = BLOCKS.register("scrap_wall_large", () -> wall(23f, 2));

    // —— 方块物品（Mindustry size 1/2 → 占地 1×1/2×2）——
    public static final DeferredItem<BlockdustryBuildingItem> TITANIUM_WALL_ITEM =
            ITEMS.register("titanium_wall", () -> new BlockdustryBuildingItem(TITANIUM_WALL.get(), new Item.Properties(), 1));
    public static final DeferredItem<BlockdustryBuildingItem> TITANIUM_WALL_LARGE_ITEM =
            ITEMS.register("titanium_wall_large", () -> new BlockdustryBuildingItem(TITANIUM_WALL_LARGE.get(), new Item.Properties(), 2));
    public static final DeferredItem<BlockdustryBuildingItem> DOOR_ITEM =
            ITEMS.register("door", () -> new BlockdustryBuildingItem(DOOR.get(), new Item.Properties(), 1));
    public static final DeferredItem<BlockdustryBuildingItem> DOOR_LARGE_ITEM =
            ITEMS.register("door_large", () -> new BlockdustryBuildingItem(DOOR_LARGE.get(), new Item.Properties(), 2));
    // —— 铜墙/废墙物品（T43 并入）——
    public static final DeferredItem<BlockdustryBuildingItem> COPPER_WALL_ITEM =
            ITEMS.register("copper_wall", () -> new BlockdustryBuildingItem(COPPER_WALL.get(), new Item.Properties(), 1));
    public static final DeferredItem<BlockdustryBuildingItem> COPPER_WALL_LARGE_ITEM =
            ITEMS.register("copper_wall_large", () -> new BlockdustryBuildingItem(COPPER_WALL_LARGE.get(), new Item.Properties(), 2));
    public static final DeferredItem<BlockdustryBuildingItem> SCRAP_WALL_ITEM =
            ITEMS.register("scrap_wall", () -> new BlockdustryBuildingItem(SCRAP_WALL.get(), new Item.Properties(), 1));
    public static final DeferredItem<BlockdustryBuildingItem> SCRAP_WALL_LARGE_ITEM =
            ITEMS.register("scrap_wall_large", () -> new BlockdustryBuildingItem(SCRAP_WALL_LARGE.get(), new Item.Properties(), 2));

    // —— 方块实体类型（墙共用 WallBlockEntity，门共用 DoorBlockEntity）——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WallBlockEntity>> WALL_ENTITY =
            BLOCK_ENTITY_TYPES.register("wall",
                    () -> BlockEntityType.Builder.of(WallBlockEntity::new,
                            TITANIUM_WALL.get(), TITANIUM_WALL_LARGE.get(),
                            COPPER_WALL.get(), COPPER_WALL_LARGE.get(),
                            SCRAP_WALL.get(), SCRAP_WALL_LARGE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DoorBlockEntity>> DOOR_ENTITY =
            BLOCK_ENTITY_TYPES.register("door",
                    () -> BlockEntityType.Builder.of(DoorBlockEntity::new, DOOR.get(), DOOR_LARGE.get()).build(null));

    // —— 门音效（Mindustry Sounds.door）——
    public static final DeferredHolder<SoundEvent, SoundEvent> DOOR_SOUND =
            SOUND_EVENTS.register("door",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "door")));

    // 工厂方法（strength 43 → 单格血 440；strength 39 → 单格血 400，组血 = 单格 × size²）喵
    private static BlockdustryBuildingBlock titaniumWallBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(43f),
                () -> WALL_ENTITY.get(),
                1);
    }

    private static BlockdustryBuildingBlock titaniumWallLargeBlock() {
        return new BlockdustryBuildingBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(43f),
                () -> WALL_ENTITY.get(),
                2);
    }

    private static DoorBlock doorBlock() {
        return new DoorBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(39f),
                () -> DOOR_ENTITY.get(),
                1);
    }

    private static DoorBlock doorLargeBlock() {
        return new DoorBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(39f),
                () -> DOOR_ENTITY.get(),
                2);
    }

    // 铜墙/废墙工厂方法（T43：strength 31→单格 320、23→240；组血 = 单格 × size²）喵
    private static BlockdustryBuildingBlock wall(float strength, int size) {
        return new WallBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(strength),
                () -> WALL_ENTITY.get(), size);
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        SOUND_EVENTS.register(bus);
    }

    private DefenseRegistrar() {}
}
