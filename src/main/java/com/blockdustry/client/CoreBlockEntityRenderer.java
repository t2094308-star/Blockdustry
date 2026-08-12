package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.CoreBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

// 核心队伍染色渲染：Mindustry 核心 = base（方块模型，9 格各切 1/3 切片）+ team 层（整张 3×3 贴图乘队伍颜色）喵
// 关键：core_team.png 的透明内容在十字形（N/W/C/E/S），四角 NW/NE/SW/SE 全透明。
// 旧实现只把 UV 裁到左上 1/3（NW 格）——而那正是全透明角，故用户看不到队伍色层喵
// 修复：锚点格是 3×3 的 NW 基准格，在其顶面叠画整张 core_team.png（uv 0..1 覆盖整个 3×3 占地）喵
public class CoreBlockEntityRenderer implements BlockEntityRenderer<CoreBlockEntity> {
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/entity/core_team.png");

    public CoreBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(CoreBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return; // 只在锚点格画一次整张队伍层喵
        int teamColor = be.getTeam().getColor(); // ARGB，忠于原作 Team 构造喵
        int a = (teamColor >> 24) & 0xFF;
        int r = (teamColor >> 16) & 0xFF;
        int g = (teamColor >> 8) & 0xFF;
        int b = teamColor & 0xFF;
        pose.pushPose();
        // 锚点格是 NW 角格，渲染器已平移到该格本地原点；抬到顶面 y+1.001 防 z-fighting 喵
        pose.translate(0f, 1.001f, 0f);
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEX));
        var matrix = pose.last().pose();
        // 3×3 平面（0..3 × 0..3），uv 覆盖整张贴图：四角透明透出 base，十字染上队伍色喵
        // 全亮固定光照，NO_OVERLAY 避免越界采样染黑（参考 CombustionGenerator 渲染器）喵
        vc.addVertex(matrix, 0f, 0f, 0f).setUv(0f, 0f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 0f, 0f, 3f).setUv(0f, 1f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 3f, 0f, 3f).setUv(1f, 1f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 3f, 0f, 0f).setUv(1f, 0f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        pose.popPose();
    }
}
