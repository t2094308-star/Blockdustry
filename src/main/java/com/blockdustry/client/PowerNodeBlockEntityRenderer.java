package com.blockdustry.client;

import com.blockdustry.power.PowerNodeBlockEntity;

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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

// PowerNode 激光渲染：忠于 Mindustry LaserColor——白→琥珀按电网满足率 lerp。
// 纯 RenderType.entityTranslucent（无 cull 双面渲染，唯一 renderType 不交错不崩）：
// billboard 双面任何角度都看到全宽光柱，白纹理染色成纯色喵
public class PowerNodeBlockEntityRenderer implements BlockEntityRenderer<PowerNodeBlockEntity> {
    // Mindustry Pal.powerLight = #fbd367（琥珀），缺电端点色喵
    private static final float AMBER_R = 0xfb / 255f;
    private static final float AMBER_G = 0xd3 / 255f;
    private static final float AMBER_B = 0x67 / 255f;
    // 本 mod 1×1 纯白纹理（原版 white.png 不可靠），配合 vertex 颜色染色成纯色光柱/斜面喵
    private static final ResourceLocation WHITE_TEX =
            ResourceLocation.fromNamespaceAndPath(com.blockdustry.Blockdustry.MODID, "textures/misc/white.png");

    // 8 角等边三角斜面（模型坐标 0-16，画时 /16 转本地世界）。每角切点沿三条棱距角 5 单位
    // → 切面平面过 (5,0,0)(0,5,0)(0,0,5)，三边均 5√2 等边三角形，法线 (1,1,1)/√3 朝外 = 45° 倒角喵
    private static final float[][][] CHAMFER_TRIS = {
            {{5, 0, 0}, {0, 5, 0}, {0, 0, 5}},           // 角(0,0,0)喵
            {{11, 0, 0}, {16, 5, 0}, {16, 0, 5}},        // 角(16,0,0)喵
            {{5, 16, 0}, {0, 11, 0}, {0, 16, 5}},        // 角(0,16,0)喵
            {{11, 16, 0}, {16, 11, 0}, {16, 16, 5}},     // 角(16,16,0)喵
            {{5, 0, 16}, {0, 5, 16}, {0, 0, 11}},        // 角(0,0,16)喵
            {{11, 0, 16}, {16, 5, 16}, {16, 0, 11}},     // 角(16,0,16)喵
            {{5, 16, 16}, {0, 11, 16}, {0, 16, 11}},     // 角(0,16,16)喵
            {{11, 16, 16}, {16, 11, 16}, {16, 16, 11}}   // 角(16,16,16)喵
    };
    // 每角斜面外法线（角坐标 0→-1, 16→+1，归一化 /√3）喵
    private static final float[][] CHAMFER_NORMALS = {
            {-0.57735f, -0.57735f, -0.57735f},
            {0.57735f, -0.57735f, -0.57735f},
            {-0.57735f, 0.57735f, -0.57735f},
            {0.57735f, 0.57735f, -0.57735f},
            {-0.57735f, -0.57735f, 0.57735f},
            {0.57735f, -0.57735f, 0.57735f},
            {-0.57735f, 0.57735f, 0.57735f},
            {0.57735f, 0.57735f, 0.57735f}
    };
    // 斜面配色：贴图侧边深灰 (110,112,128)，与 power_node 材质一致喵
    private static final int CHAMFER_R = 110, CHAMFER_G = 112, CHAMFER_B = 128;

    public PowerNodeBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    // NeoForge 扩展：渲染剔除边界覆盖所有激光端点，源节点在视野外但目标在视野内时激光仍显示喵
    @Override
    public AABB getRenderBoundingBox(PowerNodeBlockEntity be) {
        AABB box = new AABB(be.getBlockPos());
        for (BlockPos p : be.getPowerLinks()) {
            box = box.minmax(new AABB(p));
        }
        return box.inflate(1.0);
    }

    @Override
    public void render(PowerNodeBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        // 8 角等边三角斜面（真 45° 去角倒角）：方块模型 cube_all 角部实心，BER 画凸出斜面盖住角，
        // 顶点全亮 + NO_OVERLAY 防暗/黑（见 坑/BER渲染.md），法线朝外使 diffuse 光照自然喵
        {
            VertexConsumer chamferVC = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
            Matrix4f chamferMatrix = pose.last().pose();
            for (int i = 0; i < 8; i++) {
                drawChamfer(chamferVC, chamferMatrix, i);
            }
        }
        // 激光（世界坐标，须重置矩阵）喵
        if (!be.isAnchor() || be.getPowerLinks().isEmpty()) return;
        pose.pushPose();
        pose.setIdentity();
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 from = be.getBlockPos().getCenter().add(0, 0.5, 0).subtract(cam);
        // Mindustry：color = lerp(white, powerLight, t)，t=(1-satisfaction)*0.86+absin；满电近白、缺电琥珀喵
        float sat = be.getPowerStatus();
        float pulse = Math.abs((float) Math.sin(be.getLevel().getGameTime() * 0.05f)) * 0.06f; // 微弱呼吸感喵
        float t = (1f - sat) * 0.86f + pulse;
        int ri = (int) (lerp(1f, AMBER_R, t) * 255f);
        int gi = (int) (lerp(1f, AMBER_G, t) * 255f);
        int bi = (int) (lerp(1f, AMBER_B, t) * 255f);
        // 只用 entityTranslucent 一种 renderType，避免 sorted 类型交错 flush 导致 Not building 崩溃喵
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
        Matrix4f matrix = pose.last().pose();
        for (BlockPos target : be.getPowerLinks()) {
            Vec3 to = target.getCenter().add(0, 0.5, 0).subtract(cam);
            Vec3 d = to.subtract(from);
            double len = d.length();
            if (len < 1e-4) continue;
            Vec3 dir = d.normalize();
            // billboard：一面的法向 = dir×视线，该面恒面向相机；entityTranslucent 无 cull，另一面也可见喵
            Vec3 center = from.add(to).scale(0.5);
            Vec3 view = center.scale(-1); // 光束中心→相机（相对坐标已含 cam 偏移）喵
            if (view.lengthSqr() < 1e-6) view = new Vec3(0, 0, 1); else view = view.normalize();
            Vec3 n1 = dir.cross(view);
            if (n1.lengthSqr() < 1e-6) n1 = new Vec3(1, 0, 0); else n1 = n1.normalize();
            Vec3 n2 = dir.cross(n1).normalize();
            // 两个垂直面，宽度 0.2，视觉粗光柱喵
            float hw = 0.1f;
            beam(vc, matrix, from, to, n1, hw, ri, gi, bi, 230);
            beam(vc, matrix, from, to, n2, hw, ri, gi, bi, 230);
            // 端点小十字光点：节点醒目喵
            cross(vc, matrix, from, n1, n2, ri, gi, bi);
            cross(vc, matrix, to, n1, n2, ri, gi, bi);
        }
        pose.popPose();
    }

    // 沿 a→b 的细长矩形（面法向 n，半宽 hw），四顶点（无 cull，绕序无关）喵
    private static void beam(VertexConsumer vc, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 n,
                             float hw, int r, int g, int bl, int alpha) {
        Vec3 a0 = a.add(n.scale(-hw)), a1 = a.add(n.scale(hw));
        Vec3 b0 = b.add(n.scale(-hw)), b1 = b.add(n.scale(hw));
        vertex(vc, matrix, a0, r, g, bl, alpha);
        vertex(vc, matrix, b0, r, g, bl, alpha);
        vertex(vc, matrix, b1, r, g, bl, alpha);
        vertex(vc, matrix, a1, r, g, bl, alpha);
    }

    // 端点小十字光点：两个垂直细长条喵
    private static void cross(VertexConsumer vc, Matrix4f matrix, Vec3 c, Vec3 u, Vec3 v, int r, int g, int bl) {
        float len = 0.18f, hw = 0.035f;
        beam(vc, matrix, c.subtract(u.scale(len)), c.add(u.scale(len)), v, hw, r, g, bl, 255);
        beam(vc, matrix, c.subtract(v.scale(len)), c.add(v.scale(len)), u, hw, r, g, bl, 255);
    }

    private static void vertex(VertexConsumer vc, Matrix4f matrix, Vec3 p,
                               int r, int g, int bl, int alpha) {
        vc.addVertex(matrix, (float) p.x, (float) p.y, (float) p.z)
                .setColor(r, g, bl, alpha)
                .setUv(0f, 0f)
                .setOverlay(OverlayTexture.NO_OVERLAY) // 0xFFFFFF 会让 overlay 纹理越界采样成透明黑把光束染黑，必须用 NO_OVERLAY 喵
                .setLight(0xF000F0) // 全亮，不随环境光照变暗喵
                .setNormal(0f, 1f, 0f); // 固定朝上：entity shader 的 diffuse 光照恒定最亮，避免背面法线把颜色压黑喵
    }

    // 画单个角斜面三角形：模型坐标 /16 转本地世界，顶点沿外法线平移 eps 凸出 cube_all 表面防 z-fighting 喵
    // 注意：entityTranslucent 是 QUADS 模式，必须 4 顶点对齐（退化 quad v0,v1,v2,v0），否则顶点错乱把深灰色块画到别处喵
    private static void drawChamfer(VertexConsumer vc, Matrix4f matrix, int idx) {
        float[][] tri = CHAMFER_TRIS[idx];
        float[] n = CHAMFER_NORMALS[idx];
        final float inv = 1f / 16f;
        final float eps = 0.02f; // 世界单位外凸：比之前大，进一步压掉与 cube_all 面的 z-fighting/穿插喵
        for (int v = 0; v < 4; v++) {
            int vi = v % 3; // 0,1,2,0：三角形退化 quad，凑满 QUADS 顶点数喵
            vertex(vc, matrix,
                    tri[vi][0] * inv + n[0] * eps,
                    tri[vi][1] * inv + n[1] * eps,
                    tri[vi][2] * inv + n[2] * eps,
                    CHAMFER_R, CHAMFER_G, CHAMFER_B, 255,
                    n[0], n[1], n[2]);
        }
    }

    // 带法线的顶点：斜面用真实朝外法线，entity shader diffuse 光照让斜切面有自然明暗喵
    private static void vertex(VertexConsumer vc, Matrix4f matrix, float x, float y, float z,
                               int r, int g, int bl, int alpha, float nx, float ny, float nz) {
        vc.addVertex(matrix, x, y, z)
                .setColor(r, g, bl, alpha)
                .setUv(0f, 0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(nx, ny, nz);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
