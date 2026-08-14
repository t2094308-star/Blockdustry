package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.production.KilnBlockEntity;

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

// 窖炉火焰渲染（Mindustry DrawFlame，flameColor=#ffc099）：
// 1. 顶面叠层 kiln_top（炉口烧红），alpha = warmup（DrawFlame: Draw.alpha(warmup)）
// 2. 火焰双圈 billboard：外圈染 #ffc099、内圈白，半径随 absin(5s) 脉动 + 逐帧随机抖动（DrawFlame 参数原样折算：8 单位 = 1 格）
// 3. 环境光晕 billboard（Drawf.light 等效，lightRadius=60/lightSinScl=10/lightSinMag=5/size=2/alpha=0.65，裁剪到 4 格防超大 quad）
// 全部用 RenderType.entityTranslucent 单一系（坑/BER渲染.md §4 防交错崩），顶点全亮 + NO_OVERLAY（坑 §1 §2）喵
public class KilnBlockEntityRenderer implements BlockEntityRenderer<KilnBlockEntity> {
    private static final ResourceLocation TOP_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/kiln_top.png");
    // 本 mod 径向光晕贴图（软边圆光）喵
    private static final ResourceLocation GLOW_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/misc/glow.png");

    // Mindustry DrawFlame.flameColor = #ffc099 喵
    private static final int FLAME_R = 0xff, FLAME_G = 0xc0, FLAME_B = 0x99;
    // DrawFlame 参数（世界单位 → 块：Mindustry 8 单位 = 1 格）喵
    private static final float FLAME_RADIUS = 3f / 8f;        // 外圈基半径 3 单位喵
    private static final float FLAME_RADIUS_IN = 1.9f / 8f;   // 内圈基半径 1.9 单位喵
    private static final float FLAME_RADIUS_MAG = 2f / 8f;    // 外圈脉动幅度 2 单位喵
    private static final float FLAME_RADIUS_IN_MAG = 1f / 8f; // 内圈脉动幅度 1 单位喵
    private static final float FLAME_RADIUS_SCL = 5f;         // 半径脉动周期 5s 喵
    private static final float BREATH_SCL = 8f;               // 火焰亮度呼吸周期 8s（DrawFlame g=0.3）喵
    private static final float G = 0.3f;                      // 呼吸系数（DrawFlame g）喵
    private static final float R = 0.06f;                     // 随机亮度抖动幅度（DrawFlame r）喵

    public KilnBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    // 多格建筑 BER 剔除边界：覆盖整组 2×2 + 火焰光晕喵
    @Override
    public AABB getRenderBoundingBox(KilnBlockEntity be) {
        BlockPos anchor = be.hasAnchor() ? be.getAnchor() : be.getBlockPos();
        int size = be.getSize();
        return new AABB(anchor.getX(), anchor.getY(), anchor.getZ(),
                anchor.getX() + size, anchor.getY() + size, anchor.getZ() + size).inflate(3.0);
    }

    @Override
    public void render(KilnBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return; // 只在锚点格画一次喵
        float warmup = be.getWarmup();
        if (warmup <= 0f) return;   // DrawFlame: warmup>0 才画喵
        float time = (be.getLevel().getGameTime() + partialTick) / 20f; // Mindustry Time.time（秒）喵

        // —— 阶段 1：顶面炉口叠层（block 本地坐标，覆盖 2×2 顶面）喵 ——
        pose.pushPose();
        pose.translate(0.0, 1.001, 0.0); // 抬到顶面之上防 z-fighting 喵
        VertexConsumer topVC = buffer.getBuffer(RenderType.entityTranslucent(TOP_TEX));
        Matrix4f topMatrix = pose.last().pose();
        int topAlpha = (int) (255 * warmup); // DrawFlame: Draw.alpha(warmup) 喵
        // 2×2 顶面 quad：本地 (0,0,0)-(2,0,2)，UV 全图（kiln_top 64×64 覆盖整块）喵
        quad(topVC, topMatrix, 0f, 0f, 2f, 2f, 255, 255, 255, topAlpha);
        pose.popPose();

        // —— 阶段 2+3：火焰双圈 + 环境光晕（相机空间 billboard，全亮）喵 ——
        pose.pushPose();
        pose.setIdentity(); // 去掉方块本地平移，顶点用「相对相机」世界坐标（坑/BER渲染.md §5）喵
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        BlockPos anchor = be.hasAnchor() ? be.getAnchor() : be.getBlockPos();
        // 火焰在整块顶面中心：锚点本地 (1.0, 顶面之上, 1.0) 喵
        Vec3 flamePos = new Vec3(anchor.getX() + 1.0, anchor.getY() + 1.02, anchor.getZ() + 1.0);
        Vec3 flameLocal = flamePos.subtract(cam);

        // 火焰亮度呼吸：DrawFlame alpha = ((1-g) + absin(time,8,g) + rand(r) - r) * warmup 喵
        float absin8 = Math.abs((float) Math.sin(time * (Math.PI * 2f / BREATH_SCL)));
        float randJitter = (float) Math.random() * R;
        float flameAlpha = ((1f - G) + absin8 * G + randJitter - R) * warmup;
        if (flameAlpha <= 0.01f) {
            pose.popPose();
            return;
        }
        int fa = (int) (255 * flameAlpha);

        // 半径脉动：DrawFlame radius = base + absin(time,5,mag) + rand(0.1)，8 单位=1 格喵
        float absin5 = Math.abs((float) Math.sin(time * (Math.PI * 2f / FLAME_RADIUS_SCL)));
        float cr = (float) Math.random() * 0.1f;
        float outerR = FLAME_RADIUS + absin5 * FLAME_RADIUS_MAG + cr;
        float innerR = FLAME_RADIUS_IN + absin5 * FLAME_RADIUS_IN_MAG + cr;

        VertexConsumer glowVC = buffer.getBuffer(RenderType.entityTranslucent(GLOW_TEX));
        Matrix4f glowMatrix = pose.last().pose();
        // 外圈火焰（#ffc099 染）喵
        drawBillboard(glowVC, glowMatrix, flameLocal, outerR, FLAME_R, FLAME_G, FLAME_B, fa);
        // 内圈白焰（DrawFlame Draw.color(1,1,1,warmup)）喵
        drawBillboard(glowVC, glowMatrix, flameLocal, innerR, 255, 255, 255, fa);
        // 环境光晕（Drawf.light 等效：lightRadius=60、absin(10,5)、×warmup×size2，8 单位=1 格 → 最大约 16 格，裁剪到 4 格）喵
        float lightAbsin = Math.abs((float) Math.sin(time * (Math.PI * 2f / 10f)));
        float lightR = Math.min(4f, (60f + lightAbsin * 5f) * warmup * 2f / 8f);
        if (lightR > 0.5f) {
            drawBillboard(glowVC, glowMatrix, flameLocal, lightR, FLAME_R, FLAME_G, FLAME_B, (int) (0.35f * 255 * warmup));
        }
        pose.popPose();
    }

    // 顶面叠层 quad（本地坐标，绕序无关，entityTranslucent 无 cull）喵
    private static void quad(VertexConsumer vc, Matrix4f matrix, float x0, float z0, float x1, float z1,
                             int cr, int cg, int cb, int alpha) {
        vc.addVertex(matrix, x0, 0f, z0).setUv(0f, 0f).setColor(cr, cg, cb, alpha)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, x0, 0f, z1).setUv(0f, 1f).setColor(cr, cg, cb, alpha)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, x1, 0f, z1).setUv(1f, 1f).setColor(cr, cg, cb, alpha)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, x1, 0f, z0).setUv(1f, 0f).setColor(cr, cg, cb, alpha)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
    }

    // 相机朝向 billboard：中心 p（相对相机）、半径 r、染 rgba，四顶点双面（entityTranslucent 无 cull）喵
    private static void drawBillboard(VertexConsumer vc, Matrix4f matrix, Vec3 p, float r,
                                      int cr, int cg, int cb, int alpha) {
        Vec3 toCam = p.scale(-1);
        if (toCam.lengthSqr() < 1e-6) toCam = new Vec3(0, 0, 1); else toCam = toCam.normalize();
        Vec3 right = toCam.cross(new Vec3(0, 1, 0));
        if (right.lengthSqr() < 1e-6) right = new Vec3(1, 0, 0); else right = right.normalize();
        Vec3 up = right.cross(toCam).normalize();
        Vec3 a = p.add(right.scale(r)).add(up.scale(r));
        Vec3 b = p.add(right.scale(r)).subtract(up.scale(r));
        Vec3 c = p.subtract(right.scale(r)).subtract(up.scale(r));
        Vec3 d = p.subtract(right.scale(r)).add(up.scale(r));
        billboardVertex(vc, matrix, a, 0f, 0f, cr, cg, cb, alpha);
        billboardVertex(vc, matrix, b, 0f, 1f, cr, cg, cb, alpha);
        billboardVertex(vc, matrix, c, 1f, 1f, cr, cg, cb, alpha);
        billboardVertex(vc, matrix, d, 1f, 0f, cr, cg, cb, alpha);
    }

    // 光晕纹理顶点：UV 全图覆盖，径向渐变光晕，全亮 + NO_OVERLAY 喵
    private static void billboardVertex(VertexConsumer vc, Matrix4f matrix, Vec3 p, float u, float v,
                                        int cr, int cg, int cb, int alpha) {
        vc.addVertex(matrix, (float) p.x, (float) p.y, (float) p.z)
                .setUv(u, v)
                .setColor(cr, cg, cb, alpha)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(0f, 1f, 0f);
    }
}
