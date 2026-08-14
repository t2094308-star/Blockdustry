package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.PhaseWeaverBlockEntity;

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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import java.util.Random;

// 相织布编织器动画渲染（Mindustry DrawMulti(DrawRegion(-bottom), DrawWeave, DrawDefault) + Fx.smeltsmoke）喵。
// 原版织机运动（DrawWeave.java，逐条核对）：
//   1. DrawRegion("-bottom")：贴 phase-weaver-bottom 底贴（窗口地板），静态喵
//   2. DrawWeave：
//      a. Draw.rect(weave, x, y, totalProgress) —— 织纹贴图绕中心旋转，角度=totalProgress(rad)喵
//      b. Draw.color(Pal.accent #ffd37f) + Draw.alpha(warmup) —— 梭线染 accent，透明度=warmup喵
//      c. Lines.lineAngleCenter(x+Mathf.sin(totalProgress,6f,tilesize/3*size), y, 90, size*tilesize/2)
//         —— 竖向梭线（沿 z，长 1 格），x 按 sin(totalProgress*2π/6) 扫描，幅 2/3 格，周期 6 时间单位喵
//   3. DrawDefault：基贴 phase-weaver（带透明窗口），盖在织纹之上，窗口露出织纹/地板，frame 遮住边缘喵
// 时序：totalProgress 服务端按 warmup×3/MC tick 累积（60rad/s），渲染器从同步时刻按同速率外推，保证旋转连续喵。
// 全部 entityTranslucent 单一系（坑/BER渲染.md §4），顶点全亮 + NO_OVERLAY（§1/§2）喵
public class PhaseWeaverBlockEntityRenderer implements BlockEntityRenderer<PhaseWeaverBlockEntity> {
    private static final ResourceLocation TEX_BASE =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/phase_weaver.png");
    private static final ResourceLocation TEX_BOTTOM =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/phase_weaver_bottom.png");
    private static final ResourceLocation TEX_WEAVE =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/phase_weaver_weave.png");
    private static final ResourceLocation TEX_WHITE =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/misc/white.png");

    // Mindustry Pal.accent = #ffd37f（DrawWeave 梭线颜色）喵
    private static final int ACCENT_R = 0xff, ACCENT_G = 0xd3, ACCENT_B = 0x7f;
    // DrawWeave 扫描参数：幅 = tilesize/3*size = 16/3 单位 /8 = 2/3 格；线长 = size*tilesize/2 = 8 单位 /8 = 1 格喵
    private static final float SWEEP_AMP = 2f / 3f;
    private static final float LINE_HALF_LEN = 0.5f;
    // 梭线半宽（Mindustry Lines stroke ≈ 1.5 单位 /8 ≈ 0.19 格，取半宽 0.095）喵
    private static final float LINE_HALF_W = 0.095f;
    // totalProgress 外推速率：Mindustry 60rad/s ÷ MC 20tps = 3 rad/MC tick 喵
    private static final float RAD_PER_MC_TICK = 3f;

    // Fx.smeltsmoke：寿命 15 tick、粒子 6 个、旋转 45°、白色方烟团喵
    private static final float SMOKE_LIFE = 15f;
    private static final int SMOKE_PARTICLES = 6;
    private static final float SMOKE_ROT_DEG = 45f;

    public PhaseWeaverBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    // 多格建筑 BER 剔除边界：覆盖整组 2×2 + 冒烟喵
    @Override
    public AABB getRenderBoundingBox(PhaseWeaverBlockEntity be) {
        BlockPos anchor = be.hasAnchor() ? be.getAnchor() : be.getBlockPos();
        int size = be.getSize();
        return new AABB(anchor.getX(), anchor.getY(), anchor.getZ(),
                anchor.getX() + size, anchor.getY() + size, anchor.getZ() + size).inflate(2.0);
    }

    @Override
    public void render(PhaseWeaverBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return; // 只在锚点格画一次整组喵
        float warmup = be.getWarmup();
        float t = be.getLevel().getGameTime() + partialTick;
        // 客户端外推织机旋转角（服务器同步值 + 距同步时刻按 warmup×3 推进）喵
        float tp = be.getTotalProgress() + (t - be.getTotalProgressSyncGameTime()) * warmup * RAD_PER_MC_TICK;

        // —— 阶段 1：窗口内容（block-local pose，锚点格本地 0..2 × 0..2 顶面）——
        // 层次（自下而上）：bottom(地板) → weave(织纹,旋转) → shuttle(梭线) → mask(基贴 frame+窗口) 喵
        Matrix4f m = pose.last().pose();

        // 1. 底贴 phase-weaver-bottom（窗口地板，全不透明）喵
        VertexConsumer vcBottom = buffer.getBuffer(RenderType.entityTranslucent(TEX_BOTTOM));
        quadFull(m, vcBottom, 1.001f, 255, 255, 255, 255);

        // 2. 织纹 phase-weaver-weave（绕 2×2 中心旋转 totalProgress，Draw.rect(weave,x,y,totalProgress)）喵
        pose.pushPose();
        pose.translate(1.0f, 1.002f, 1.0f);
        pose.mulPose(Axis.YP.rotation(tp));
        VertexConsumer vcWeave = buffer.getBuffer(RenderType.entityTranslucent(TEX_WEAVE));
        quadCentered(pose.last().pose(), vcWeave, 0f, 255, 255, 255, 255);
        pose.popPose();

        // 3. 梭线（accent 竖线沿 z，x 扫描；DrawWeave color accent alpha warmup）喵
        if (warmup > 0.01f) {
            float cx = 1f + SWEEP_AMP * (float) Math.sin(tp * (Math.PI / 3f));
            VertexConsumer vcShuttle = buffer.getBuffer(RenderType.entityTranslucent(TEX_WHITE));
            quadLine(m, vcShuttle, cx, 1.003f, LINE_HALF_LEN, LINE_HALF_W,
                    ACCENT_R, ACCENT_G, ACCENT_B, (int) (255 * warmup));
        }

        // 4. mask 基贴 phase-weaver（frame 不透明 + 窗口透明，裁掉窗口外的 bottom/weave/shuttle）喵
        VertexConsumer vcMask = buffer.getBuffer(RenderType.entityTranslucent(TEX_BASE));
        quadFull(m, vcMask, 1.004f, 255, 255, 255, 255);

        // —— 阶段 2：冒烟 billboard（相机空间，Fx.smeltsmoke）——
        pose.pushPose();
        pose.setIdentity();
        drawSmoke(be, t, pose, buffer);
        pose.popPose();
    }

    // 整张 2×2 顶面 quad：本地 (0,y,0)-(2,y,2)，UV 全图，染 rgba，全亮 + NO_OVERLAY 喵
    private static void quadFull(Matrix4f m, VertexConsumer vc, float y, int r, int g, int b, int a) {
        vc.addVertex(m, 0f, y, 0f).setUv(0f, 0f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(m, 0f, y, 2f).setUv(0f, 1f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(m, 2f, y, 2f).setUv(1f, 1f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(m, 2f, y, 0f).setUv(1f, 0f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
    }

    // 中心 quad：本地 (-1,y,-1)-(1,y,1)，UV 全图（旋转后的织纹用，绕已平移到中心的 pose）喵
    private static void quadCentered(Matrix4f m, VertexConsumer vc, float y, int r, int g, int b, int a) {
        vc.addVertex(m, -1f, y, -1f).setUv(0f, 0f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(m, -1f, y, 1f).setUv(0f, 1f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(m, 1f, y, 1f).setUv(1f, 1f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(m, 1f, y, -1f).setUv(1f, 0f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
    }

    // 竖向梭线 quad：中心 (cx, y, 1)，z 半长 halfLen、x 半宽 halfW，染 rgba 喵
    private static void quadLine(Matrix4f m, VertexConsumer vc, float cx, float y, float halfLen, float halfW,
                                 int r, int g, int b, int a) {
        vc.addVertex(m, cx - halfW, y, 1f - halfLen).setUv(0f, 0f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(m, cx - halfW, y, 1f + halfLen).setUv(0f, 1f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(m, cx + halfW, y, 1f + halfLen).setUv(1f, 1f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(m, cx + halfW, y, 1f - halfLen).setUv(1f, 0f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
    }

    // 冒烟（Fx.smeltsmoke）：6 个白方烟团、旋转 45°、相机 billboard、15 tick 淡出 + 轻微上升（SiliconSmelter 同款）喵
    private void drawSmoke(PhaseWeaverBlockEntity be, float t, PoseStack pose, MultiBufferSource buffer) {
        long smokeStart = be.getSmokeStartGameTime();
        if (smokeStart < 0) return;
        float elapsed = t - smokeStart; // t = gameTime+partialTick，与冒烟起点同用 MC tick 喵
        if (elapsed < 0f || elapsed > SMOKE_LIFE) return;
        float fin = elapsed / SMOKE_LIFE; // 0→1 喵
        float fout = 1f - fin;
        BlockPos base = be.hasAnchor() ? be.getAnchor() : be.getBlockPos();
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        // 烟囱口：2×2 中心上方（相机相对坐标）喵
        double px = base.getX() + 1.0 - cam.x;
        double py = base.getY() + 1.3 - cam.y;
        double pz = base.getZ() + 1.0 - cam.z;
        Matrix4f matrix = pose.last().pose();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEX_WHITE));
        // 种子：锚点哈希 + 冒烟起点，跨帧稳定；不同爆次图案不同喵
        Random rng = new Random(base.asLong() * 31L + smokeStart * 7L);
        // Mindustry randLenVectors：半径 4+fin*5 单位、halfSize 0.5+fout*2 单位（1 格 = 8 单位）喵
        float radius = (4f + fin * 5f) / 8f;   // 0.5 → 1.125 格
        float half = (0.5f + fout * 2f) / 8f;  // 0.3125 → 0.0625 格
        int alpha = (int) (255 * fout);        // 3D 适配：fout 淡出（原版骤灭）喵
        for (int i = 0; i < SMOKE_PARTICLES; i++) {
            float ang = (float) (rng.nextFloat() * Math.PI * 2);
            float len = rng.nextFloat() * radius;
            double x = px + Math.cos(ang) * len;
            double z = pz + Math.sin(ang) * len;
            double y = py + 0.3 * fin; // 3D 适配：轻微上升（原版水平面）喵
            drawRotatedBillboard(vc, matrix, new Vec3(x, y, z), half, SMOKE_ROT_DEG, 255, 255, 255, alpha);
        }
    }

    // 相机朝向 billboard：中心 p（相机相对坐标）、半宽 half、绕视轴旋转 rotDeg、染 rgba，双面（entityTranslucent 无 cull）喵
    private static void drawRotatedBillboard(VertexConsumer vc, Matrix4f matrix, Vec3 p,
                                             float half, float rotDeg, int cr, int cg, int cb, int alpha) {
        Vec3 toCam = p.scale(-1);
        if (toCam.lengthSqr() < 1e-6) toCam = new Vec3(0, 0, 1); else toCam = toCam.normalize();
        Vec3 right = toCam.cross(new Vec3(0, 1, 0));
        if (right.lengthSqr() < 1e-6) right = new Vec3(1, 0, 0); else right = right.normalize();
        Vec3 up = right.cross(toCam);
        // 绕视轴旋转 rotDeg（Fill.square rotation=45）喵
        double rad = Math.toRadians(rotDeg);
        double cos = Math.cos(rad), sin = Math.sin(rad);
        Vec3 r2 = right.scale((float) cos).add(up.scale((float) sin));
        Vec3 u2 = right.scale((float) -sin).add(up.scale((float) cos));
        Vec3 a = p.add(r2.scale(half)).add(u2.scale(half));
        Vec3 b = p.add(r2.scale(half)).subtract(u2.scale(half));
        Vec3 c = p.subtract(r2.scale(half)).subtract(u2.scale(half));
        Vec3 d = p.subtract(r2.scale(half)).add(u2.scale(half));
        vertex(vc, matrix, a, 0f, 0f, cr, cg, cb, alpha);
        vertex(vc, matrix, b, 0f, 1f, cr, cg, cb, alpha);
        vertex(vc, matrix, c, 1f, 1f, cr, cg, cb, alpha);
        vertex(vc, matrix, d, 1f, 0f, cr, cg, cb, alpha);
    }

    // billboard 顶点：全亮 + NO_OVERLAY（坑/BER渲染.md §1/§2）喵
    private static void vertex(VertexConsumer vc, Matrix4f matrix, Vec3 p, float u, float v,
                               int cr, int cg, int cb, int alpha) {
        vc.addVertex(matrix, (float) p.x, (float) p.y, (float) p.z)
                .setUv(u, v)
                .setColor(cr, cg, cb, alpha)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(0f, 1f, 0f);
    }
}
