package com.blockdustry.item;

import java.util.ArrayList;
import java.util.List;

import com.blockdustry.Blockdustry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// Mindustry 材料物品独立注册类：为 MC 没有的材料注册独立 DeferredItem（铜/玻璃/钛/钍/塑料钢等）喵。
// 不并入共享 BlockdustryBlocks，独立 DeferredRegister 避免动共享注册表喵。
public final class BlockdustryItems {
    // 材料物品注册表喵
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);

    // —— 新注册的 Mindustry 赛普罗（Serpulo）材料（MC 没有的，全部为普通锭/材料物品）喵 ——
    // 埃里克尔（Erekir）材料（beryllium/tungsten/oxide/carbide/fissile_matter/dormant_cyst）暂不迁移喵
    public static final DeferredItem<Item> COPPER = ITEMS.register("copper", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> METAGLASS = ITEMS.register("metaglass", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TITANIUM = ITEMS.register("titanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> THORIUM = ITEMS.register("thorium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLASTANIUM = ITEMS.register("plastanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PHASE_FABRIC = ITEMS.register("phase_fabric", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SURGE_ALLOY = ITEMS.register("surge_alloy", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SPORE_POD = ITEMS.register("spore_pod", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BLAST_COMPOUND = ITEMS.register("blast_compound", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PYRATITE = ITEMS.register("pyratite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SCRAP = ITEMS.register("scrap", () -> new Item(new Item.Properties()));

    // 本类新注册材料（MC 无的赛普罗材料），运行时惰性组装避免注册期 get() 未绑定喵
    public static List<Item> newMaterials() {
        return List.of(
                COPPER.get(), METAGLASS.get(), TITANIUM.get(), THORIUM.get(),
                PLASTANIUM.get(), PHASE_FABRIC.get(), SURGE_ALLOY.get(), SPORE_POD.get(),
                BLAST_COMPOUND.get(), PYRATITE.get(), SCRAP.get());
    }

    // 全部迁移材料（含 MC 煤/沙 + 共享石墨/硅/铅 + 本类新材料），供物品源菜单/核心白名单/创造栏用喵
    public static List<Item> allMaterials() {
        List<Item> list = new ArrayList<>();
        list.add(Items.COAL);
        list.add(Items.SAND);
        list.add(com.blockdustry.building.BlockdustryBlocks.GRAPHITE.get());
        list.add(com.blockdustry.building.BlockdustryBlocks.SILICON.get());
        list.add(com.blockdustry.building.BlockdustryBlocks.LEAD.get());
        list.addAll(newMaterials());
        return list;
    }

    // 注册到 mod 事件总线喵
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    private BlockdustryItems() {}
}
