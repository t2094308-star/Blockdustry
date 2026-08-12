package com.blockdustry.team;

import java.util.HashMap;
import java.util.Map;

import com.blockdustry.BlockdustryAttachments;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

// 队伍共享物品存储（Mindustry CoreBlock 共享 storage 迁移）：Level attachment 存 Map<Team, Storage> 喵
public final class BlockdustryTeamStorage {
    private BlockdustryTeamStorage() {}

    // 单队物品池：每物品种类容量 4000（coreShard itemCapacity，Mindustry 共享 storage）喵
    public static class Storage {
        public static final int MAX_PER_TYPE = 4000;
        private final Map<Item, Integer> items = new HashMap<>();

        public boolean canAccept(Item item) {
            return items.getOrDefault(item, 0) < MAX_PER_TYPE;
        }

        public void add(Item item, int count) {
            items.merge(item, count, Integer::sum);
        }

        public boolean canTake(Item item) {
            return items.getOrDefault(item, 0) > 0;
        }

        public boolean take(Item item) {
            int c = items.getOrDefault(item, 0);
            if (c <= 0) return false;
            if (c == 1) items.remove(item);
            else items.put(item, c - 1);
            return true;
        }

        public int getCount(Item item) {
            return items.getOrDefault(item, 0);
        }

        public Map<Item, Integer> getAll() {
            return items;
        }

        public int totalStored() {
            int sum = 0;
            for (int c : items.values()) sum += c;
            return sum;
        }

        // NBT 序列化辅助（attachment serializer 用）喵
        public void readItems(CompoundTag tag) {
            items.clear();
            for (String key : tag.getAllKeys()) {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(key));
                if (item != null && item != Items.AIR) items.put(item, tag.getInt(key));
            }
        }

        public CompoundTag writeItems() {
            CompoundTag tag = new CompoundTag();
            items.forEach((item, count) -> tag.putInt(BuiltInRegistries.ITEM.getKey(item).toString(), count));
            return tag;
        }
    }

    // 取/建某队伍的共享存储喵
    public static Storage get(ServerLevel level, BlockdustryTeam team) {
        var map = level.getData(BlockdustryAttachments.TEAM_STORAGE.get());
        return map.computeIfAbsent(team, t -> new Storage());
    }

    // 某队伍共享池总存量喵
    public static int totalStored(ServerLevel level, BlockdustryTeam team) {
        return get(level, team).totalStored();
    }
}
