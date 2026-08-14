package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.entities.FireBulletEntity;

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

// Mindustry fuse 金属碎片霰弹（ShrapnelBulletType）渲染喵
// - 弹体：沿速度方向的白→a9d8ff 长三角金属片（宽 17/8=2.125 → 0、长 min(射程,已飞行)、fout 淡出），
//   弹尾补一根反向短三角（长 10/8=1.25）做成钝角喵
// - 命中白闪（Fx.hitLancer 等效）：8 根白色辐射短线、全方向、12 tick、线长 1→5 格喵
// - 光效（Drawf.light 等效，T15 方案 2 billboard）：沿弹片每 ~2.5 格铺 toColor 低 alpha 光晕（线光），
//   弹丸起点（炮口）放暖黄 fbd367 光晕（点光，半径 20/8=2.5）喵
// 原版碎片是纯色三角无贴图 → 用纯色染 quad，不拷贴图；禁火焰色 ffdd55/db401c 喵
public class FireBulletRenderer extends EntityRenderer<FireBulletEntity> {
    // 本 mod 1×1 纯白纹理（弹体纯色染）+ 径向光晕（光效 billboard）喵
    private static final ResourceLocation WHITE_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/misc/white.png");
    private static final ResourceLocation GLOW_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/misc/glow.png");

    // Mindustry Pal.lancerLaser = #a9d8ff（titanium 弹药终点色，fuse 只启用钛弹）喵
    private static final int TO_R = 0xa9, TO_G = 0xd8, TO_B = 0xff;
    // Mindustry Pal.powerLight = #fbd367（弹丸点光暖黄）喵
    private static final int PL_R = 0xfb, PL_G = 0xd3, PL_B = 0x67;

    // 折算参数（Mindustry 8 单位 = 1 格）喵
    private static final float W = 17f / 8f;           // 弹宽 17 单位 = 2.125 格（初始，随 fout 缩到 0）
    private static final float RANGE = 90f / 8f;       // 射程 90 单位 = 11.25 格
    private static final float LENGTH = 100f / 8f;     // 弹长 100 单位 = 12.5 格（命中长上限）
    private static final float TAIL_LEN = 10f / 8f;    // 尾部钝角短三角 10 单位 = 1.25 格
    private static final float FLASH_LIFE = 12f;       // Fx.hitLancer 白闪寿命 12 tick
    private static final float LIGHT_SPACING = 2.5f;   // 线光 billboard 每 2-3 格一张

    public FireBulletRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(FireBulletEntity entity, float yaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffer, int light) {
        Vec3 vel = entity.getDeltaMovement();
        Vec3 longAxis = new Vec3(vel.x, 0, vel.z).normalize();
        if (longAxis.lengthSqr() < 1e-6) {
            longAxis = new Vec3(1, 0, 0);
        }
        Vec3 wide = new Vec3(-longAxis.z, 0, longAxis.x);

        // 渐变与衰减：fin = age/maxLife（白→a9d8ff），shardFout = 1-fin（宽与透明度都乘 fout）。
        // 命中建筑后弹丸停 12 tick 播白闪（age 已超 maxLife，shardFout=0 无弹片），白闪用独立 foutF 不受影响喵
        float age = entity.tickCount + partialTick;
        float maxLife = Math.max(1, entity.getMaxLife());
        float fin = Math.min(1f, age / maxLife);
        float shardFout = Math.max(0f, 1f - fin);
        int flash = entity.getFlashTicks();
        if (shardFout <= 0.01f && flash <= 0) return;
        int cr = (int) (255 + (TO_R - 255) * fin);
        int cg = (int) (255 + (TO_G - 255) * fin);
        int cb = (int) (255 + (TO_B - 255) * fin);
        int alpha = (int) (255 * shardFout);
        float wEff = W * shardFout;

        // 弹片长 L = min(射程, 命中长)：hitscan 折中飞行弹下取已飞距离，随弹飞行从炮口伸到命中点喵
        float speed = (float) vel.length();
        float traveled = age * speed;
        float L = Math.min(Math.min(RANGE, LENGTH), Math.max(0f, traveled));

        Vec3 camLocal = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().subtract(entity.position());
        Matrix4f matrix = pose.last().pose();

        // —— 阶段 1：光效 billboard（glow 纹理，单一 RenderType 不交错）喵
        if (shardFout > 0.01f) {
            VertexConsumer glowVC = buffer.getBuffer(RenderType.entityTranslucent(GLOW_TEX));
            // 线光：沿弹片每 ~2.5 格铺一张低 alpha 光晕（模拟 Drawf.light 线光，颜色 toColor、透明度 0.6、宽随 fout 缩）喵
            for (float d = 0.5f; d <= L; d += LIGHT_SPACING) {
                Vec3 p = longAxis.scale(-d);
                float r = Math.max(0.6f, wEff * 0.8f);
                drawBillboard(glowVC, matrix, p, camLocal, r, TO_R, TO_G, TO_B, (int) (0.6f * 255 * shardFout));
            }
            // 点光：弹丸起点（炮口）暖黄 fbd367 光晕（半径 20/8=2.5、透明度 0.6、随 fout 淡出，模拟 BulletType.drawLight）喵
            Vec3 spawnLocal = entity.getSpawn().subtract(entity.position());
            float pr = (20f / 8f) * shardFout;
            drawBillboard(glowVC, matrix, spawnLocal, camLocal, pr, PL_R, PL_G, PL_B, (int) (0.6f * 255 * shardFout));

            // —— 阶段 2：弹片主体 + 尾三角（white 纹理）喵
            VertexConsumer whiteVC = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
            float y = 0.06f; // y 抬高防与地表共面喵
            if (L > 0.01f) {
                Vec3 baseC = longAxis.scale(-L);
                float hw = wEff / 2f;
                Vec3 c0 = baseC.subtract(wide.scale(hw));
                Vec3 d0 = baseC.add(wide.scale(hw));
                // 主体：弹尖在实体（宽 0），向后延伸 L 到宽底（宽 wEff），等腰三角（退化 quad）喵
                vertex(whiteVC, matrix, 0f, y, 0f, cr, cg, cb, alpha);
                vertex(whiteVC, matrix, 0f, y, 0f, cr, cg, cb, alpha);
                vertex(whiteVC, matrix, (float) c0.x, y, (float) c0.z, cr, cg, cb, alpha);
                vertex(whiteVC, matrix, (float) d0.x, y, (float) d0.z, cr, cg, cb, alpha);
                // 尾部钝角：弹尾补一根反向短三角（长 1.25 格）喵
                Vec3 tailApex = baseC.subtract(longAxis.scale(TAIL_LEN));
                vertex(whiteVC, matrix, (float) c0.x, y, (float) c0.z, cr, cg, cb, alpha);
                vertex(whiteVC, matrix, (float) d0.x, y, (float) d0.z, cr, cg, cb, alpha);
                vertex(whiteVC, matrix, (float) tailApex.x, y, (float) tailApex.z, cr, cg, cb, alpha);
                vertex(whiteVC, matrix, (float) tailApex.x, y, (float) tailApex.z, cr, cg, cb, alpha);
            }
        }

        // 命中白闪（Fx.hitLancer 等效）：8 根白色辐射短线、全方向、lifetime 12 tick、线长 1→5 格喵
        if (flash > 0) {
            VertexConsumer flashVC = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
            float foutF = Math.max(0f, flash / FLASH_LIFE);
            float y = 0.06f;
            Random fr = new Random(entity.getId() * 104729L + 1L);
            for (int i = 0; i < 8; i++) {
                float ang = fr.nextFloat() * (float) (Math.PI * 2);
                float tilt = (fr.nextFloat() - 0.5f) * 0.6f; // 少量俯仰，3D 全方向感喵
                Vec3 d = new Vec3((float) Math.cos(ang), tilt, (float) Math.sin(ang)).normalize();
                float len = 1f + 4f * foutF; // 线长 1→5 格随 fout 缩喵
                Vec3 a = new Vec3(0f, y, 0f);
                Vec3 b = a.add(d.scale(len));
                Vec3 mid = a.add(b).scale(0.5f);
                Vec3 view = mid.scale(-1).add(camLocal);
                if (view.lengthSqr() < 1e-6) view = new Vec3(0, 0, 1); else view = view.normalize();
                Vec3 n = d.cross(view);
                if (n.lengthSqr() < 1e-6) n = new Vec3(1, 0, 0); else n = n.normalize();
                // 线宽 stroke = fout*1.5 单位 → 半宽 1.5*fout/8/2 格喵
                beam(flashVC, matrix, a, b, n, 0.09375f * foutF, 255, 255, 255, (int) (255 * foutF));
            }
        }
    }

    // 相机朝向 billboard：中心 p、半径 r、染 rgba，四顶点双面（entityTranslucent 无 cull）喵
    private static void drawBillboard(VertexConsumer vc, Matrix4f matrix, Vec3 p, Vec3 camLocal,
                                      float r, int cr, int cg, int cb, int alpha) {
        Vec3 toCam = camLocal.subtract(p);
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

    // 白纹理顶点：UV 固定 (0,0)（1×1 纯白染纯色）喵
    private static void vertex(VertexConsumer vc, Matrix4f matrix, float x, float y, float z,
                               int cr, int cg, int cb, int alpha) {
        vc.addVertex(matrix, x, y, z)
                .setColor(cr, cg, cb, alpha)
                .setUv(0f, 0f)
                .setOverlay(OverlayTexture.NO_OVERLAY) // 0xFFFFFF 越界采样染黑，必须 NO_OVERLAY 喵
                .setLight(0xF000F0) // 全亮，不随环境光变暗（坑-炮管黑.md）喵
                .setNormal(0f, 1f, 0f);
    }

    // 光晕纹理顶点：UV 全图覆盖，径向渐变光晕喵
    private static void billboardVertex(VertexConsumer vc, Matrix4f matrix, Vec3 p, float u, float v,
                                        int cr, int cg, int cb, int alpha) {
        vc.addVertex(matrix, (float) p.x, (float) p.y, (float) p.z)
                .setUv(u, v)
                .setColor(cr, cg, cb, alpha)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(0f, 1f, 0f);
    }

    @Override
    public ResourceLocation getTextureLocation(FireBulletEntity entity) {
        return WHITE_TEX;
    }
}
