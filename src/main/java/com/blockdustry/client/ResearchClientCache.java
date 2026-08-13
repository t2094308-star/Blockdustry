package com.blockdustry.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.blockdustry.research.ResearchNode;
import com.blockdustry.research.ResearchTree;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

// 客户端研究状态缓存：网络同步写入，Screen/创意栏过滤只读喵
public final class ResearchClientCache {
    private static final Set<ResourceLocation> UNLOCKED = new HashSet<>();
    private static final Map<ResourceLocation, Map<ResourceLocation, Integer>> PROGRESS = new HashMap<>();
    private static final Map<ResourceLocation, Integer> STORAGE = new HashMap<>();

    private ResearchClientCache() {}

    // 队伍共享池物品数更新（网络收到 ResearchStoragePayload 时调用）喵
    public static void updateStorage(Map<String, Integer> items) {
        STORAGE.clear();
        items.forEach((item, count) -> {
            ResourceLocation itemId = ResourceLocation.tryParse(item);
            if (itemId != null) STORAGE.put(itemId, count);
        });
    }

    // 队伍共享池某物品存量（材料图标旁「剩余」显示用）喵
    public static int getStorage(Item item) {
        if (item == null) return 0;
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        return STORAGE.getOrDefault(itemId, 0);
    }

    // 全量更新（网络收到 ResearchStatePayload 时调用，服务端线程 enqueueWork 到主线程）喵
    public static void update(Set<String> unlocked, Map<String, Map<String, Integer>> progress) {
        UNLOCKED.clear();
        for (String s : unlocked) {
            ResourceLocation id = ResourceLocation.tryParse(s);
            if (id != null) UNLOCKED.add(id);
        }
        PROGRESS.clear();
        progress.forEach((node, itemMap) -> {
            ResourceLocation nodeId = ResourceLocation.tryParse(node);
            if (nodeId == null) return;
            Map<ResourceLocation, Integer> m = new HashMap<>();
            itemMap.forEach((item, count) -> {
                ResourceLocation itemId = ResourceLocation.tryParse(item);
                if (itemId != null) m.put(itemId, count);
            });
            PROGRESS.put(nodeId, m);
        });
    }

    // 是否已解锁（默认解锁节点恒真）喵
    public static boolean isUnlocked(ResourceLocation id) {
        if (UNLOCKED.contains(id)) return true;
        ResearchNode n = ResearchTree.get().node(id);
        return n != null && n.defaultUnlocked;
    }

    // 节点某材料已投入量喵
    public static int getProgress(ResourceLocation nodeId, ResourceLocation itemId) {
        Map<ResourceLocation, Integer> m = PROGRESS.get(nodeId);
        return m == null ? 0 : m.getOrDefault(itemId, 0);
    }

    // 单节点解锁事件：加缓存 + 游戏内提示 + 解锁音（Mindustry uiUnlock）喵
    public static void onUnlock(String nodeId) {
        ResourceLocation id = ResourceLocation.tryParse(nodeId);
        if (id == null) return;
        UNLOCKED.add(id);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ResearchNode node = ResearchTree.get().node(id);
            mc.player.displayClientMessage(Component.translatable(
                    "blockdustry.research.completed",
                    node == null ? Component.literal(nodeId) : nameFor(node)), true);
            mc.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.6f, 1.4f);
        }
    }

    // 节点图标：优先按物品，其次按方块喵
    public static ItemStack iconFor(ResearchNode node) {
        Item item = BuiltInRegistries.ITEM.get(node.unlockContent);
        if (item != null && item != Items.AIR) return new ItemStack(item);
        Block block = BuiltInRegistries.BLOCK.get(node.unlockContent);
        if (block != null && block != Blocks.AIR) return new ItemStack(block);
        return ItemStack.EMPTY;
    }

    // 节点显示名喵
    public static Component nameFor(ResearchNode node) {
        Item item = BuiltInRegistries.ITEM.get(node.unlockContent);
        if (item != null && item != Items.AIR) return item.getDescription();
        Block block = BuiltInRegistries.BLOCK.get(node.unlockContent);
        if (block != null && block != Blocks.AIR) return block.getName();
        return Component.literal(node.id.getPath());
    }
}
