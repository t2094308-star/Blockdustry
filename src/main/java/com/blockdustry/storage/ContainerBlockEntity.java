package com.blockdustry.storage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.blockdustry.building.BlockdustryBuildingEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 存储容器方块实体（Mindustry StorageBlock.StorageBuild 迁移）：多物品类型存储，每类型各占 itemCapacity=300，
// 忠于原版 separateItemCapacity=true 语义（容器可同时存多种物品、各类型独立上限）喵。
// 原版 StorageBlock 无 buildConfiguration/无点击 UI，点开只显示通用信息面板列出库存；本 mod 用
// Jade 通用 provider（读 getStoredItem/getStoredCount/getCapacity）+ 渲染器顶面主物品图标作「简单存储显示」喵。
// 原版 outputsItems=false（物品靠 unloader 取出），本 mod 尚无 unloader，故 tick 用 Blockdustry 现有
// dumpItem 语义向相邻传送带/建筑卸货，保证与物流交接；下游不收则积存在库内喵。
// 注意：核心链接（Mindustry 放核心旁 linkedCore 直通核心池）本轮不做，属已知简化喵
public class ContainerBlockEntity extends BlockdustryBuildingEntity {
    public static final int ITEM_CAPACITY = 300; // Mindustry container.itemCapacity = 300 喵

    // 多类型库存：Item → 数量（LinkedHashMap 保插入序，展示/卸货稳定）喵
    private final Map<Item, Integer> contents = new LinkedHashMap<>();
    // 卸货轮询指针（Mindustry ItemModule.take 的 takeRotation，公平轮流卸出各类型）喵
    private int rotation;

    public ContainerBlockEntity(BlockPos pos, BlockState state) {
        super(ContainerRegistrar.CONTAINER_ENTITY.get(), pos, state);
    }

    public ContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 库存快照（渲染/Jade/调试用，只读）喵
    public Map<Item, Integer> getContents() {
        return java.util.Collections.unmodifiableMap(contents);
    }

    // 库存总量（所有类型合计）喵
    public int totalCount() {
        int sum = 0;
        for (int c : contents.values()) sum += c;
        return sum;
    }

    // 第一非空物品（Mindustry ItemModule.first；Jade/渲染只展示主类型）喵
    @Override
    public Item getStoredItem() {
        for (Map.Entry<Item, Integer> e : contents.entrySet()) {
            if (e.getValue() > 0) return e.getKey();
        }
        return null;
    }

    // 主类型数量（Jade 展示「主类型 xN / 300」，忠于 per-type 容量语义）喵
    @Override
    public int getStoredCount() {
        Item first = getStoredItem();
        return first == null ? 0 : contents.getOrDefault(first, 0);
    }

    @Override
    public int getCapacity() {
        return ITEM_CAPACITY;
    }

    // 多类型容量判定：该类型数量 < itemCapacity 即可收（Mindustry separateItemCapacity）喵
    @Override
    public boolean acceptsItem(Item item) {
        return item != null && item != Items.AIR && contents.getOrDefault(item, 0) < ITEM_CAPACITY;
    }

    // 多类型存储整体不判满，按类型判（acceptsItem）喵
    @Override
    public boolean isFull() {
        return false;
    }

    // 入自己库存（原 acceptItem(Item) 改名），类型已满则拒收喵
    @Override
    public void storeItem(Item item) {
        if (!acceptsItem(item)) return;
        contents.put(item, contents.getOrDefault(item, 0) + 1);
        setChanged();
    }

    // 从轮询指针起取出一个已存储物品（Mindustry ItemModule.take 轮转），空返回 null 喵
    @Override
    public Item removeOne() {
        List<Item> keys = new ArrayList<>(contents.keySet());
        if (keys.isEmpty()) return null;
        int n = keys.size();
        for (int i = 0; i < n; i++) {
            int idx = (rotation + i) % n;
            Item it = keys.get(idx);
            int c = contents.getOrDefault(it, 0);
            if (c > 0) {
                if (c == 1) contents.remove(it);
                else contents.put(it, c - 1);
                rotation = (idx + 1) % n;
                setChanged();
                return it;
            }
        }
        return null;
    }

    // 每模组 tick：从轮询指针起找非空类型，卸给相邻可接收的传送带/建筑；下游不收则积存喵
    @Override
    protected void tickAnchor() {
        if (contents.isEmpty()) return;
        List<Item> keys = new ArrayList<>(contents.keySet());
        int n = keys.size();
        for (int i = 0; i < n; i++) {
            int idx = (rotation + i) % n;
            Item it = keys.get(idx);
            if (contents.getOrDefault(it, 0) > 0 && dumpItem(it)) {
                // 卸出的恰是该类型（dumpItem 只传件不改库），扣一个；指针移到下一位喵
                int c = contents.get(it);
                if (c == 1) contents.remove(it);
                else contents.put(it, c - 1);
                rotation = (idx + 1) % n;
                setChanged();
                break;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("bd_capacity", ITEM_CAPACITY);
        ListTag list = new ListTag();
        for (Map.Entry<Item, Integer> e : contents.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putString("item", BuiltInRegistries.ITEM.getKey(e.getKey()).toString());
            t.putInt("count", e.getValue());
            list.add(t);
        }
        tag.put("bd_container_items", list);
        tag.putInt("bd_container_rot", rotation);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        contents.clear();
        ListTag list = tag.getList("bd_container_items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(t.getString("item")));
            int count = t.getInt("count");
            if (item != null && item != Items.AIR && count > 0) contents.put(item, count);
        }
        rotation = Math.max(0, tag.getInt("bd_container_rot"));
    }
}
