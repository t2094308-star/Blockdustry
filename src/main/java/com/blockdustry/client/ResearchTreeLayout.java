package com.blockdustry.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blockdustry.research.ResearchNode;
import com.blockdustry.research.ResearchTree;

import net.minecraft.resources.ResourceLocation;

// Mindustry ResearchDialog 树布局的精确移植（BranchTreeLayout = Reingold-Tilford 算法）喵
// 根的子树分为左右两半：左半 top 布局（往下）、右半 bottom 布局（往上），再 shift 左半对齐，形成 Mindustry 标志性平衡树喵
public final class ResearchTreeLayout {
    private ResearchTreeLayout() {}

    // 返回各节点中心坐标 + 整树包围盒（含方框半宽）喵
    public static LayoutResult layout(ResearchNode rootNode, float nodeSize, float spacing) {
        LNode root = build(rootNode, nodeSize);
        List<LNode> children = new ArrayList<>(root.children);
        int split = (int) Math.ceil(children.size() / 2f);
        List<LNode> left = new ArrayList<>(children.subList(0, split));
        List<LNode> right = new ArrayList<>(children.subList(split, children.size()));

        root.children = left;
        new BranchLayout(Loc.top, spacing, nodeSize).layout(root);
        float lastY = root.y;

        if (!right.isEmpty()) {
            root.children = right;
            new BranchLayout(Loc.bottom, spacing, nodeSize).layout(root);
            shift(left, root.y - lastY);
        }
        root.children = children;

        Map<ResourceLocation, float[]> positions = new HashMap<>();
        copy(root, positions);

        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (float[] p : positions.values()) {
            minX = Math.min(minX, p[0] - nodeSize / 2);
            maxX = Math.max(maxX, p[0] + nodeSize / 2);
            minY = Math.min(minY, p[1] - nodeSize / 2);
            maxY = Math.max(maxY, p[1] + nodeSize / 2);
        }
        return new LayoutResult(positions, minX, maxX, minY, maxY);
    }

    private static LNode build(ResearchNode node, float nodeSize) {
        LNode n = new LNode();
        n.node = node;
        n.width = n.height = nodeSize;
        for (ResourceLocation cid : ResearchTree.get().childrenOf(node.id)) {
            ResearchNode c = ResearchTree.get().node(cid);
            if (c == null) continue;
            LNode cn = build(c, nodeSize);
            cn.parent = n;
            n.children.add(cn);
        }
        return n;
    }

    private static void shift(List<LNode> children, float amount) {
        for (LNode n : children) {
            n.y += amount;
            if (!n.children.isEmpty()) shift(n.children, amount);
        }
    }

    private static void copy(LNode node, Map<ResourceLocation, float[]> out) {
        out.put(node.node.id, new float[]{node.x, node.y});
        for (LNode c : node.children) copy(c, out);
    }

    private enum Loc {
        top, bottom
    }

    // 布局结果：节点id → [x, y] 中心坐标 + 包围盒喵
    public static final class LayoutResult {
        public final Map<ResourceLocation, float[]> positions;
        public final float minX, maxX, minY, maxY;

        LayoutResult(Map<ResourceLocation, float[]> positions, float minX, float maxX, float minY, float maxY) {
            this.positions = positions;
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }
    }

    // 布局用树节点（Reingold-Tilford 内部字段）喵
    private static final class LNode {
        ResearchNode node;
        List<LNode> children = new ArrayList<>();
        LNode parent;
        float width, height, x, y;
        float mode, prelim, change, shift;
        float cachedWidth = -1f;
        int number = -1;
        LNode thread, ancestor;

        boolean isLeaf() {
            return children.isEmpty();
        }

        float calcWidth() {
            if (children.isEmpty()) return width;
            if (cachedWidth > 0) return cachedWidth;
            float cw = 0;
            for (LNode c : children) cw += c.calcWidth();
            return cachedWidth = Math.max(width, cw);
        }
    }

    // BranchTreeLayout 单次布局（rootLocation=top/bottom）喵
    private static final class BranchLayout {
        private final Loc loc;
        private final float gapBetweenLevels;
        private final float gapBetweenNodes;
        private final List<Float> sizeOfLevel = new ArrayList<>();

        BranchLayout(Loc loc, float spacing, float nodeSize) {
            this.loc = loc;
            this.gapBetweenLevels = spacing;
            this.gapBetweenNodes = spacing;
        }

        void layout(LNode root) {
            firstWalk(root, null);
            calcSizeOfLevels(root, 0);
            secondWalk(root, -root.prelim, 0, 0);
        }

        private boolean yAxis() {
            return loc == Loc.top || loc == Loc.bottom;
        }

        private int levelSign() {
            return loc == Loc.bottom ? -1 : 1;
        }

        private float nodeThickness(LNode n) {
            return n.height;
        }

        private void calcSizeOfLevels(LNode node, int level) {
            float oldSize;
            if (sizeOfLevel.size() <= level) {
                sizeOfLevel.add(0f);
                oldSize = 0f;
            } else {
                oldSize = sizeOfLevel.get(level);
            }
            float size = nodeThickness(node);
            if (oldSize < size) {
                sizeOfLevel.set(level, size);
            }
            if (!node.isLeaf()) {
                for (LNode c : node.children) calcSizeOfLevels(c, level + 1);
            }
        }

        private float getSizeOfLevel(int level) {
            return sizeOfLevel.get(level);
        }

        private float getDistance(LNode v, LNode w) {
            return (v.width + w.width) / 2f + gapBetweenNodes;
        }

        private LNode nextLeft(LNode v) {
            return v.isLeaf() ? v.thread : v.children.get(0);
        }

        private LNode nextRight(LNode v) {
            return v.isLeaf() ? v.thread : v.children.get(v.children.size() - 1);
        }

        private int getNumber(LNode node, LNode parent) {
            if (node.number == -1) {
                int num = 1;
                for (LNode c : parent.children) c.number = num++;
            }
            return node.number;
        }

        private LNode ancestor(LNode vIMinus, LNode parentOfV, LNode defaultAncestor) {
            LNode a = vIMinus.ancestor != null ? vIMinus.ancestor : vIMinus;
            return a.parent == parentOfV ? a : defaultAncestor;
        }

        private void moveSubtree(LNode wMinus, LNode wPlus, LNode parent, float shift) {
            int subtrees = getNumber(wPlus, parent) - getNumber(wMinus, parent);
            wPlus.change = wPlus.change - shift / subtrees;
            wPlus.shift = wPlus.shift + shift;
            wMinus.change = wMinus.change + shift / subtrees;
            wPlus.prelim = wPlus.prelim + shift;
            wPlus.mode = wPlus.mode + shift;
        }

        private LNode apportion(LNode v, LNode defaultAncestor, LNode leftSibling, LNode parentOfV) {
            if (leftSibling == null) return defaultAncestor;
            LNode vOPlus = v;
            LNode vIPlus = v;
            LNode vIMinus = leftSibling;
            LNode vOMinus = parentOfV.children.get(0);
            float sIPlus = vIPlus.mode;
            float sOPlus = vOPlus.mode;
            float sIMinus = vIMinus.mode;
            float sOMinus = vOMinus.mode;
            LNode nextRightVIMinus = nextRight(vIMinus);
            LNode nextLeftVIPlus = nextLeft(vIPlus);
            while (nextRightVIMinus != null && nextLeftVIPlus != null) {
                vIMinus = nextRightVIMinus;
                vIPlus = nextLeftVIPlus;
                vOMinus = nextLeft(vOMinus);
                vOPlus = nextRight(vOPlus);
                vOPlus.ancestor = v;
                float shift = (vIMinus.prelim + sIMinus) - (vIPlus.prelim + sIPlus) + getDistance(vIMinus, vIPlus);
                if (shift > 0) {
                    moveSubtree(ancestor(vIMinus, parentOfV, defaultAncestor), v, parentOfV, shift);
                    sIPlus += shift;
                    sOPlus += shift;
                }
                sIMinus += vIMinus.mode;
                sIPlus += vIPlus.mode;
                sOMinus += vOMinus.mode;
                sOPlus += vOPlus.mode;
                nextRightVIMinus = nextRight(vIMinus);
                nextLeftVIPlus = nextLeft(vIPlus);
            }
            if (nextRightVIMinus != null && nextRight(vOPlus) == null) {
                vOPlus.thread = nextRightVIMinus;
                vOPlus.mode += sIMinus - sOPlus;
            }
            if (nextLeftVIPlus != null && nextLeft(vOMinus) == null) {
                vOMinus.thread = nextLeftVIPlus;
                vOMinus.mode += sIPlus - sOMinus;
                defaultAncestor = v;
            }
            return defaultAncestor;
        }

        private void executeShifts(LNode v) {
            float shift = 0;
            float change = 0;
            for (int i = v.children.size() - 1; i >= 0; i--) {
                LNode w = v.children.get(i);
                change += w.change;
                w.prelim += shift;
                w.mode += shift;
                shift += w.shift + change;
            }
        }

        private void firstWalk(LNode v, LNode leftSibling) {
            if (v.isLeaf()) {
                if (leftSibling != null) {
                    v.prelim = leftSibling.prelim + getDistance(v, leftSibling);
                }
            } else {
                LNode defaultAncestor = v.children.get(0);
                LNode previousChild = null;
                for (LNode w : v.children) {
                    firstWalk(w, previousChild);
                    defaultAncestor = apportion(w, defaultAncestor, previousChild, v);
                    previousChild = w;
                }
                executeShifts(v);
                float midpoint = (v.children.get(0).prelim + v.children.get(v.children.size() - 1).prelim) / 2f;
                if (leftSibling != null) {
                    v.prelim = leftSibling.prelim + getDistance(v, leftSibling);
                    v.mode = v.prelim - midpoint;
                } else {
                    v.prelim = midpoint;
                }
            }
        }

        private void secondWalk(LNode v, float m, int level, float levelStart) {
            float levelSign = levelSign();
            boolean yAxis = yAxis();
            float levelSize = getSizeOfLevel(level);
            float x = v.prelim + m;
            float y;
            // alignment = awayFromRoot（BranchTreeLayout 默认）喵
            y = levelStart + levelSize - levelSign * (nodeThickness(v) / 2f);
            if (!yAxis) {
                float t = x;
                x = y;
                y = t;
            }
            v.x = x;
            v.y = y;
            if (!v.isLeaf()) {
                float nextLevelStart = levelStart + (levelSize + gapBetweenLevels) * levelSign;
                for (LNode w : v.children) {
                    secondWalk(w, m + v.mode, level + 1, nextLevelStart);
                }
            }
        }
    }
}
