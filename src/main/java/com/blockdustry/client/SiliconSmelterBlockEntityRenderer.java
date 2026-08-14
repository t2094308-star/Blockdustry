package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.SiliconSmelterBlockEntity;

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

import java.util.Random;

// 硅冶炼厂动画渲染（Mindustry DrawMulti(DrawDefault, DrawFlame) + Fx.smeltsmoke）：
// - 尾焰：顶贴图 quad（silicon_smelter_top，2×2 footprint）alpha=warmup + glow 染 #ffef99 外圈 + 白芯，
//   外圈/白芯半宽与闪烁频率全按 DrawFlame 参数（详见 docs/子agent/^T35_siliconSmelter冒烟特效研究.md）喵
// - 冒烟：Fx.smeltsmoke 等效——6 个白方烟团（white.png 染白、旋转 45°、相机 billboard），
//   半径 4+fin*5 单位/8、halfSize 0.5+fout*2 单位/8、寿命 15 tick；3D 适配轻微上升 + fout 淡出喵
public class SiliconSmelterBlockEntityRenderer implements BlockEntityRenderer<SiliconSmelterBlockEntity> {
    private static final ResourceLocation TEX_TOP =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/entity/silicon_smelter_top.png");
    private static final ResourceLocation GLOW_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/misc/glow.png");
    private static final ResourceLocation WHITE_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/misc/white.png");
    // Mindustry DrawFlame flameColor = #ffef99 喵
    private static final int FLAME_R = 0xff, FLAME_G = 0xef, FLAME_B = 0x99;
    // Fx.smeltsmoke：寿命 15 tick、粒子 6 个、旋转 45° 喵
    private static final float SMOKE_LIFE = 15f;
    private static final int SMOKE_PARTICLES = 6;
    private static final float SMOKE_ROT_DEG = 45f;

    public SiliconSmelterBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    // 多格建筑 BER 剔除边界：光晕/冒烟超出锚点 1×1×1，从锚点扩到整组 2×2 + inflate，防余光特效被剔（坑/BER渲染.md §3）喵
    @Override
    public AABB getRenderBoundingBox(SiliconSmelterBlockEntity be) {
        BlockPos anchor = be.hasAnchor() ? be.getAnchor() : be.getBlockPos();
        int size = be.getSize();
        return new AABB(anchor.getX(), anchor.getY(), anchor.getZ(),
                anchor.getX() + size, anchor.getY() + size, anchor.getZ() + size).inflate(2.0);
    }

    @Override
    public void render(SiliconSmelterBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return;
        float warmup = be.getWarmup();
        // 阶段 1：顶贴图 quad（block-local pose，水平铺 2×2）喵
        if (warmup > 0f) {
            drawTopQuad(warmup, pose, buffer);
        }
        // 阶段 2：billboard（世界坐标、相机相对）—— 火焰光晕 + 冒烟喵
        pose.pushPose();
        pose.setIdentity();
        if (warmup > 0f) {
            drawFlameGlow(be, warmup, partialTick, pose, buffer);
        }
        drawSmoke(be, partialTick, pose, buffer);
        pose.popPose();
    }

    // 尾焰 base：silicon_smelter_top 贴图铺满 2×2，alpha=warmup（Draw.rect(top, build.x, build.y)）喵
    private void drawTopQuad(float warmup, PoseStack pose, MultiBufferSource buffer) {
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEX_TOP));
        Matrix4f matrix = pose.last().pose();
        int a = (int) (255 * warmup);
        float half = 1.0f;    // 2×2 footprint 半宽 1.0 格（锚点 block-local 中心 = +1,+1）喵
        float y = 1.001f;     // 顶面上方防 z-fighting 喵
        vc.addVertex(matrix, 1f - half, y, 1f - half).setUv(0f, 0f).setColor(255, 255, 255, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 1f - half, y, 1f + half).setUv(0f, 1f).setColor(255, 255, 255, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 1f + half, y, 1f + half).setUv(1f, 1f).setColor(255, 255, 255, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, 1f + half, y, 1f - half).setUv(1f, 0f).setColor(255, 255, 255, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
    }

    // 尾焰光晕：外圈 glow 染 #ffef99 + 内芯白（DrawFlame 两个 Fill.circle，3D 用 billboard）喵
    private void drawFlameGlow(SiliconSmelterBlockEntity be, float warmup, float partialTick,
                               PoseStack pose, MultiBufferSource buffer) {
        long gameTime = be.getLevel().getGameTime();
        // Mindustry Time.time 以秒计，必须 /20（kiln L64 同款）；否则 absin 周期 5/8 tick 闪烁快 20× 喵
        float t = (gameTime + partialTick) / 20f;
        BlockPos base = be.hasAnchor() ? be.getAnchor() : be.getBlockPos();
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        // 火焰中心：2×2 建筑中心顶面上方一点（相机相对坐标）喵
        Vec3 center = new Vec3(
                base.getX() + 1.0 - cam.x,
                base.getY() + 1.15 - cam.y,
                base.getZ() + 1.0 - cam.z);
        Matrix4f matrix = pose.last().pose();
        // 闪烁：DrawFlame 外圈 radius=3+absin(t,5,2)、alpha=0.7+absin(t,8,0.3)；内芯 1.9+absin(t,5,1) 喵
        float absin5 = Math.abs((float) Math.sin(t * (Math.PI * 2f / 5f)));
        float absin8 = Math.abs((float) Math.sin(t * (Math.PI * 2f / 8f)));
        // 外圈：glow 光晕，半宽 (3+2*absin)/8 格，alpha=warmup*(0.7+0.3*absin) 喵
        float glowHalf = (3f + 2f * absin5) / 8f;
        int aGlow = (int) (255 * warmup * (0.7f + 0.3f * absin8));
        VertexConsumer glowVC = buffer.getBuffer(RenderType.entityTranslucent(GLOW_TEX));
        drawRotatedBillboard(glowVC, matrix, center, glowHalf, 0f, FLAME_R, FLAME_G, FLAME_B, aGlow);
        // 内芯：白，半宽 (1.9+absin)/8 格，alpha=warmup 喵
        float coreHalf = (1.9f + absin5) / 8f;
        int aCore = (int) (255 * warmup);
        VertexConsumer whiteVC = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
        drawRotatedBillboard(whiteVC, matrix, center, coreHalf, 0f, 255, 255, 255, aCore);
    }

    // 冒烟（Fx.smeltsmoke）：6 个白方烟团、旋转 45°、相机 billboard、15 tick 淡出 + 轻微上升喵
    private void drawSmoke(SiliconSmelterBlockEntity be, float partialTick, PoseStack pose,
                           MultiBufferSource buffer) {
        long smokeStart = be.getSmokeStartGameTime();
        if (smokeStart < 0) return;
        long gameTime = be.getLevel().getGameTime();
        float elapsed = gameTime + partialTick - smokeStart;
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
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
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

    // billboard 顶点：全亮 + NO_OVERLAY（坑文档 BER渲染 §1/§2）喵
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
