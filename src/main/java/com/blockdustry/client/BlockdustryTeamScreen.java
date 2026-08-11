package com.blockdustry.client;

import com.blockdustry.network.SetTeamPayload;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

// 队伍调试 UI：显示目标与当前队伍，点击按钮设置队伍喵
public class BlockdustryTeamScreen extends Screen {
    private final BlockPos targetPos;
    private final int targetEntityId;
    private final String targetName;
    private String currentTeam = "查询中...";

    public BlockdustryTeamScreen(BlockPos pos, int entityId, String targetName) {
        super(Component.literal("队伍调试"));
        this.targetPos = pos;
        this.targetEntityId = entityId;
        this.targetName = targetName;
    }

    @Override
    protected void init() {
        int y = 68;
        for (BlockdustryTeam team : BlockdustryTeam.values()) {
            final BlockdustryTeam t = team;
            this.addRenderableWidget(Button.builder(Component.literal(team.name()), btn ->
                            PacketDistributor.sendToServer(new SetTeamPayload(targetPos, targetEntityId, t.name())))
                    .bounds(this.width / 2 - 80, y, 160, 20)
                    .build());
            y += 24;
        }
    }

    // 服务端返回的队伍更新显示喵
    public void updateTeam(String team) {
        this.currentTeam = team;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        // 文字最后画，确保在模糊背景之上清晰显示喵
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, Component.literal("目标: " + targetName), this.width / 2, 38, 0xAAAAAA);
        guiGraphics.drawCenteredString(this.font, Component.literal("当前队伍: " + currentTeam), this.width / 2, 50, 0xFFFF55);
    }
}
