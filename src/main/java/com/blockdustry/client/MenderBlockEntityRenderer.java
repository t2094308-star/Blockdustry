package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.MenderBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import java.util.List;

// 修理器（Mindustry mender）动画渲染，忠实 MendProjector.draw() + Fx.healBlockFull：
// 1) topRegion 呼吸：mender_top 贴图染 baseColor(#84f491)，alpha=heat*absin(t,31.8,1)*0.5 喵
// 2) 旋转方框脉冲：原版 Lines.square(x,y,min(1+(1-f)*size*ts/2, size*ts/2))，f=1-(time/100)%1；
//    3D 用 45° 菱形细条线框，半径从 0.125 格脉冲到 0.5 格（1 格=8 Mindustry 单位）喵
// 3) 维修闪烁（healBlockFull 等效）：目标块顶面 quad 染 baseColor，alpha=fout 淡出（20 tick）喵
public class MenderBlockEntityRenderer implements BlockEntityRenderer<MenderBlockEntity> {
    private static final ResourceLocation TEX_TOP =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/entity/mender_top.png");
    private static final ResourceLocation WHITE_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/misc/white.png");
    // Mindustry MendProjector baseColor = #84f491 喵
    private static final int BASE_R = 0x84, BASE_G = 0xf4, BASE_B = 0x91;
    // 呼吸周期：Mathf.absin(time, 50/PI2, 1) 喵
    private static final float BREATH_PERIOD = 50f / (float) Math.PI / 2f;

    public MenderBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(MenderBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return;
        long gameTime = be.getLevel().getGameTime();
        float t = gameTime + partialTick;
        float heat = be.getHeat();
        // 阶段 1：顶部呼吸 quad（block-local）喵
        if (heat > 0.01f) {
            drawBreathingTop(be, t, heat, pose, buffer);
        }
        // 阶段 2：旋转方框脉冲（block-local）喵
        if (heat > 0.01f) {
            drawPulseFrame(be, t, heat, pose, buffer);
        }
        // 阶段 3：维修闪烁（世界坐标，目标块）喵
        drawRepairFlash(be, t, partialTick, pose, buffer);
    }

    // 顶部呼吸：mender_top 贴图铺满 1×1，alpha=heat*absin*0.5，染 baseColor 喵
    private void drawBreathingTop(MenderBlockEntity be, float t, float heat,
                                  PoseStack pose, MultiBufferSource buffer) {
        float absin = Math.abs((float) Math.sin(t * (Math.PI * 2f / BREATH_PERIOD)));
        float alpha = heat * absin * 0.5f;
        if (alpha < 0.01f) return;
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEX_TOP));
        Matrix4f matrix = pose.last().pose();
        float y = 1.001f;
        int a = (int) (255 * alpha);
        vc.addVertex(matrix, 0f, y, 0f).setUv(0f, 0f).setColor(BASE_R, BASE_G, BASE_B, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 0f, y, 1f).setUv(0f, 1f).setColor(BASE_R, BASE_G, BASE_B, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 1f, y, 1f).setUv(1f, 1f).setColor(BASE_R, BASE_G, BASE_B, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 1f, y, 0f).setUv(1f, 0f).setColor(BASE_R, BASE_G, BASE_B, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
    }

    // 旋转方框脉冲：45° 菱形细条线框（原版 Lines.square 旋转 45°）喵
    private void drawPulseFrame(MenderBlockEntity be, float t, float heat,
                                PoseStack pose, MultiBufferSource buffer) {
        float f = 1f - (t / 100f) % 1f;
        // 原版 radius = min(1 + (1-f)*size*ts/2, size*ts/2)，单位像素；1 格 = 8 单位 → 格喵
        float radius = Math.min(0.125f + (1f - f) * 0.5f, 0.5f);
        if (radius <= 0.01f) return;
        // stroke = (2f*f + 0.2f)*heat（原版像素），3D 加粗为 0.04 格细条喵
        float stroke = Math.max(0.02f, 0.04f * heat);
        float cx = 0.5f, cy = 1.1f, cz = 0.5f;
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
        Matrix4f matrix = pose.last().pose();
        // 菱形顶点（45° 旋转：角在轴向）喵
        float d = radius;
        drawEdge(vc, matrix, cx, cz + d, cx + d, cz, cy, stroke, heat);
        drawEdge(vc, matrix, cx + d, cz, cx, cz - d, cy, stroke, heat);
        drawEdge(vc, matrix, cx, cz - d, cx - d, cz, cy, stroke, heat);
        drawEdge(vc, matrix, cx - d, cz, cx, cz + d, cy, stroke, heat);
    }

    // 水平细条（block-local）：从 (x1,z1) 到 (x2,z2) 的矩形线，y 高度、厚度 stroke 格，染 baseColor 喵
    private void drawEdge(VertexConsumer vc, Matrix4f matrix, float x1, float z1,
                          float x2, float z2, float y, float stroke, float heat) {
        float dx = x2 - x1, dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dz * dz);
        if (len < 1e-4f) return;
        float nx = -dz / len, nz = dx / len; // 法向
        float hx = nx * stroke / 2f, hz = nz * stroke / 2f;
        int a = (int) (255 * heat);
        vc.addVertex(matrix, x1 - hx, y, z1 - hz).setUv(0f, 0f).setColor(BASE_R, BASE_G, BASE_B, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, x1 + hx, y, z1 + hz).setUv(0f, 1f).setColor(BASE_R, BASE_G, BASE_B, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, x2 + hx, y, z2 + hz).setUv(1f, 1f).setColor(BASE_R, BASE_G, BASE_B, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, x2 - hx, y, z2 - hz).setUv(1f, 0f).setColor(BASE_R, BASE_G, BASE_B, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
    }

    // 维修闪烁（healBlockFull 等效）：目标块顶面 quad 染 baseColor，alpha=fout，20 tick 淡出喵
    private void drawRepairFlash(MenderBlockEntity be, float t, float partialTick,
                                 PoseStack pose, MultiBufferSource buffer) {
        long start = be.getRepairStartGameTime();
        if (start < 0) return;
        float elapsed = t - start;
        if (elapsed < 0f || elapsed > be.getRepairEffectLife()) return;
        float fout = 1f - elapsed / be.getRepairEffectLife();
        float alpha = fout * 0.6f;
        if (alpha < 0.01f) return;
        List<BlockPos> targets = be.getRepairTargets();
        if (targets.isEmpty()) return;
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        pose.pushPose();
        pose.setIdentity();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
        Matrix4f matrix = pose.last().pose();
        int a = (int) (255 * alpha);
        for (BlockPos p : targets) {
            double x0 = p.getX() - cam.x, z0 = p.getZ() - cam.z;
            double y0 = p.getY() + 1.01 - cam.y;
            vc.addVertex(matrix, (float) x0, (float) y0, (float) z0).setUv(0f, 0f).setColor(BASE_R, BASE_G, BASE_B, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
            vc.addVertex(matrix, (float) x0, (float) y0, (float) (z0 + 1)).setUv(0f, 1f).setColor(BASE_R, BASE_G, BASE_B, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
            vc.addVertex(matrix, (float) (x0 + 1), (float) y0, (float) (z0 + 1)).setUv(1f, 1f).setColor(BASE_R, BASE_G, BASE_B, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
            vc.addVertex(matrix, (float) (x0 + 1), (float) y0, (float) z0).setUv(1f, 0f).setColor(BASE_R, BASE_G, BASE_B, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        }
        pose.popPose();
    }
}
