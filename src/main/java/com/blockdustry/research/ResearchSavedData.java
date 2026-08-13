package com.blockdustry.research;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

// 研究进度持久化：每世界一份（挂在 overworld），跨维度共享喵
// unlocked = 已解锁节点集合；progress = 节点id -> (物品id -> 已投入数量) 喵
// 每处变更显式 setDirty()，保证落盘（规避 Level attachment 原地改不持久化坑）喵
public class ResearchSavedData extends SavedData {
    public static final String DATA_NAME = "blockdustry_research";

    private final Set<ResourceLocation> unlocked = new HashSet<>();
    private final Map<ResourceLocation, Map<ResourceLocation, Integer>> progress = new HashMap<>();

    // 取 overworld 的实例（所有维度统一走这里，保证同一份存档）喵
    public static ResearchSavedData get(ServerLevel level) {
        var storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(
                new SavedData.Factory<ResearchSavedData>(ResearchSavedData::new, ResearchSavedData::load), DATA_NAME);
    }

    public boolean isUnlocked(ResourceLocation id) {
        return unlocked.contains(id);
    }

    public void unlock(ResourceLocation id) {
        if (unlocked.add(id)) setDirty();
    }

    public Set<ResourceLocation> unlockedSet() {
        return unlocked;
    }

    // 完整进度表（服务端序列化/同步用，只读遍历）喵
    public Map<ResourceLocation, Map<ResourceLocation, Integer>> progressMap() {
        return progress;
    }

    // 懒建节点进度表喵
    private Map<ResourceLocation, Integer> progressOf(ResourceLocation nodeId) {
        return progress.computeIfAbsent(nodeId, k -> new HashMap<>());
    }

    public int getProgress(ResourceLocation nodeId, ResourceLocation itemId) {
        return progressOf(nodeId).getOrDefault(itemId, 0);
    }

    public void addProgress(ResourceLocation nodeId, ResourceLocation itemId, int count) {
        if (count <= 0) return;
        progressOf(nodeId).merge(itemId, count, Integer::sum);
        setDirty();
    }

    // 节点是否已投入满需求（由调用方比对有效需求）喵
    public boolean isFullyResearched(ResourceLocation nodeId, Map<ResourceLocation, Integer> needs) {
        Map<ResourceLocation, Integer> p = progressOf(nodeId);
        for (Map.Entry<ResourceLocation, Integer> e : needs.entrySet()) {
            if (p.getOrDefault(e.getKey(), 0) < e.getValue()) return false;
        }
        return true;
    }

    private static ResearchSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        ResearchSavedData data = new ResearchSavedData();
        ListTag unlockedTag = tag.getList("unlocked", Tag.TAG_STRING);
        for (int i = 0; i < unlockedTag.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(unlockedTag.getString(i));
            if (id != null) data.unlocked.add(id);
        }
        CompoundTag progressTag = tag.getCompound("progress");
        for (String nodeKey : progressTag.getAllKeys()) {
            ResourceLocation nodeId = ResourceLocation.tryParse(nodeKey);
            if (nodeId == null) continue;
            CompoundTag itemTag = progressTag.getCompound(nodeKey);
            Map<ResourceLocation, Integer> map = new HashMap<>();
            for (String itemKey : itemTag.getAllKeys()) {
                ResourceLocation itemId = ResourceLocation.tryParse(itemKey);
                if (itemId != null) map.put(itemId, itemTag.getInt(itemKey));
            }
            data.progress.put(nodeId, map);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag unlockedTag = new ListTag();
        for (ResourceLocation id : unlocked) {
            unlockedTag.add(StringTag.valueOf(id.toString()));
        }
        tag.put("unlocked", unlockedTag);
        CompoundTag progressTag = new CompoundTag();
        progress.forEach((nodeId, map) -> {
            CompoundTag itemTag = new CompoundTag();
            map.forEach((itemId, count) -> itemTag.putInt(itemId.toString(), count));
            if (!itemTag.isEmpty()) progressTag.put(nodeId.toString(), itemTag);
        });
        tag.put("progress", progressTag);
        return tag;
    }
}
