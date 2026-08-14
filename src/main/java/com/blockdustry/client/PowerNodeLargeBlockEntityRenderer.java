package com.blockdustry.client;

import com.blockdustry.power.PowerNodeLargeBlockEntity;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import java.util.List;

// 大型电力节点激光渲染（2×2 power-node-large 版）：沿用 1×1 PowerNodeBlockEntityRenderer 的激光机制，
// 忠于 Mindustry LaserColor——白→琥珀按电网满足率 lerp。只画激光（不含 1×1 专属倒角斜面，原版大型节点贴图自带切角观感）喵
public class PowerNodeLargeBlockEntityRenderer implements BlockEntityRenderer<PowerNodeLargeBlockEntity> {
    // Mindustry Pal.powerLight = #fbd367（琥珀），缺电端点色喵
    private static final float AMBER_R = 0xfb / 255f;
    private static final float AMBER_G = 0xd3 / 255f;
    private static final float AMBER_B = 0x67 / 255f;
    // 本 mod 1×1 纯白纹理（原版 white.png 不可靠），配合 vertex 颜色染色成纯色光柱喵
    private static final ResourceLocation WHITE_TEX =
            ResourceLocation.fromNamespaceAndPath(com.blockdustry.Blockdustry.MODID, "textures/misc/white.png");

    public PowerNodeLargeBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    // NeoForge 扩展：渲染剔除边界覆盖所有激光端点，源节点在视野外但目标在视野内时激光仍显示喵
    @Override
    public AABB getRenderBoundingBox(PowerNodeLargeBlockEntity be) {
        AABB box = new AABB(be.getBlockPos());
        for (BlockPos p : be.getPowerLinks()) {
            box = box.minmax(new AABB(p));
        }
        return box.inflate(1.0);
    }

    @Override
    public void render(PowerNodeLargeBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        // 只锚点格画激光（非锚点格是填充块，画了会重叠双份）喵
        if (!be.isAnchor()) return;
        List<BlockPos> links = be.getRenderLinks();
        if (links.isEmpty()) return;

        pose.pushPose();
        pose.setIdentity();
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 from = linkPoint(be.getBlockPos(), be).subtract(cam);
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
        for (BlockPos target : links) {
            Vec3 to = linkPoint(target, be.getLevel().getBlockEntity(target)).subtract(cam);
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

    // 链接端点：大型节点目标偏移半格到整组中心，其余按方块中心（顶部 y+0.5）喵
    private static Vec3 linkPoint(BlockPos pos, BlockEntity be) {
        boolean large = be instanceof PowerNodeLargeBlockEntity;
        return pos.getCenter().add(large ? 0.5 : 0, 0.5, large ? 0.5 : 0);
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

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
