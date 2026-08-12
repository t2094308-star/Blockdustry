package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.TurretBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

// duo 炮塔动画渲染（Mindustry DrawTurret）：基座沿用方块模型，BER 叠画转盘 + 双炮管，
// 随 aimYaw 旋转、后坐力位移（转盘整体 + 每管独立），忠于 Mindustry 参数喵
public class TurretBlockEntityRenderer implements BlockEntityRenderer<TurretBlockEntity> {
    private static final ResourceLocation TEX_TOP =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/entity/turret_top.png");
    private static final ResourceLocation TEX_BARREL_L =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/entity/turret_barrel_l.png");
    private static final ResourceLocation TEX_BARREL_R =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/entity/turret_barrel_r.png");

    public TurretBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(TurretBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return;
        // 后坐力本地衰减：从收到同步的时刻起每 tick 减 1/20（recoilTime=reload=20）喵
        float elapsed = (be.getLevel().getGameTime() + partialTick - be.lastSyncTick());
        float top = Math.max(0, be.getRecoilTop() - elapsed / 20f);
        float rl = Math.max(0, be.getRecoilL() - elapsed / 20f);
        float rr = Math.max(0, be.getRecoilR() - elapsed / 20f);
        float yaw = be.getAimYaw();

        // 移到 1×1 块顶面，先整体绕 Y 旋转（贴图"前"方向以 duo 实图为准，必要时 ±180 校准）喵
        pose.pushPose();
        pose.translate(0.5f, 1.02f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));

        // 转盘本体：整体后坐力沿炮管反方向（局部 +Z）平移：pow(recoil,1.8)*0.5px/8 喵
        float topBack = (float) Math.pow(top, 1.8f) * 0.5f / 8f;
        pose.pushPose();
        pose.translate(0, 0, topBack);
        drawQuad(pose, buffer, TEX_TOP, light, overlay);
        pose.popPose();

        // 双炮管：整体后坐力基础上再各自加 moveY=-1.5px 的管后坐力喵
        drawBarrel(pose, buffer, TEX_BARREL_L, topBack + 1.5f * rl / 8f, light, overlay);
        drawBarrel(pose, buffer, TEX_BARREL_R, topBack + 1.5f * rr / 8f, light, overlay);
        pose.popPose();
    }

    // 平面 quad 铺满 1 格（-0.5..0.5），uv 覆盖整张贴图，法线朝上（entityCutout 透明底正常）喵
    private void drawQuad(PoseStack pose, MultiBufferSource buffer, ResourceLocation tex,
                          int light, int overlay) {
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(tex));
        var matrix = pose.last().pose();
        vc.addVertex(matrix, -0.5f, 0f, -0.5f).setUv(0f, 0f).setColor(255, 255, 255, 255).setLight(light).setOverlay(overlay).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, -0.5f, 0f, 0.5f).setUv(0f, 1f).setColor(255, 255, 255, 255).setLight(light).setOverlay(overlay).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 0.5f, 0f, 0.5f).setUv(1f, 1f).setColor(255, 255, 255, 255).setLight(light).setOverlay(overlay).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 0.5f, 0f, -0.5f).setUv(1f, 0f).setColor(255, 255, 255, 255).setLight(light).setOverlay(overlay).setNormal(0f, 1f, 0f);
    }

    private void drawBarrel(PoseStack pose, MultiBufferSource buffer, ResourceLocation tex,
                            float back, int light, int overlay) {
        pose.pushPose();
        pose.translate(0, 0, back);
        drawQuad(pose, buffer, tex, light, overlay);
        pose.popPose();
    }
}
