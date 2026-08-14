package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.FuseBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

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

import java.util.Random;

// 熔毁炮（fuse）动画渲染（Mindustry DrawTurret）：3×3 建筑，BER 叠画单一转盘 fuse_top，
// 随 aimYaw 旋转、整体后坐力位移 pow(recoil,1.8)*5/8（原版 recoil=5 单位=0.625 格）；
// 热区 glow（fuse-heat.png 覆在转盘上，色乘 ab3400、alpha=heat、1/20 衰减）；
// 炮口闪光（Fx.lightningShoot 等效：7 根白→a9d8ff 短辐射线、±50° 锥、12 tick）喵
public class FuseBlockEntityRenderer implements BlockEntityRenderer<FuseBlockEntity> {
    private static final ResourceLocation TEX_TOP =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/entity/fuse_top.png");
    private static final ResourceLocation TEX_HEAT =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/entity/fuse_heat.png");
    private static final ResourceLocation WHITE_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/misc/white.png");

    // 3×3 占地：转盘 quad 半宽（以建筑中心为原点的 -1.5..1.5）喵
    private static final float HALF = 1.5f;
    // Mindustry Pal.turretHeat = #ab3400（暗橙红）喵
    private static final int HEAT_R = 0xab, HEAT_G = 0x34, HEAT_B = 0x00;
    // Pal.lancerLaser = #a9d8ff（炮口闪光终点色）喵
    private static final int FLASH_TO_R = 0xa9, FLASH_TO_G = 0xd8, FLASH_TO_B = 0xff;
    // Fx.lightningShoot 炮口闪光寿命 12 tick；shootY=0.5 与 fireAt 弹丸出生点一致喵
    private static final float FLASH_LIFE = 12f;

    public FuseBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(FuseBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return;
        // 后坐力本地衰减：从收到同步的时刻起每 tick 减 1/reload（recoilTime=reload）；热区 1/20；闪光 1 tick 喵
        float elapsed = (be.getLevel().getGameTime() + partialTick - be.lastSyncTick());
        float top = Math.max(0, be.getRecoilTop() - elapsed / be.getReload());
        float heat = Math.max(0, be.getHeat() - elapsed / 20f);
        int muzzle = Math.max(0, be.getMuzzleFlashTicks() - (int) elapsed);
        float yaw = be.getAimYaw();

        // 炮口闪光：world 空间画（独立于转盘旋转帧），7 根白→浅蓝短辐射线、±50° 锥、12 tick 喵
        if (muzzle > 0) {
            pose.pushPose();
            pose.setIdentity();
            drawMuzzleFlash(be, pose, buffer, muzzle);
            pose.popPose();
        }

        // 移到 3×3 建筑顶面中心（锚点在 NW 角，中心为 +1,+1），先整体绕 Y 旋转（贴图"前"方向校准同 duo）喵
        pose.pushPose();
        pose.translate(1.0f, 1.02f, 1.0f);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));

        // 转盘本体：整体后坐力沿炮管反方向（局部 +Z）平移：pow(recoil,1.8)*5/8（原版 5 单位=0.625 格）喵
        float topBack = (float) Math.pow(top, 1.8f) * 5f / 8f;
        pose.pushPose();
        pose.translate(0, 0.008f, topBack);
        drawQuad(pose, buffer, TEX_TOP, light, overlay);
        // 热区 glow：fuse_heat.png 覆在转盘上，色乘 ab3400、alpha=heat（additive 用 entityTranslucent+全亮近似）喵
        if (heat > 0.01f) {
            drawHeat(pose, buffer, heat);
        }
        pose.popPose();
        pose.popPose();
    }

    // 炮口闪光（Fx.lightningShoot 等效）：7 根白→浅蓝短辐射线，±50° 锥，12 tick 淡出喵
    private void drawMuzzleFlash(FuseBlockEntity be, PoseStack pose, MultiBufferSource buffer, int muzzle) {
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        BlockPos anchor = be.hasAnchor() ? be.getAnchor() : be.getBlockPos();
        Vec3 center = anchor.getCenter().add(1, 0, 1).subtract(cam);
        double yawRad = Math.toRadians(be.getAimYaw());
        Vec3 aim = new Vec3(-Math.sin(yawRad), 0, -Math.cos(yawRad));
        // 炮口 = 建筑中心 + aim*shootY + (0,0.5,0)，与 fireAt 弹丸出生点一致喵
        Vec3 muzzlePos = center.add(aim.scale(0.5f)).add(0, 0.5, 0);

        float fin = 1f - muzzle / FLASH_LIFE;
        float fout = muzzle / FLASH_LIFE;
        int cr = (int) (255 + (FLASH_TO_R - 255) * fin);
        int cg = (int) (255 + (FLASH_TO_G - 255) * fin);
        int cb = (int) (255 + (FLASH_TO_B - 255) * fin);

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
        Matrix4f matrix = pose.last().pose();
        // 固定种子：跨帧稳定，fin 只缩放长度/偏移，不闪喵
        Random rng = new Random(be.getBlockPos().hashCode() * 31L + 7L);
        for (int i = 0; i < 7; i++) {
            float ang = (rng.nextFloat() * 100f - 50f) * (float) Math.PI / 180f; // ±50° 喵
            float cos = (float) Math.cos(ang), sin = (float) Math.sin(ang);
            // 绕 Y 旋转 aim：x' = x*cos + z*sin，z' = -x*sin + z*cos 喵
            Vec3 dir = new Vec3(aim.x * cos + aim.z * sin, 0, -aim.x * sin + aim.z * cos);
            float startOff = rng.nextFloat() * 3.125f * fin * fin; // 25 单位=3.125 格 * finpow 喵
            float len = 0.25f + 0.625f * fin; // 2→7 单位喵
            Vec3 a = muzzlePos.add(dir.scale(startOff));
            Vec3 b = a.add(dir.scale(len));
            Vec3 mid = a.add(b).scale(0.5f);
            Vec3 view = mid.scale(-1); // 相对坐标已含 cam 偏移：中点→相机喵
            if (view.lengthSqr() < 1e-6) view = new Vec3(0, 0, 1); else view = view.normalize();
            Vec3 n = dir.cross(view);
            if (n.lengthSqr() < 1e-6) n = new Vec3(1, 0, 0); else n = n.normalize();
            // 线宽 stroke = fout*1.2+0.5 单位 → 半宽 (0.5+1.2*fout)/8/2 格喵
            beam(vc, matrix, a, b, n, 0.03125f + 0.075f * fout, cr, cg, cb, (int) (255 * fout));
        }
    }

    // 平面 quad 铺满 3×3（-1.5..1.5，半宽 HALF），uv 覆盖整张贴图，法线朝上（entityCutout 透明底正常）。
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

    // 热区 glow：fuse_heat.png 覆在转盘上（y 略高防共面），色乘 ab3400、alpha=heat，entityTranslucent+全亮近似 additive 喵
    private void drawHeat(PoseStack pose, MultiBufferSource buffer, float heat) {
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEX_HEAT));
        var matrix = pose.last().pose();
        int a = (int) (heat * 255);
        vc.addVertex(matrix, -HALF, 0.004f, -HALF).setUv(0f, 0f).setColor(HEAT_R, HEAT_G, HEAT_B, a).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, -HALF, 0.004f, HALF).setUv(0f, 1f).setColor(HEAT_R, HEAT_G, HEAT_B, a).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, HALF, 0.004f, HALF).setUv(1f, 1f).setColor(HEAT_R, HEAT_G, HEAT_B, a).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, HALF, 0.004f, -HALF).setUv(1f, 0f).setColor(HEAT_R, HEAT_G, HEAT_B, a).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
    }

    // 沿 a→b 的细长矩形（面法向 n、半宽 hw），四顶点无 cull（billboard 短辐射线）喵
    private static void beam(VertexConsumer vc, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 n,
                             float hw, int cr, int cg, int cb, int alpha) {
        Vec3 a0 = a.add(n.scale(-hw)), a1 = a.add(n.scale(hw));
        Vec3 b0 = b.add(n.scale(-hw)), b1 = b.add(n.scale(hw));
        vertex(vc, matrix, (float) a0.x, (float) a0.y, (float) a0.z, cr, cg, cb, alpha);
        vertex(vc, matrix, (float) b0.x, (float) b0.y, (float) b0.z, cr, cg, cb, alpha);
        vertex(vc, matrix, (float) b1.x, (float) b1.y, (float) b1.z, cr, cg, cb, alpha);
        vertex(vc, matrix, (float) a1.x, (float) a1.y, (float) a1.z, cr, cg, cb, alpha);
    }

    private static void vertex(VertexConsumer vc, Matrix4f matrix, float x, float y, float z,
                               int cr, int cg, int cb, int alpha) {
        vc.addVertex(matrix, x, y, z)
                .setColor(cr, cg, cb, alpha)
                .setUv(0f, 0f)
                .setOverlay(OverlayTexture.NO_OVERLAY) // 0xFFFFFF 越界采样染黑，必须 NO_OVERLAY 喵
                .setLight(0xF000F0) // 全亮，不随环境光照变暗喵
                .setNormal(0f, 1f, 0f);
    }
}
