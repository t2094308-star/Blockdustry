package com.blockdustry.client;

import java.util.List;

import com.blockdustry.item.BlockdustryItems;
import com.blockdustry.network.SorterSelectPayload;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;

// 分拣器配置菜单（Mindustry Sorter buildConfiguration 风格）：列出全部赛普罗材料，点击即向服务端发送设定物品喵。
// 顶部「清除」按钮对应 Mindustry clearOnDoubleTap（configClear → sortItem=null）；当前设定物品标记「✓」喵
public class SorterScreen extends Screen {
    private final BlockPos targetPos;
    private final Item current;

    public SorterScreen(BlockPos pos, Item current) {
        super(Component.literal("分拣器"));
        this.targetPos = pos;
        this.current = current;
    }

    @Override
    protected void init() {
        // 清除按钮：清空设定（Mindustry configClear）喵
        this.addRenderableWidget(Button.builder(Component.literal("清除（不设定）"), btn ->
                        PacketDistributor.sendToServer(new SorterSelectPayload(targetPos, "")))
                .bounds(this.width / 2 - 55, 36, 110, 20)
                .build());

        List<Item> items = BlockdustryItems.allMaterials();
        int colWidth = 110;
        int gap = 6;
        int cols = 2;
        int x0 = this.width / 2 - (cols * colWidth + (cols - 1) * gap) / 2;
        int y = 64;
        int col = 0;
        for (Item item : items) {
            final Item it = item;
            String label = item.getDescription().getString();
            if (item == current) label += " ✓";
            final String id = BuiltInRegistries.ITEM.getKey(it).toString();
            this.addRenderableWidget(Button.builder(Component.literal(label), btn ->
                            PacketDistributor.sendToServer(new SorterSelectPayload(targetPos, id)))
                    .bounds(x0 + col * (colWidth + gap), y, colWidth, 20)
                    .build());
            col++;
            if (col >= cols) {
                col = 0;
                y += 24;
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFF);
        String cur = current != null ? current.getDescription().getString() : "未设定";
        guiGraphics.drawCenteredString(this.font,
                Component.literal("选择要直通/反转的物品（当前: " + cur + "）"),
                this.width / 2, 26, 0xAAAAAA);
    }
}
