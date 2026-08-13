package com.blockdustry.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.blockdustry.research.ResearchNode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

// 科技树图标资源工具：加载 Mindustry 原版 PNG 贴图（已复制到 assets/blockdustry/textures/research/），
// 解析尺寸并按比例绘制——严禁自绘模型/贴图，全部用 Mindustry 原版喵
public final class ResearchIcons {
    private ResearchIcons() {}

    private static final Map<ResourceLocation, int[]> SIZES = new HashMap<>();

    // 节点 → Mindustry 原版方块贴图（elevator 无 Mindustry 等价 → null 走 MC 物品图标兜底）喵
    public static ResourceLocation nodeTexture(ResearchNode node) {
        String base = "blockdustry:textures/research/blocks/";
        return switch (node.id.getPath()) {
            case "core" -> ResourceLocation.tryParse(base + "core.png");
            case "conveyor" -> ResourceLocation.tryParse(base + "conveyor.png");
            case "router" -> ResourceLocation.tryParse(base + "router.png");
            case "drill" -> ResourceLocation.tryParse(base + "drill.png");
            case "power_node" -> ResourceLocation.tryParse(base + "power_node.png");
            case "combustion_generator" -> ResourceLocation.tryParse(base + "combustion_generator.png");
            case "graphite_press" -> ResourceLocation.tryParse(base + "graphite_press.png");
            case "battery" -> ResourceLocation.tryParse(base + "battery.png");
            case "turret" -> ResourceLocation.tryParse(base + "duo.png");
            case "scatter" -> ResourceLocation.tryParse(base + "scatter.png");
            case "fuse" -> ResourceLocation.tryParse(base + "fuse.png");
            case "arc" -> ResourceLocation.tryParse(base + "arc.png");
            case "unit_factory" -> ResourceLocation.tryParse(base + "ground_factory.png");
            case "power_source" -> ResourceLocation.tryParse(base + "power_source.png");
            case "item_source" -> ResourceLocation.tryParse(base + "item_source.png");
            default -> null;
        };
    }

    // drill 顶面叠层（Mindustry mechanical-drill-top，叠加到 base 上组成完整钻头图标）喵
    public static ResourceLocation drillTopTexture() {
        return ResourceLocation.tryParse("blockdustry:textures/research/blocks/drill_top.png");
    }

    // 材料 → Mindustry 原版物品贴图（非 Blockdustry 材料 → null 走 MC 物品图标兜底）喵
    public static ResourceLocation itemTexture(Item item) {
        if (item == null || item == Items.AIR) return null;
        ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
        String path = id.getPath();
        return switch (path) {
            case "copper_ingot" -> ResourceLocation.tryParse("blockdustry:textures/research/items/copper.png");
            case "lead" -> ResourceLocation.tryParse("blockdustry:textures/research/items/lead.png");
            case "graphite" -> ResourceLocation.tryParse("blockdustry:textures/research/items/graphite.png");
            case "silicon" -> ResourceLocation.tryParse("blockdustry:textures/research/items/silicon.png");
            default -> null;
        };
    }

    // 在 boxW×boxH 内按比例缩放居中绘制贴图（MC blit 无独立缩放，用 PoseStack 矩阵实现）喵
    public static void drawScaled(GuiGraphics g, ResourceLocation tex, int x, int y, int boxW, int boxH) {
        if (tex == null) return;
        int[] size = sizeOf(tex);
        if (size == null) return;
        float scale = Math.min((float) boxW / size[0], (float) boxH / size[1]);
        float w = size[0] * scale;
        float h = size[1] * scale;
        float dx = x + (boxW - w) / 2f;
        float dy = y + (boxH - h) / 2f;
        g.pose().pushPose();
        g.pose().translate(dx, dy, 0);
        g.pose().scale(scale, scale, 1f);
        g.blit(tex, 0, 0, 0, 0f, 0f, size[0], size[1], size[0], size[1]);
        g.pose().popPose();
    }

    // 节点图标：优先 Mindustry 方块贴图（drill 叠顶面）；无则 MC 物品图标兜底喵
    public static void drawNodeIcon(GuiGraphics g, ResearchNode node, int x, int y, int box) {
        ResourceLocation tex = nodeTexture(node);
        if (tex == null) {
            var stack = ResearchClientCache.iconFor(node);
            int s = Math.min(box, 16);
            g.renderItem(stack, x + (box - s) / 2, y + (box - s) / 2);
            return;
        }
        drawScaled(g, tex, x, y, box, box);
        if (node.id.getPath().equals("drill")) {
            ResourceLocation top = drillTopTexture();
            if (top != null) drawScaled(g, top, x, y, box, box);
        }
    }

    // 材料图标：优先 Mindustry 物品贴图；无则 MC 物品图标兜底喵
    public static void drawItemIcon(GuiGraphics g, Item item, int x, int y, int box) {
        ResourceLocation tex = itemTexture(item);
        if (tex == null) {
            g.renderItem(new net.minecraft.world.item.ItemStack(item), x, y);
            return;
        }
        drawScaled(g, tex, x, y, box, box);
    }

    // 读取 PNG 头解析宽高并缓存（字节 16-19 宽、20-23 高，均大端）喵
    public static synchronized int[] sizeOf(ResourceLocation loc) {
        int[] cached = SIZES.get(loc);
        if (cached != null) return cached;
        try (InputStream in = Minecraft.getInstance().getResourceManager().getResource(loc)
                .map(r -> {
                    try {
                        return r.open();
                    } catch (IOException e) {
                        return null;
                    }
                }).orElse(null)) {
            if (in == null) return null;
            byte[] hdr = new byte[24];
            int read = 0;
            while (read < 24) {
                int r = in.read(hdr, read, 24 - read);
                if (r < 0) break;
                read += r;
            }
            if (read < 24) return null;
            int w = ((hdr[16] & 0xFF) << 24) | ((hdr[17] & 0xFF) << 16) | ((hdr[18] & 0xFF) << 8) | (hdr[19] & 0xFF);
            int h = ((hdr[20] & 0xFF) << 24) | ((hdr[21] & 0xFF) << 16) | ((hdr[22] & 0xFF) << 8) | (hdr[23] & 0xFF);
            if (w <= 0 || h <= 0) return null;
            int[] result = {w, h};
            SIZES.put(loc, result);
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
