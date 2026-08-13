package com.blockdustry.client;

import java.util.List;

import com.blockdustry.item.BlockdustryItems;
import com.blockdustry.network.ItemSourceSelectPayload;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;

// 物品源菜单（Mindustry 配置菜单风格）：列出全部赛普罗材料，点击按钮即向服务端发送选中产物喵。
// 两列网格布局，当前产物标记「✓」喵
public class ItemSourceScreen extends Screen {
    private final BlockPos targetPos;
    private final Item current;

    public ItemSourceScreen(BlockPos pos, Item current) {
        super(Component.literal("物品源"));
        this.targetPos = pos;
        this.current = current;
    }

    @Override
    protected void init() {
        List<Item> items = BlockdustryItems.allMaterials();
        int colWidth = 110;
        int gap = 6;
        int cols = 2;
        int x0 = this.width / 2 - (cols * colWidth + (cols - 1) * gap) / 2;
        int y = 40;
        int col = 0;
        for (Item item : items) {
            final Item it = item;
            String label = item.getDescription().getString();
            if (item == current) label += " ✓";
            final String id = BuiltInRegistries.ITEM.getKey(it).toString();
            this.addRenderableWidget(Button.builder(Component.literal(label), btn ->
                            PacketDistributor.sendToServer(new ItemSourceSelectPayload(targetPos, id)))
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
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
        String cur = current != null ? current.getDescription().getString() : "煤";
        guiGraphics.drawCenteredString(this.font,
                Component.literal("选择要产出的材料（当前: " + cur + "）"),
                this.width / 2, 28, 0xAAAAAA);
    }
}
