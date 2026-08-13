package com.blockdustry.research;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blockdustry.Blockdustry;
import com.blockdustry.BlockdustryTeams;
import com.blockdustry.team.BlockdustryTeam;
import com.blockdustry.team.BlockdustryTeamStorage;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.network.PacketDistributor;

// 研究服务端权威 API：解锁判断、研究入口（spend）、递归解锁与自动解锁喵
// 研究消耗 = 扣队伍共享资源（BlockdustryTeamStorage 核心池，非背包；DERELICT 兜底 SHARDED）——用户要求唯一偏离 Mindustry 处喵
// 门控/UI/网络都只调本类；本类只读 ResearchTree + ResearchSavedData + TeamStorage，不碰网络喵
@EventBusSubscriber(modid = Blockdustry.MODID)
public final class ResearchManager {
    private ResearchManager() {}

    // 玩家所属队伍，DERELICT（无主/中立）兜底回退 SHARDED（与网络查询核心物资同款语义）喵
    public static BlockdustryTeam teamOf(ServerPlayer player) {
        BlockdustryTeam team = BlockdustryTeams.getTeam(player);
        return team == BlockdustryTeam.DERELICT ? BlockdustryTeam.SHARDED : team;
    }

    // 服务端：节点是否已解锁（默认解锁节点恒为真）喵
    public static boolean isUnlocked(ServerLevel level, ResourceLocation id) {
        ResearchNode node = ResearchTree.get().node(id);
        if (node == null) return false;
        if (node.defaultUnlocked) return true;
        return ResearchSavedData.get(level).isUnlocked(id);
    }

    // 服务端：是否可研究（父全部解锁 + 自身未解锁）喵
    public static boolean canResearch(ServerLevel level, ResourceLocation id) {
        ResearchTree tree = ResearchTree.get();
        ResearchNode node = tree.node(id);
        if (node == null || node.defaultUnlocked) return false;
        if (isUnlocked(level, id)) return false;
        ResearchNode p = node.parent();
        return p == null || isUnlocked(level, p.id);
    }

    // 研究入口：从队伍共享池尽力扣料并记进度；满则解锁并递归自动解锁。返回本次投入的材料总件数喵
    // 镜像 Mindustry ResearchDialog.spend：used = min(need - completed, available)，扣完记进度喵
    public static int spend(ServerPlayer player, ResourceLocation id) {
        ServerLevel level = player.serverLevel();
        ResearchTree tree = ResearchTree.get();
        ResearchNode node = tree.node(id);
        if (node == null || !canResearch(level, id)) return 0;

        ResearchSavedData data = ResearchSavedData.get(level);
        Map<Item, Integer> reqs = tree.effectiveRequirements(node);
        if (reqs.isEmpty()) {
            // Mindustry：需求为空且父已解锁 → 自动解锁（checkAutoUnlocks）喵
            unlock(level, id);
            return 0;
        }

        BlockdustryTeamStorage.Storage storage = BlockdustryTeamStorage.get(level, teamOf(player));
        int totalSpent = 0;
        boolean complete = true;
        for (Map.Entry<Item, Integer> e : reqs.entrySet()) {
            Item item = e.getKey();
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            int need = e.getValue();
            int invested = data.getProgress(id, itemId);
            int gap = need - invested;
            if (gap <= 0) continue;
            // 从队伍共享池扣（尽力扣，最多扣 gap 个）；共享池为空则本次不扣喵
            int removed = storage.take(item, gap);
            if (removed > 0) {
                data.addProgress(id, itemId, removed);
                totalSpent += removed;
            }
            if (invested + removed < need) complete = false;
        }
        if (complete) unlock(level, id);
        return totalSpent;
    }

    // 解锁：沿父链全解锁（保证多人一致，镜像 Mindustry ResearchDialog.unlock），再自动解锁免费子节点喵
    public static void unlock(ServerLevel level, ResourceLocation id) {
        ResearchTree tree = ResearchTree.get();
        ResearchNode node = tree.node(id);
        if (node == null) return;
        ResearchSavedData data = ResearchSavedData.get(level);
        // 收集父链（含自身），从根向叶逐个解锁喵
        List<ResourceLocation> chain = new ArrayList<>();
        ResearchNode cur = node;
        while (cur != null && !data.isUnlocked(cur.id)) {
            chain.add(cur.id);
            cur = cur.parent();
        }
        for (int i = chain.size() - 1; i >= 0; i--) {
            data.unlock(chain.get(i));
        }
        // 自动解锁：父已解锁 + 有效需求为空的子节点（Mindustry checkAutoUnlocks 简化版）喵
        autoUnlockChildren(level, node.id);
    }

    private static void autoUnlockChildren(ServerLevel level, ResourceLocation id) {
        ResearchTree tree = ResearchTree.get();
        ResearchSavedData data = ResearchSavedData.get(level);
        for (ResourceLocation childId : tree.childrenOf(id)) {
            ResearchNode child = tree.node(childId);
            if (child == null || child.defaultUnlocked || data.isUnlocked(childId)) continue;
            ResearchNode p = child.parent();
            boolean parentsUnlocked = p == null || isUnlocked(level, p.id);
            boolean noReq = tree.effectiveRequirements(child).values().stream().allMatch(v -> v <= 0);
            if (parentsUnlocked && noReq) {
                data.unlock(childId);
                autoUnlockChildren(level, childId);
            }
        }
    }

    // 调试/管理：一键解锁所有非默认科技节点（服务端存档），返回本次新解锁数喵
    public static int unlockAll(ServerLevel level) {
        ResearchTree tree = ResearchTree.get();
        ResearchSavedData data = ResearchSavedData.get(level);
        int count = 0;
        for (ResearchNode n : tree.allNodes()) {
            if (!n.defaultUnlocked && !data.isUnlocked(n.id)) {
                data.unlock(n.id);
                count++;
            }
        }
        return count;
    }

    // 构建玩家队伍共享池物品数表（item RL 字符串 → 数量），供客户端研究面板显示「剩余/需求」喵
    public static Map<String, Integer> buildStorage(ServerPlayer player) {
        BlockdustryTeamStorage.Storage storage = BlockdustryTeamStorage.get(player.serverLevel(), teamOf(player));
        Map<String, Integer> out = new HashMap<>();
        storage.getAll().forEach((item, count) -> out.put(BuiltInRegistries.ITEM.getKey(item).toString(), count));
        return out;
    }

    // 加入服务器时全量同步研究状态 + 队伍共享池给该玩家（B 组网络补全）喵
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, buildState(player.serverLevel()));
            PacketDistributor.sendToPlayer(player, new ResearchStoragePayload(buildStorage(player)));
        }
    }

    // 构建服务端→客户端全量状态包（unlocked 集合 + progress 表，用字符串 RL 传输）喵
    public static ResearchStatePayload buildState(ServerLevel level) {
        ResearchSavedData data = ResearchSavedData.get(level);
        Set<String> unlocked = new HashSet<>();
        for (ResourceLocation id : data.unlockedSet()) unlocked.add(id.toString());
        Map<String, Map<String, Integer>> progress = new HashMap<>();
        data.progressMap().forEach((nodeId, itemMap) -> {
            Map<String, Integer> m = new HashMap<>();
            itemMap.forEach((itemId, count) -> m.put(itemId.toString(), count));
            progress.put(nodeId.toString(), m);
        });
        return new ResearchStatePayload(unlocked, progress);
    }
}
