package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.ForceProjectorBlockEntity;
import com.blockdustry.building.ForceProjectorBlockEntity.AbsorbPoint;
import com.blockdustry.team.BlockdustryTeam;

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

// 力墙投影（Mindustry force-projector）护盾渲染，忠实 ForceProjector.draw()/drawShield() + Fx.shieldBreak/Fx.absorb：
// 1) 蓄力 top 加色：force-projector-top 贴图铺 3×3，alpha=buildup/shieldHealth×0.75（原版 additive 混色，3D 用 translucent 近似）喵
// 2) 护盾六边形：实心盘 alpha≈0.09（原版 !animateShields 分支）+ 线框 alpha 1；颜色 team.color→white 按 hit 闪白；
//    3D 画水平六边形（x-z 平面），中心=建筑中心，半径=realRadius() 格，顶点从角度 0 起（与 insideHex 同构）喵
// 3) 破碎动画（Fx.shieldBreak 40 tick）：六边形线框从 breakRadius 扩散 + fout 淡出喵
// 4) 拦截点光点（Fx.absorb 12 tick）：白色 billboard 淡出喵
public class ForceProjectorBlockEntityRenderer implements BlockEntityRenderer<ForceProjectorBlockEntity> {
    private static final ResourceLocation TEX_TOP =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/entity/force_projector_top.png");
    private static final ResourceLocation WHITE_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/misc/white.png");
    private static final int SIDES = 6;
    private static final float EDGE_THICK = 0.06f;   // 线框细条厚度（格），3D 加粗保证可见喵
    private static final float SHIELD_Y = 1.5f;      // 护盾高度（锚点基座上 1.5 格）喵
    private static final float FILL_ALPHA = 0.09f;   // 原版 !animateShields 实心盘 alpha喵

    public ForceProjectorBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(ForceProjectorBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return;
        // 阶段 1：蓄力 top 加色（block-local）喵
        if (be.getBuildup() > 0f && !be.isBroken()) {
            drawBuildupTop(be, pose, buffer);
        }
        // 阶段 2/3/4：世界坐标特效（护盾 + 破碎 + 拦截点）喵
        pose.pushPose();
        pose.setIdentity();
        if (!be.isBroken() && be.getWarmup() > 0.01f) {
            drawShield(be, partialTick, pose, buffer);
        }
        drawBreak(be, partialTick, pose, buffer);
        drawAbsorbPoints(be, partialTick, pose, buffer);
        pose.popPose();
    }

    // 蓄力 top：force-projector-top 铺 3×3（block-local 中心 +1,+1），alpha=buildup/shieldHealth×0.75 喵
    private void drawBuildupTop(ForceProjectorBlockEntity be, PoseStack pose, MultiBufferSource buffer) {
        float alpha = be.getBuildup() / 750f * 0.75f; // shieldHealth=750 喵
        if (alpha < 0.01f) return;
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEX_TOP));
        Matrix4f matrix = pose.last().pose();
        float y = 1.001f;
        int a = (int) (255 * alpha);
        vc.addVertex(matrix, 0f, y, 0f).setUv(0f, 0f).setColor(255, 255, 255, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 0f, y, 3f).setUv(0f, 1f).setColor(255, 255, 255, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 3f, y, 3f).setUv(1f, 1f).setColor(255, 255, 255, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 3f, y, 0f).setUv(1f, 0f).setColor(255, 255, 255, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
    }

    // 护盾：实心盘 + 线框（颜色 team.color→white 按 hit 混合）喵
    private void drawShield(ForceProjectorBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer) {
        float r = be.realRadius();
        if (r <= 0.001f) return;
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double cx = be.centerX() - cam.x, cz = be.centerZ() - cam.z;
        double cy = be.getBlockPos().getY() + SHIELD_Y - cam.y;
        int[] col = teamColor(be.getTeam(), be.getHit());
        // 实心盘（两个 quad 覆盖凸六边形）喵
        VertexConsumer fill = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
        Matrix4f matrix = pose.last().pose();
        int fa = (int) (255 * (FILL_ALPHA + Math.min(0.15f, be.getHit() * 0.12f)));
        float[] vx = new float[SIDES], vz = new float[SIDES];
        for (int i = 0; i < SIDES; i++) {
            double a = Math.toRadians(i * 60.0);
            vx[i] = (float) (cx + r * Math.cos(a));
            vz[i] = (float) (cz + r * Math.sin(a));
        }
        fillHex(fill, matrix, vx, vz, (float) cy, fa, col);
        // 线框（6 条细条）喵
        VertexConsumer edge = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
        int ea = 255;
        for (int i = 0; i < SIDES; i++) {
            int j = (i + 1) % SIDES;
            drawEdge(edge, matrix, vx[i], vz[i], vx[j], vz[j], (float) cy, EDGE_THICK, ea, col);
        }
    }

    // 实心六边形：v0..v5 两个 quad（v0,v1,v2,v3）与（v0,v3,v4,v5）喵
    private void fillHex(VertexConsumer vc, Matrix4f matrix, float[] vx, float[] vz,
                         float y, int a, int[] col) {
        quad(vc, matrix, vx[0], vz[0], vx[1], vz[1], vx[2], vz[2], vx[3], vz[3], y, a, col);
        quad(vc, matrix, vx[0], vz[0], vx[3], vz[3], vx[4], vz[4], vx[5], vz[5], y, a, col);
    }

    private void quad(VertexConsumer vc, Matrix4f matrix, float x0, float z0, float x1, float z1,
                      float x2, float z2, float x3, float z3, float y, int a, int[] col) {
        vc.addVertex(matrix, x0, y, z0).setUv(0f, 0f).setColor(col[0], col[1], col[2], a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, x1, y, z1).setUv(0f, 1f).setColor(col[0], col[1], col[2], a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, x2, y, z2).setUv(1f, 1f).setColor(col[0], col[1], col[2], a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, x3, y, z3).setUv(1f, 0f).setColor(col[0], col[1], col[2], a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
    }

    // 水平细条（世界坐标，相机相对）：从 (x1,z1) 到 (x2,z2) 的矩形线喵
    private void drawEdge(VertexConsumer vc, Matrix4f matrix, float x1, float z1,
                          float x2, float z2, float y, float thick, int a, int[] col) {
        float dx = x2 - x1, dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dz * dz);
        if (len < 1e-4f) return;
        float nx = -dz / len, nz = dx / len;
        float hx = nx * thick / 2f, hz = nz * thick / 2f;
        vc.addVertex(matrix, x1 - hx, y, z1 - hz).setUv(0f, 0f).setColor(col[0], col[1], col[2], a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, x1 + hx, y, z1 + hz).setUv(0f, 1f).setColor(col[0], col[1], col[2], a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, x2 + hx, y, z2 + hz).setUv(1f, 1f).setColor(col[0], col[1], col[2], a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, x2 - hx, y, z2 - hz).setUv(1f, 0f).setColor(col[0], col[1], col[2], a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
    }

    // 破碎动画（Fx.shieldBreak 40 tick）：线框从 breakRadius 扩散 + fout 淡出喵
    private void drawBreak(ForceProjectorBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer) {
        long start = be.getBreakStartGameTime();
        if (start < 0) return;
        long gameTime = be.getLevel().getGameTime();
        float elapsed = gameTime + partialTick - start;
        if (elapsed < 0f || elapsed > be.getBreakEffectLife()) return;
        float fin = elapsed / be.getBreakEffectLife();
        float fout = 1f - fin;
        float r = be.getBreakRadius() + fin * 1.5f; // 3D 适配：扩散 1.5 格（原版 +1 Mindustry 单位）喵
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double cx = be.centerX() - cam.x, cz = be.centerZ() - cam.z;
        double cy = be.getBlockPos().getY() + SHIELD_Y - cam.y;
        int[] col = teamColor(be.getTeam(), 0f);
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
        Matrix4f matrix = pose.last().pose();
        int a = (int) (255 * fout);
        float[] vx = new float[SIDES], vz = new float[SIDES];
        for (int i = 0; i < SIDES; i++) {
            double ang = Math.toRadians(i * 60.0);
            vx[i] = (float) (cx + r * Math.cos(ang));
            vz[i] = (float) (cz + r * Math.sin(ang));
        }
        for (int i = 0; i < SIDES; i++) {
            int j = (i + 1) % SIDES;
            drawEdge(vc, matrix, vx[i], vz[i], vx[j], vz[j], (float) cy, EDGE_THICK, a, col);
        }
    }

    // 拦截点光点（Fx.absorb 12 tick）：白色 billboard 淡出喵
    private void drawAbsorbPoints(ForceProjectorBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer) {
        List<AbsorbPoint> points = be.getAbsorbPoints();
        if (points.isEmpty()) return;
        long gameTime = be.getLevel().getGameTime();
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Matrix4f matrix = pose.last().pose();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
        for (AbsorbPoint ap : points) {
            float elapsed = gameTime + partialTick - ap.time;
            if (elapsed < 0f || elapsed > be.getAbsorbEffectLife()) continue;
            float fout = 1f - elapsed / be.getAbsorbEffectLife();
            Vec3 p = new Vec3(ap.x - cam.x, ap.y - cam.y, ap.z - cam.z);
            drawBillboard(vc, matrix, p, 0.12f * (1f + fout), 255, 255, 255, (int) (255 * fout));
        }
    }

    // 相机朝向 billboard 白点（参考 SiliconSmelter renderer）喵
    private static void drawBillboard(VertexConsumer vc, Matrix4f matrix, Vec3 p,
                                      float half, int cr, int cg, int cb, int alpha) {
        Vec3 toCam = p.scale(-1);
        if (toCam.lengthSqr() < 1e-6) toCam = new Vec3(0, 0, 1);
        else toCam = toCam.normalize();
        Vec3 right = toCam.cross(new Vec3(0, 1, 0));
        if (right.lengthSqr() < 1e-6) right = new Vec3(1, 0, 0);
        else right = right.normalize();
        Vec3 up = right.cross(toCam);
        Vec3 a = p.add(right.scale(half)).add(up.scale(half));
        Vec3 b = p.add(right.scale(half)).subtract(up.scale(half));
        Vec3 c = p.subtract(right.scale(half)).subtract(up.scale(half));
        Vec3 d = p.subtract(right.scale(half)).add(up.scale(half));
        vertex(vc, matrix, a, 0f, 0f, cr, cg, cb, alpha);
        vertex(vc, matrix, b, 0f, 1f, cr, cg, cb, alpha);
        vertex(vc, matrix, c, 1f, 1f, cr, cg, cb, alpha);
        vertex(vc, matrix, d, 1f, 0f, cr, cg, cb, alpha);
    }

    private static void vertex(VertexConsumer vc, Matrix4f matrix, Vec3 p, float u, float v,
                               int cr, int cg, int cb, int alpha) {
        vc.addVertex(matrix, (float) p.x, (float) p.y, (float) p.z)
                .setUv(u, v)
                .setColor(cr, cg, cb, alpha)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(0f, 1f, 0f);
    }

    // 队伍 ARGB → [R,G,B]，受击时向白色混合（原版 Draw.color(team.color, Color.white, clamp(hit))）喵
    private static int[] teamColor(BlockdustryTeam team, float hit) {
        int argb = team.getColor();
        float[] comp = {
                ((argb >> 16) & 0xFF) / 255f,
                ((argb >> 8) & 0xFF) / 255f,
                (argb & 0xFF) / 255f
        };
        float h = Math.max(0f, Math.min(1f, hit));
        int[] out = new int[3];
        for (int i = 0; i < 3; i++) {
            float v = comp[i] + (1f - comp[i]) * h;
            out[i] = (int) (255 * v);
        }
        return out;
    }
}
