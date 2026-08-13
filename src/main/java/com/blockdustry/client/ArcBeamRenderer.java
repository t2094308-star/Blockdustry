package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.entities.ArcBeamEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import java.util.Random;

// Mindustry arc 电弧渲染：从炮口（实体位置）到命中点画锯齿闪电链（Mindustry Lightning 视觉近似）。
// 纯 RenderType.entityTranslucent（无 cull 双面），白色纹理染色成 Pal.lancerLaser=#a9d8ff 浅蓝喵
public class ArcBeamRenderer extends EntityRenderer<ArcBeamEntity> {
    // 本 mod 1×1 纯白纹理（原版 white.png 不可靠），配合 vertex 颜色染色喵
    private static final ResourceLocation WHITE_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/misc/white.png");
    // Mindustry Pal.lancerLaser = #a9d8ff（arc 闪电色）喵
    private static final int BOLT_R = 0xa9, BOLT_G = 0xd8, BOLT_B = 0xff;
    // 闪电锯齿段数喵
    private static final int SEGS = 8;

    public ArcBeamRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(ArcBeamEntity entity, float yaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffer, int light) {
        // 实体渲染器 pose 原点 = 实体位置（炮口），终点换算成局部坐标喵
        Vec3 end = entity.getEnd().subtract(entity.position());
        double len = end.length();
        if (len < 1e-4) return;

        // 用实体 id 作随机种子：闪电形状稳定（跨帧不闪烁）喵
        Random r = new Random(entity.getId() * 7919L);
        Vec3[] pts = new Vec3[SEGS + 1];
        pts[0] = Vec3.ZERO;
        pts[SEGS] = end;
        Vec3 dir = end.normalize();
        Vec3 perp = new Vec3(-dir.z, 0, dir.x);
        if (perp.lengthSqr() < 1e-6) perp = new Vec3(1, 0, 0);
        perp = perp.normalize();
        float amt = (float) len * 0.09f; // 横向抖动幅度：随长度缩放喵
        for (int i = 1; i < SEGS; i++) {
            Vec3 base = Vec3.ZERO.lerp(end, i / (float) SEGS);
            Vec3 off = perp.scale((r.nextFloat() * 2f - 1f) * amt)
                    .add(new Vec3(0, (r.nextFloat() * 2f - 1f) * amt * 0.35f, 0));
            pts[i] = base.add(off);
        }

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
        Matrix4f matrix = pose.last().pose();
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
                .subtract(entity.position());
        // 每段画两个垂直面（billboard 双面都朝向相机），粗闪电感喵
        for (int i = 0; i < SEGS; i++) {
            Vec3 a = pts[i], b = pts[i + 1];
            Vec3 d = b.subtract(a);
            if (d.lengthSqr() < 1e-8) continue;
            Vec3 dn = d.normalize();
            Vec3 mid = a.add(b).scale(0.5);
            Vec3 view = mid.scale(-1).add(cam); // 段中点→相机（局部坐标）喵
            if (view.lengthSqr() < 1e-6) view = new Vec3(0, 0, 1); else view = view.normalize();
            Vec3 n1 = dn.cross(view);
            if (n1.lengthSqr() < 1e-6) n1 = new Vec3(1, 0, 0); else n1 = n1.normalize();
            Vec3 n2 = dn.cross(n1).normalize();
            float hw = 0.05f; // 闪电半宽 0.05 格喵
            beam(vc, matrix, a, b, n1, hw, BOLT_R, BOLT_G, BOLT_B, 220);
            beam(vc, matrix, a, b, n2, hw, BOLT_R, BOLT_G, BOLT_B, 220);
        }
        // 端点亮点（炮口与命中点）喵
        float hl = 0.12f, hw2 = 0.03f;
        Vec3 dn = dir;
        Vec3 perp2 = perp;
        beam(vc, matrix, Vec3.ZERO.subtract(dn.scale(hl)), Vec3.ZERO.add(dn.scale(hl)), perp2, hw2, 255, 255, 255, 255);
        beam(vc, matrix, end.subtract(dn.scale(hl)), end.add(dn.scale(hl)), perp2, hw2, 255, 255, 255, 255);
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

    private static void vertex(VertexConsumer vc, Matrix4f matrix, Vec3 p,
                               int r, int g, int bl, int alpha) {
        vc.addVertex(matrix, (float) p.x, (float) p.y, (float) p.z)
                .setColor(r, g, bl, alpha)
                .setUv(0f, 0f)
                .setOverlay(OverlayTexture.NO_OVERLAY) // 0xFFFFFF 会越界采样染黑，必须用 NO_OVERLAY 喵
                .setLight(0xF000F0) // 全亮，不随环境光照变暗喵
                .setNormal(0f, 1f, 0f); // entity shader 固定最亮，避免背面法线压黑喵
    }

    @Override
    public ResourceLocation getTextureLocation(ArcBeamEntity entity) {
        return WHITE_TEX;
    }
}
