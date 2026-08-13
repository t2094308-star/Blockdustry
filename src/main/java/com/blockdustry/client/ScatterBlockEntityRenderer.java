package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.ScatterBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

// 分裂炮（Scatter）动画渲染（Mindustry DrawTurret）：基座沿用 2×2 方块模型（四象限跨格），
// BER 叠画单一转盘 scatter-mid，随 aimYaw 旋转、整体后坐力位移；贴图用 scatter 专属 -mid，
// 不再复用 duo 的 turret_top 双管转盘喵
public class ScatterBlockEntityRenderer implements BlockEntityRenderer<ScatterBlockEntity> {
    // scatter 专属旋转部件（Mindustry scatter-mid.png，单管转盘）喵
    private static final ResourceLocation TEX_TOP =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/entity/scatter_top.png");
    // 2×2 占地：转盘 quad 半宽（以建筑中心为原点的 -1..1）喵
    private static final float HALF = 1f;

    public ScatterBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(ScatterBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return;
        // 后坐力本地衰减：从收到同步的时刻起每 tick 减 1/reload（recoilTime=reload）喵
        float elapsed = (be.getLevel().getGameTime() + partialTick - be.lastSyncTick());
        float top = Math.max(0, be.getRecoilTop() - elapsed / be.getReload());
        float yaw = be.getAimYaw();

        // 移到 2×2 建筑顶面中心（锚点在 NW 角，中心为 +1,+1），先整体绕 Y 旋转（贴图"前"方向校准同 duo）喵
        pose.pushPose();
        pose.translate(1.0f, 1.02f, 1.0f);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));

        // 转盘本体：整体后坐力沿炮管反方向（局部 +Z）平移：pow(recoil,1.8)*0.5px/8 喵
        float topBack = (float) Math.pow(top, 1.8f) * 0.5f / 8f;
        pose.pushPose();
        pose.translate(0, 0.008f, topBack);
        drawQuad(pose, buffer, TEX_TOP, light, overlay);
        pose.popPose();
        pose.popPose();
    }

    // 平面 quad 铺满 2×2（-1..1，半宽 HALF），uv 覆盖整张贴图，法线朝上（entityCutout 透明底正常）。
    // ⚠️ 必须全亮 setLight(FULL_BRIGHT) + NO_OVERLAY：透传 BER 的 light 在白天 blockLight=0 时极暗（研究-炮管黑.md）喵
    private void drawQuad(PoseStack pose, MultiBufferSource buffer, ResourceLocation tex,
                          int light, int overlay) {
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(tex));
        var matrix = pose.last().pose();
        vc.addVertex(matrix, -HALF, 0f, -HALF).setUv(0f, 0f).setColor(255, 255, 255, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, -HALF, 0f, HALF).setUv(0f, 1f).setColor(255, 255, 255, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, HALF, 0f, HALF).setUv(1f, 1f).setColor(255, 255, 255, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, HALF, 0f, -HALF).setUv(1f, 0f).setColor(255, 255, 255, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
    }
}
