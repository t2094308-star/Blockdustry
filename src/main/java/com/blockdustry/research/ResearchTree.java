package com.blockdustry.research;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

// 科技树依赖图（从零重做）：从 ResearchNodes 平面列表构建单父树，计算深度、沿父链累乘倍率与最终研究需求喵
// 构建语义对齐 Mindustry TechNode：costMultiplier 与 perItemMultiplier 都沿父链累乘（effectiveMult/effectivePerItem）喵
public final class ResearchTree {
    private static volatile ResearchTree INSTANCE;

    private final Map<ResourceLocation, ResearchNode> byId = new LinkedHashMap<>();
    private final Map<ResourceLocation, ResearchNode> byUnlockContent = new LinkedHashMap<>();
    private final List<ResearchNode> roots = new ArrayList<>();

    private ResearchTree(List<ResearchNode> nodes) {
        // 1. 注册 id 与解锁内容索引喵
        for (ResearchNode n : nodes) {
            if (n.id == null || n.unlockContent == null) continue;
            byId.put(n.id, n);
            byUnlockContent.put(n.unlockContent, n);
        }
        // 2. 建 parent/children 单父链接，收集根喵
        for (ResearchNode n : nodes) {
            if (n.parentId != null) {
                ResearchNode p = byId.get(n.parentId);
                if (p != null) {
                    n.setParent(p);
                    p.addChild(n);
                }
            }
            if (n.isRoot()) roots.add(n);
        }
        // 3. BFS 计算深度（根 depth=0，父.depth+1，防环）喵
        Deque<ResearchNode> queue = new ArrayDeque<>();
        for (ResearchNode r : roots) {
            r.setDepth(0);
            queue.add(r);
        }
        while (!queue.isEmpty()) {
            ResearchNode cur = queue.poll();
            for (ResearchNode c : cur.children()) {
                c.setDepth(cur.depth() + 1);
                queue.add(c);
            }
        }
        // 4. 从根 DFS 沿父链累乘 costMultiplier 与 perItemMultiplier 喵
        for (ResearchNode root : roots) {
            computeEffective(root, 1f, new HashMap<>());
        }
        // 5. 计算每节点最终有效研究需求喵
        for (ResearchNode n : nodes) {
            n.setRequirements(computeRequirements(n));
        }
    }

    // 沿父链累乘：子有效倍率 = 父有效倍率 * 自身倍率；per-item 同理累乘喵
    private void computeEffective(ResearchNode node, float parentMult, Map<Item, Float> parentPerItem) {
        float mult = parentMult * node.costMultiplier;
        Map<Item, Float> perItem = new HashMap<>(parentPerItem);
        node.perItemMultiplier.forEach((item, v) -> perItem.put(item, perItem.getOrDefault(item, 1f) * v));
        node.setEffectivePerItem(perItem);
        for (ResearchNode c : node.children()) {
            computeEffective(c, mult, perItem);
        }
    }

    // 有效研究需求：有显式 researchCost 覆盖则用之，否则对每项建造物量套 Mindustry 公式（用沿父链累乘倍率）喵
    private Map<Item, Integer> computeRequirements(ResearchNode node) {
        if (!node.researchCostOverride.isEmpty()) {
            return node.researchCostOverride;
        }
        Map<Item, Integer> out = new LinkedHashMap<>();
        // 有效标量倍率 = 沿父链累乘（含自身）喵
        float mult = 1f;
        ResearchNode cur = node;
        while (cur != null) {
            mult *= cur.costMultiplier;
            cur = cur.parent();
        }
        Map<Item, Float> perItem = node.effectivePerItem();
        for (Map.Entry<Item, Integer> e : node.buildRequirements.entrySet()) {
            Item item = e.getKey();
            int amount = e.getValue();
            float p = perItem.getOrDefault(item, 1f);
            int qty = ResearchCost.researchCost(mult, amount, p);
            if (qty > 0) out.put(item, qty);
        }
        return out;
    }

    // 懒加载单例（双检锁），首次访问发生在注册完成后，注册表已冻结喵
    public static ResearchTree get() {
        ResearchTree t = INSTANCE;
        if (t == null) {
            synchronized (ResearchTree.class) {
                t = INSTANCE;
                if (t == null) {
                    t = new ResearchTree(ResearchNodes.all());
                    INSTANCE = t;
                }
            }
        }
        return t;
    }

    public ResearchNode node(ResourceLocation id) {
        return byId.get(id);
    }

    // 门控用：方块 -> 解锁它的节点喵
    public ResearchNode nodeForBlock(Block block) {
        return byUnlockContent.get(BuiltInRegistries.BLOCK.getKey(block));
    }

    // 子节点 id 列表（UI 遍历/树绘制用）喵
    public List<ResourceLocation> childrenOf(ResourceLocation id) {
        ResearchNode n = byId.get(id);
        if (n == null) return List.of();
        List<ResourceLocation> out = new ArrayList<>(n.children().size());
        for (ResearchNode c : n.children()) out.add(c.id);
        return out;
    }

    public int depthOf(ResourceLocation id) {
        ResearchNode n = byId.get(id);
        return n == null ? 0 : n.depth();
    }

    public List<ResearchNode> roots() {
        return roots;
    }

    public boolean hasNode(ResourceLocation id) {
        return byId.containsKey(id);
    }

    // 全部节点（UI 遍历/树绘制用，顺序 = 注册顺序）喵
    public List<ResearchNode> allNodes() {
        return List.copyOf(byId.values());
    }

    // 有效研究需求（含倍率与公式），供研究面板与 spend 用喵
    public Map<Item, Integer> effectiveRequirements(ResearchNode node) {
        return node.requirements();
    }
}
