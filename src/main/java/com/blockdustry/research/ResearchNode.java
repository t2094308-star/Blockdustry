package com.blockdustry.research;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

// 科技树节点（Mindustry TechNode 结构忠实移植，从零重做）喵
// - 单父多叉树：每个节点至多一个 parent（parentId 为空=根），children 由 ResearchTree 建边填充喵
// - 研究需求 = 公式(自身 costMultiplier) + 沿父链累乘的 perItemMultiplier（Mindustry researchCostMultipliers 继承）喵
// - 自身不可变；parent/children/depth/effectivePerItem/requirements 由 ResearchTree 构建期计算（graph 字段）喵
public final class ResearchNode {
    public final ResourceLocation id;                     // 节点 id（blockdustry:drill）喵
    public final ResourceLocation parentId;               // 父节点 id，根为 null（Mindustry 单父 TechNode）喵
    public final ResourceLocation unlockContent;          // 解锁内容（方块/物品 RL），门控用喵
    public final boolean defaultUnlocked;                 // 默认解锁（Mindustry alwaysUnlocked，如 core-shard/sandbox）喵
    public final Map<Item, Integer> buildRequirements;    // Mindustry 建造配方（公式输入：建造量）喵
    public final Map<Item, Integer> researchCostOverride; // 显式研究成本覆盖（Mindustry researchCost，如 conveyor=铜×5）喵
    public final float costMultiplier;                    // 自身成本倍率（Mindustry researchCostMultiplier，如 groundFactory=0.5）喵
    public final Map<Item, Float> perItemMultiplier;      // 自身按物品追加倍率（Mindustry researchCostMultipliers，沿父链累乘）喵

    // —— 构建期由 ResearchTree 填充的图字段 —— 喵
    private ResearchNode parent;
    private final List<ResearchNode> children = new ArrayList<>();
    private int depth;
    private Map<Item, Float> effectivePerItem = Map.of();  // 沿父链累乘后的按物品倍率喵
    private Map<Item, Integer> requirements = Map.of();    // 最终有效研究需求喵

    private ResearchNode(Builder b) {
        this.id = b.id;
        this.parentId = b.parentId;
        this.unlockContent = b.unlockContent;
        this.defaultUnlocked = b.defaultUnlocked;
        this.buildRequirements = Map.copyOf(b.buildRequirements);
        this.researchCostOverride = Map.copyOf(b.researchCostOverride);
        this.costMultiplier = b.costMultiplier;
        this.perItemMultiplier = Map.copyOf(b.perItemMultiplier);
    }

    // 便捷构造 blockdustry:path 节点喵
    public static Builder builder(String path) {
        return new Builder(ResourceLocation.fromNamespaceAndPath("blockdustry", path));
    }

    public ResearchNode parent() { return parent; }
    public List<ResearchNode> children() { return children; }
    public int depth() { return depth; }
    public Map<Item, Float> effectivePerItem() { return effectivePerItem; }
    public Map<Item, Integer> requirements() { return requirements; }
    public boolean isRoot() { return parentId == null; }

    void setParent(ResearchNode parent) { this.parent = parent; }
    void addChild(ResearchNode child) { children.add(child); }
    void setDepth(int depth) { this.depth = depth; }
    void setEffectivePerItem(Map<Item, Float> m) { this.effectivePerItem = Map.copyOf(m); }
    void setRequirements(Map<Item, Integer> m) { this.requirements = Map.copyOf(m); }

    public static final class Builder {
        final ResourceLocation id;
        ResourceLocation parentId;
        ResourceLocation unlockContent;
        boolean defaultUnlocked;
        final Map<Item, Integer> buildRequirements = new LinkedHashMap<>();
        final Map<Item, Integer> researchCostOverride = new LinkedHashMap<>();
        float costMultiplier = 1f;
        final Map<Item, Float> perItemMultiplier = new LinkedHashMap<>();

        Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder parent(String path) {
            this.parentId = ResourceLocation.fromNamespaceAndPath("blockdustry", path);
            return this;
        }

        public Builder unlockBlock(Block block) {
            this.unlockContent = BuiltInRegistries.BLOCK.getKey(block);
            return this;
        }

        public Builder unlockItem(Item item) {
            this.unlockContent = BuiltInRegistries.ITEM.getKey(item);
            return this;
        }

        public Builder defaultUnlocked(boolean v) {
            this.defaultUnlocked = v;
            return this;
        }

        public Builder buildRequirement(Item item, int count) {
            this.buildRequirements.put(item, count);
            return this;
        }

        public Builder researchCost(Item item, int count) {
            this.researchCostOverride.put(item, count);
            return this;
        }

        public Builder costMultiplier(float v) {
            this.costMultiplier = v;
            return this;
        }

        public Builder perItem(Item item, float v) {
            this.perItemMultiplier.put(item, v);
            return this;
        }

        public ResearchNode build() {
            return new ResearchNode(this);
        }
    }
}
