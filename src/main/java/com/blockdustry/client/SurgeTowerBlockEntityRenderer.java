package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.SurgeTowerBlockEntity;

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

// Mindustry PowerNode 涌电塔（surge-tower）激光渲染喵。
// 忠实原版 PowerNode.laser：白→琥珀按电网满足率 lerp 的远距光柱（laserRange=40f）。
// 复用 PowerNodeBlockEntityRenderer 的激光绘制法（billboard 双面 + entityTranslucent 单一 renderType）喵。
// 视觉层：塔顶放射光柱到链接目标（原版标准 PowerNode 激光，无闪电特效——见光效研究文档）喵
public class SurgeTowerBlockEntityRenderer implements BlockEntityRenderer<SurgeTowerBlockEntity> {
    // Mindustry Pal.powerLight = #fbd367（琥珀），缺电端点色喵
    private static final float AMBER_R = 0xfb / 255f;
    private static final float AMBER_G = 0xd3 / 255f;
    private static final float AMBER_B = 0x67 / 255f;
    private static final ResourceLocation WHITE_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/misc/white.png");

    public SurgeTowerBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    // NeoForge 扩展：渲染剔除边界覆盖所有激光端点，源塔在视野外但目标在视野内时激光仍显示喵
    @Override
    public AABB getRenderBoundingBox(SurgeTowerBlockEntity be) {
        AABB box = new AABB(be.getBlockPos());
        for (BlockPos p : be.getPowerLinks()) {
            box = box.minmax(new AABB(p));
        }
        return box.inflate(1.0);
    }

    @Override
    public void render(SurgeTowerBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        // 只锚点格画激光（links 存锚点），非锚点格仅由方块模型显示底座喵
        if (!be.isAnchor() || be.getPowerLinks().isEmpty()) return;

        pose.pushPose();
        pose.setIdentity();
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        // 2×2 塔顶中心：锚点格中心 + (0.5, 0.5, 0.5)（原版 laserRange 从建筑中心起算）喵
        Vec3 from = be.getBlockPos().getCenter().add(0.5, 0.5, 0.5).subtract(cam);

        // Mindustry：color = lerp(white, powerLight, t)，t=(1-satisfaction)*0.86+absin；满电近白、缺电琥珀喵
        float sat = be.getPowerStatus();
        float pulse = Math.abs((float) Math.sin(be.getLevel().getGameTime() * 0.05f)) * 0.06f;
        float t = (1f - sat) * 0.86f + pulse;
        int ri = (int) (lerp(1f, AMBER_R, t) * 255f);
        int gi = (int) (lerp(1f, AMBER_G, t) * 255f);
        int bi = (int) (lerp(1f, AMBER_B, t) * 255f);

        // 只用 entityTranslucent 一种 renderType，避免 sorted 类型交错 flush 崩溃喵
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
        Matrix4f matrix = pose.last().pose();
        for (BlockPos target : be.getPowerLinks()) {
            Vec3 to = target.getCenter().add(0, 0.5, 0).subtract(cam);
            Vec3 d = to.subtract(from);
            double len = d.length();
            if (len < 1e-4) continue;
            Vec3 dir = d.normalize();
            Vec3 center = from.add(to).scale(0.5);
            Vec3 view = center.scale(-1);
            if (view.lengthSqr() < 1e-6) view = new Vec3(0, 0, 1); else view = view.normalize();
            Vec3 n1 = dir.cross(view);
            if (n1.lengthSqr() < 1e-6) n1 = new Vec3(1, 0, 0); else n1 = n1.normalize();
            Vec3 n2 = dir.cross(n1).normalize();
            float hw = 0.1f;
            beam(vc, matrix, from, to, n1, hw, ri, gi, bi, 230);
            beam(vc, matrix, from, to, n2, hw, ri, gi, bi, 230);
            cross(vc, matrix, from, n1, n2, ri, gi, bi);
            cross(vc, matrix, to, n1, n2, ri, gi, bi);
        }
        pose.popPose();
    }

    // 沿 a→b 的细长矩形（面法向 n，半宽 hw）喵
    private static void beam(VertexConsumer vc, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 n,
                             float hw, int r, int g, int bl, int alpha) {
        Vec3 a0 = a.add(n.scale(-hw)), a1 = a.add(n.scale(hw));
        Vec3 b0 = b.add(n.scale(-hw)), b1 = b.add(n.scale(hw));
        vertex(vc, matrix, a0, r, g, bl, alpha);
        vertex(vc, matrix, b0, r, g, bl, alpha);
        vertex(vc, matrix, b1, r, g, bl, alpha);
        vertex(vc, matrix, a1, r, g, bl, alpha);
    }

    // 端点小十字光点喵
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
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(0f, 1f, 0f);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
