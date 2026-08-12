package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.BlockdustryBlocks;
import com.blockdustry.network.QueryCoreStoragePayload;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

// 顶部中央资源栏（忠于 Mindustry 资源栏）：黑 60% 背景、每行 4 项「图标+数量」并排、
// 数量左对齐 minWidth、仅显示非零项、≥1000 用 K/M 缩写喵
@EventBusSubscriber(modid = Blockdustry.MODID, value = Dist.CLIENT)
public class CoreHudHandler {
    private static int coalCount;
    private static int graphiteCount;
    private static long lastQueryTick = 0;

    // 服务端返回的白名单物资数量更新喵
    public static void setCoreStorage(int coal, int graphite) {
        coalCount = coal;
        graphiteCount = graphite;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.font == null) return;
        // 每 20 tick（1 秒）向服务端查询一次喵
        long now = mc.level.getGameTime();
        if (now - lastQueryTick >= 20) {
            lastQueryTick = now;
            PacketDistributor.sendToServer(new QueryCoreStoragePayload());
        }
        GuiGraphics gui = event.getGuiGraphics();
        int screenW = mc.getWindow().getGuiScaledWidth();
        // 每行 4 项，每项占宽（图标 16 + 数量区）喵
        int itemW = 56;
        int itemsPerRow = 4;
        int rowH = 20;
        // 只显示非零项（Mindustry 资源栏仅非零条目）喵
        java.util.List<int[]> entries = new java.util.ArrayList<>();
        if (coalCount > 0) entries.add(new int[]{0, coalCount});
        if (graphiteCount > 0) entries.add(new int[]{1, graphiteCount});
        if (entries.isEmpty()) return;
        // 右上角布局，按行换行喵
        int rows = (entries.size() + itemsPerRow - 1) / itemsPerRow;
        int cols = Math.min(itemsPerRow, entries.size());
        int panelW = cols * itemW;
        int panelH = rows * rowH;
        // 右上角布局：贴右留 4px 边距，避开 Jade 默认 tooltip（鼠标处屏幕中上偏左）喵
        int x0 = screenW - panelW - 4;
        int y0 = 4;
        gui.fill(x0 - 2, y0 - 2, x0 + panelW + 2, y0 + panelH + 2, 0x99000000); // 黑 60% 背景喵
        for (int i = 0; i < entries.size(); i++) {
            int col = i % itemsPerRow;
            int row = i / itemsPerRow;
            int x = x0 + col * itemW;
            int y = y0 + row * rowH;
            if (entries.get(i)[0] == 0) {
                drawResource(gui, mc, x, y, new ItemStack(Items.COAL), entries.get(i)[1]);
            } else {
                drawResource(gui, mc, x, y, new ItemStack(BlockdustryBlocks.GRAPHITE.get()), entries.get(i)[1]);
            }
        }
    }

    // 每项：物品图标 + 数量（左对齐，≥1000 用 K/M 缩写）喵
    private static void drawResource(GuiGraphics gui, Minecraft mc, int x, int y, ItemStack stack, int count) {
        gui.renderItem(stack, x, y);
        gui.drawString(mc.font, fmt(count), x + 18, y + 4, 0xFFFFFF);
    }

    // Mindustry 资源数量缩写：≥1000 K、≥1000000 M 喵
    private static String fmt(int n) {
        if (n >= 1000000) return String.format("%.1fM", n / 1000000f);
        if (n >= 1000) return String.format("%.1fK", n / 1000f);
        return String.valueOf(n);
    }
}
