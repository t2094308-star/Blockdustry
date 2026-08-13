package com.blockdustry.client;

import java.util.List;
import java.util.Map;

import com.blockdustry.research.ResearchNode;
import com.blockdustry.research.ResearchSpendPayload;
import com.blockdustry.research.ResearchTree;
import com.blockdustry.research.QueryResearchPayload;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;

// 科技树研究面板：精确复刻 Mindustry ResearchDialog（从零重做 v2）喵
// - 布局：全屏树（非左右对半分）——BranchTreeLayout（Reingold-Tilford）+ 左右半平衡，节点遍布全屏喵
// - 缩放：PoseStack pushPose/scale(s,s,1f) 套住整个树画布（节点框/图标/文字/L 形连线都随缩放，见 docs/坑 §9）喵
// - 信息面板：选中节点时在节点旁浮层出现（Mindustry infoTable 跟随 hover 节点），钳制到屏幕内，非固定右栏喵
// - 节点按钮：Mindustry 原版方块贴图（严禁自绘）+ 三态——已解锁=accent 金框、可研究=亮灰框、未达前置=红框喵
// - 研究消耗走队伍共享资源（服务端从 BlockdustryTeamStorage 扣），面板显示共享池存量喵
// - 交互：滚轮围绕鼠标缩放(0.25~1)、左键拖动平移、点击节点选中、点面板研究按钮消耗喵
public class ResearchScreen extends Screen {
    // Mindustry nodeSize=Scl.scl(60)、spacing=20 喵
    private static final float NODE = 56f;
    private static final float SPACING = 20f;
    private static final int ICON_SIZE = 36;   // 节点内小图标盒（Mindustry resizeImage 32 近似）喵
    private static final int PANEL_W = 110;    // 信息面板宽度（缩小至少3倍，原 240）喵

    // Mindustry Pal / Color 精确色值喵
    private static final int ACCENT = 0xffffd37f;      // Pal.accent 金黄喵
    private static final int GRAY = 0xff454545;        // Pal.gray 深灰（锁定连线）喵
    private static final int LIGHT_GRAY = 0xffc6c6c6;  // Color.lightGray 喵
    private static final int SCARLET = 0xffff3619;     // Color.scarlet（需求不足）喵
    private static final int RED = 0xffe55454;         // Pal.remove 红（锁定框）喵
    private static final int PANEL_BG = 0xee1c1c22;    // 信息面板半透明深底喵
    private static final int NODE_FILL = 0xff232323;   // 节点按钮底色（Mindustry button 暗底）喵
    private static final int BG = 0xe00f0f0f;          // 整屏深底喵

    private final List<ResearchNode> nodes;
    private final Map<ResourceLocation, float[]> pos;
    private float scale = 1f, offsetX, offsetY;
    private ResearchNode selected;
    private int[] researchBtn;      // 研究按钮命中区 [x0,y0,x1,y1]，不可用时 null 喵
    private int[] panelRect;        // 信息面板命中区 [x0,y0,x1,y1]，防止点击面板误触拖动喵
    private boolean dragging, moved;
    private double startX, startY;

    public ResearchScreen() {
        super(Component.translatable("screen.blockdustry.research.title"));
        this.nodes = ResearchTree.get().allNodes();
        ResearchNode root = rootNode();
        if (root != null) {
            ResearchTreeLayout.LayoutResult r = ResearchTreeLayout.layout(root, NODE, SPACING);
            this.pos = r.positions;
        } else {
            this.pos = Map.of();
        }
        // 开屏查询全量状态 + 队伍共享池存量喵
        PacketDistributor.sendToServer(new QueryResearchPayload());
    }

    @Override
    protected void init() {
        super.init();
        fitToScreen();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        super.render(g, mx, my, partial);
        g.fill(0, 0, this.width, this.height, BG);
        // 顶部标题（Mindustry titleTable 位置）喵
        g.drawCenteredString(this.font, this.title, this.width / 2, 8, ACCENT);

        // 树画布：PoseStack 整体缩放，节点框/图标/文字/连线都随 scale（docs/坑 §9 scale(s,s,1f)）喵
        int titleH = 30;
        g.enableScissor(0, titleH, this.width, this.height - titleH);
        g.pose().pushPose();
        g.pose().translate(offsetX, offsetY, 0);
        g.pose().scale(scale, scale, 1f);
        drawConnections(g);
        for (ResearchNode node : nodes) {
            if (isVisible(node)) drawNode(g, node);
        }
        g.pose().popPose();
        g.disableScissor();

        drawPanel(g);
    }

    // 信息迷雾（Mindustry checkNodes）：只有「父链全部已解锁」的节点才可见，深层锁定节点隐藏喵
    private boolean isVisible(ResearchNode node) {
        ResearchNode p = node.parent();
        while (p != null) {
            if (!ResearchClientCache.isUnlocked(p.id)) return false;
            p = p.parent();
        }
        return true;
    }

    private boolean isVisibleId(ResourceLocation id) {
        ResearchNode n = ResearchTree.get().node(id);
        return n != null && isVisible(n);
    }

    // —— 交互 ——
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            // 点在信息面板内不触发拖动喵
            if (panelRect != null && mx >= panelRect[0] && mx <= panelRect[2] && my >= panelRect[1] && my <= panelRect[3]) {
                return true;
            }
            this.startX = mx;
            this.startY = my;
            this.dragging = true;
            this.moved = false;
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY) {
        if (button == 0 && this.dragging) {
            if (Math.hypot(mx - this.startX, my - this.startY) > 4) {
                this.moved = true;
            }
            this.offsetX += (float) dragX;
            this.offsetY += (float) dragY;
            return true;
        }
        return super.mouseDragged(mx, my, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0 && this.dragging) {
            this.dragging = false;
            if (!this.moved) {
                // 先判研究按钮喵
                if (this.researchBtn != null && this.selected != null
                        && mx >= researchBtn[0] && mx <= researchBtn[2]
                        && my >= researchBtn[1] && my <= researchBtn[3]) {
                    spend(this.selected);
                    return true;
                }
                // 再判画布节点选中喵
                ResearchNode hit = findHovered((int) mx, (int) my);
                if (hit != null) {
                    this.selected = hit;
                } else if (panelRect == null || !(mx >= panelRect[0] && mx <= panelRect[2] && my >= panelRect[1] && my <= panelRect[3])) {
                    // 点空白处取消选中喵
                    this.selected = null;
                }
                return true;
            }
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horiz, double vert) {
        if (vert != 0) {
            float newScale = Math.max(0.25f, Math.min(1f, this.scale * (vert > 0 ? 1.1f : 0.9f)));
            // 围绕鼠标缩放：保持鼠标下的树坐标不动喵
            float wx = (float) ((mx - this.offsetX) / this.scale);
            float wy = (float) ((my - this.offsetY) / this.scale);
            this.offsetX = (float) (mx - wx * newScale);
            this.offsetY = (float) (my - wy * newScale);
            this.scale = newScale;
            return true;
        }
        return super.mouseScrolled(mx, my, horiz, vert);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    // —— 画布绘制（树坐标空间，已整体套 scale）——
    private void drawConnections(GuiGraphics g) {
        for (ResearchNode node : nodes) {
            if (!isVisible(node)) continue;
            float[] p = pos.get(node.id);
            if (p == null) continue;
            for (ResourceLocation cid : ResearchTree.get().childrenOf(node.id)) {
                if (!isVisibleId(cid)) continue;   // 迷雾：子节点不可见则连线也不画喵
                float[] c = pos.get(cid);
                if (c == null) continue;
                boolean lock = !ResearchClientCache.isUnlocked(node.id) || !ResearchClientCache.isUnlocked(cid);
                int color = lock ? GRAY : ACCENT;
                float px = p[0], py = p[1], cx = c[0], cy = c[1];
                // L 形连线（Mindustry View.drawChildren 同款：先横到子 x，再竖到子 y），粗细随整体 scale 喵
                drawThickLine(g, px, py, cx, py, color, 4f);
                drawThickLine(g, cx, py, cx, cy, color, 4f);
            }
        }
    }

    private void drawNode(GuiGraphics g, ResearchNode node) {
        float[] p = pos.get(node.id);
        if (p == null) return;
        int half = (int) (NODE / 2);
        int ix = (int) p[0] - half;
        int iy = (int) p[1] - half;
        int iw = (int) NODE;
        boolean unlocked = ResearchClientCache.isUnlocked(node.id);
        boolean selectable = isSelectable(node);
        boolean researchable = selectable && !unlocked;

        // 按钮底 + 边框（Mindustry button/buttonOver/buttonRed 三态），随整体 scale 喵
        int border = unlocked ? ACCENT : researchable ? LIGHT_GRAY : RED;
        g.fill(ix - 2, iy - 2, ix + iw + 2, iy + iw + 2, border);
        g.fill(ix, iy, ix + iw, iy + iw, NODE_FILL);

        // 内容图标（Mindustry 原版方块贴图，严禁自绘）喵
        int iconBox = ICON_SIZE;
        int cx = ix + (iw - iconBox) / 2;
        int cy = iy + (iw - iconBox) / 2 - 2;
        ResearchIcons.drawNodeIcon(g, node, cx, cy, iconBox);
        // 未解锁压暗（Mindustry：锁定时图标灰/暗，可研究偏亮灰）喵
        if (!unlocked) {
            g.fill(ix, iy, ix + iw, iy + iw, researchable ? 0x2a000000 : 0x66000000);
        }
        // 名称（随整体 scale 缩放）喵
        String n = this.font.plainSubstrByWidth(ResearchClientCache.nameFor(node).getString(), iw);
        g.drawCenteredString(this.font, n, ix + iw / 2, iy + iw - 10, unlocked ? 0xffe8e8e8 : 0xff9a9a9a);
    }

    // —— 信息面板（Mindustry infoTable：跟随节点浮层，非固定右栏）——
    private void drawPanel(GuiGraphics g) {
        if (selected == null) {
            this.researchBtn = null;
            this.panelRect = null;
            return;
        }
        ResearchNode node = selected;
        float[] p = pos.get(node.id);
        if (p == null) {
            this.researchBtn = null;
            this.panelRect = null;
            return;
        }
        boolean unlocked = ResearchClientCache.isUnlocked(node.id);
        boolean researchable = isResearchable(node);

        // 紧凑面板：缩小至少3倍（宽 240→110、大图 64→32、行距收紧）喵
        int big = 32;
        Map<Item, Integer> reqs = ResearchTree.get().effectiveRequirements(node);
        int reqRows = unlocked ? 0 : reqs.size();
        int reqBlock = unlocked ? 10 : 10 + reqRows * 12;
        int panelH = 4 + big + 4 + 10 + 10 + reqBlock + 4 + 3 + 4 + (researchable ? 16 : 12) + 2;

        // 位置：节点右下角浮层，钳制到屏幕内；超出右/下边界则翻转到节点左侧/上方喵
        float nodeSx = offsetX + p[0] * scale;
        float nodeSy = offsetY + p[1] * scale;
        int px = (int) (nodeSx + NODE / 2) + 6;
        int py = (int) (nodeSy + NODE / 2) + 6;
        if (px + PANEL_W > this.width - 4) {
            px = (int) (nodeSx - NODE / 2) - PANEL_W - 6;
        }
        if (py + panelH > this.height - 4) {
            py = Math.max(40, this.height - panelH - 4);
        }
        px = Math.max(4, px);
        py = Math.max(40, py);
        this.panelRect = new int[]{px, py, px + PANEL_W, py + panelH};

        // 面板底 + 边框（状态色）喵
        int border = unlocked ? ACCENT : researchable ? LIGHT_GRAY : RED;
        g.fill(px - 2, py - 2, px + PANEL_W + 2, py + panelH + 2, border);
        g.fill(px, py, px + PANEL_W, py + panelH, PANEL_BG);

        // 小图预览（Mindustry 原版贴图）喵
        int bx = px + (PANEL_W - big) / 2;
        int by = py + 4;
        g.fill(bx - 2, by - 2, bx + big + 2, by + big + 2, 0xff101010);
        ResearchIcons.drawNodeIcon(g, node, bx, by, big);

        // 名称（截断到面板宽）喵
        String nameStr = this.font.plainSubstrByWidth(ResearchClientCache.nameFor(node).getString(), PANEL_W - 6);
        g.drawCenteredString(this.font, nameStr, px + PANEL_W / 2, by + big + 4, 0xFFFFFF);

        // 状态喵
        String state = unlocked ? "已解锁" : researchable ? "可研究" : "未解锁父节点";
        int stateColor = unlocked ? ACCENT : researchable ? LIGHT_GRAY : SCARLET;
        g.drawCenteredString(this.font, state, px + PANEL_W / 2, by + big + 14, stateColor);

        // 需求材料「原版图标 名称 剩余/需求」（剩余 = min(共享池存量, 待投入)）喵
        int ly = by + big + 24;
        if (unlocked) {
            g.drawString(this.font, "已解锁", px + 6, ly, ACCENT);
            ly += 10;
        } else {
            g.drawString(this.font, "材料", px + 6, ly, LIGHT_GRAY);
            ly += 10;
            for (Map.Entry<Item, Integer> e : reqs.entrySet()) {
                Item item = e.getKey();
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                int invested = ResearchClientCache.getProgress(node.id, itemId);
                int need = e.getValue();
                int remaining = need - invested;
                int available = Math.min(ResearchClientCache.getStorage(item), Math.max(remaining, 0));
                boolean met = invested >= need;
                boolean has = available > 0 || met;
                ResearchIcons.drawItemIcon(g, item, px + 6, ly, 12);
                String txt = item.getDescription().getString() + " " + available + "/" + remaining;
                txt = this.font.plainSubstrByWidth(txt, PANEL_W - 26);
                g.drawString(this.font, txt, px + 22, ly + 2, has ? LIGHT_GRAY : SCARLET);
                ly += 12;
            }
        }

        // 进度条（已投入件数/需求件数）喵
        float pct = progressOf(node);
        int barX = px + 6;
        int barY = ly + 2;
        int barW = PANEL_W - 12;
        g.fill(barX, barY, barX + barW, barY + 3, 0xff000000);
        g.fill(barX, barY, barX + (int) (barW * pct), barY + 3, unlocked ? ACCENT : 0xff55ff55);

        // 研究按钮（Mindustry 样式：暗底 + accent 边框 + 白字），消耗队伍共享池喵
        if (researchable) {
            int b0 = px + 6;
            int b1 = py + panelH - 16;
            int b2 = px + PANEL_W - 6;
            int b3 = py + panelH - 4;
            this.researchBtn = new int[]{b0, b1, b2, b3};
            g.fill(b0, b1, b2, b3, NODE_FILL);
            g.fill(b0, b1, b2, b1 + 1, ACCENT);
            g.fill(b0, b1, b0 + 1, b3, ACCENT);
            g.fill(b2 - 1, b1, b2, b3, ACCENT);
            g.fill(b0, b3 - 1, b2, b3, ACCENT);
            g.drawCenteredString(this.font, "研究", (b0 + b2) / 2, b1 + 3, 0xFFFFFF);
        } else {
            this.researchBtn = null;
            g.drawCenteredString(this.font, unlocked ? "已解锁" : "需先研究父节点", px + PANEL_W / 2, py + panelH - 14, unlocked ? ACCENT : GRAY);
        }
    }

    // —— 工具 ——
    private void spend(ResearchNode node) {
        if (node != null && isResearchable(node)) {
            PacketDistributor.sendToServer(new ResearchSpendPayload(node.id.toString()));
        }
    }

    // 可研究：未解锁 + 父已解锁喵
    private boolean isResearchable(ResearchNode node) {
        return isSelectable(node) && !ResearchClientCache.isUnlocked(node.id);
    }

    // 可选（Mindustry selectable = objectives 完成）：父已解锁喵
    private boolean isSelectable(ResearchNode node) {
        ResearchNode p = node.parent();
        return p == null || ResearchClientCache.isUnlocked(p.id);
    }

    private float progressOf(ResearchNode node) {
        int invested = 0;
        int total = 0;
        for (Map.Entry<Item, Integer> e : ResearchTree.get().effectiveRequirements(node).entrySet()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(e.getKey());
            total += e.getValue();
            invested += Math.min(e.getValue(), ResearchClientCache.getProgress(node.id, itemId));
        }
        return total == 0 ? 0f : (float) invested / total;
    }

    private ResearchNode rootNode() {
        List<ResearchNode> roots = ResearchTree.get().roots();
        return roots.isEmpty() ? null : roots.get(0);
    }

    private void fitToScreen() {
        // 迷雾：只按可见节点包围盒适配，避免被隐藏深层节点撑大喵
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        boolean any = false;
        float half = NODE / 2f;
        for (ResearchNode node : nodes) {
            if (!isVisible(node)) continue;
            float[] p = pos.get(node.id);
            if (p == null) continue;
            any = true;
            minX = Math.min(minX, p[0] - half);
            maxX = Math.max(maxX, p[0] + half);
            minY = Math.min(minY, p[1] - half);
            maxY = Math.max(maxY, p[1] + half);
        }
        if (!any) {
            this.scale = 1f;
            return;
        }
        float canvasW = this.width - 20f;
        float canvasH = this.height - 80f;
        float bw = Math.max(1f, maxX - minX);
        float bh = Math.max(1f, maxY - minY);
        this.scale = Math.min(Math.min(canvasW / bw, canvasH / bh), 1f);
        this.offsetX = this.width / 2f - (minX + maxX) / 2f * this.scale;
        this.offsetY = (this.height + 30f) / 2f - (minY + maxY) / 2f * this.scale;
    }

    // 屏幕坐标 → 树坐标找节点（逆变换：tree = (screen - offset) / scale）；迷雾下不可见节点不可选喵
    private ResearchNode findHovered(int mx, int my) {
        float tx = (mx - this.offsetX) / this.scale;
        float ty = (my - this.offsetY) / this.scale;
        int half = (int) (NODE / 2);
        for (ResearchNode node : nodes) {
            if (!isVisible(node)) continue;
            float[] p = pos.get(node.id);
            if (p == null) continue;
            if (tx >= p[0] - half && tx <= p[0] + half && ty >= p[1] - half && ty <= p[1] + half) {
                return node;
            }
        }
        return null;
    }

    // 粗细线段（fill 细矩形模拟 L 形连线），树坐标空间，粗细随整体 scale 喵
    private void drawThickLine(GuiGraphics g, float x0, float y0, float x1, float y1, int color, float thickness) {
        int t = Math.max(1, (int) thickness);
        if (Math.abs(x1 - x0) < 0.01f) {
            g.fill((int) x0 - t / 2, (int) Math.min(y0, y1), (int) x0 - t / 2 + t, (int) Math.max(y0, y1), color);
        } else {
            g.fill((int) Math.min(x0, x1), (int) y0 - t / 2, (int) Math.max(x0, x1), (int) y0 - t / 2 + t, color);
        }
    }
}
