package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.production.IncineratorBlockEntity;

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

// 焚化炉火焰渲染（Mindustry IncineratorBuild.draw 全参数忠实迁移）：
//   heat>0 时：alpha = ((1-g) + absin(Time.time,8,g) + rand(r) - r) × heat（g=0.3, r=0.06）
//   外圈 Fill.circle 半径 2 单位、tint flameColor #ffad9d；内圈 Fill.circle 半径 1 单位、纯白 alpha=heat。
//   3D 用 billboard 光晕（外圈 glow.png 染 #ffad9d，内圈 white.png 白），2 单位=0.25 格、1 单位=0.125 格。
// 全部 RenderType.entityTranslucent 单一系（坑/BER渲染.md §4），顶点全亮 + NO_OVERLAY（§1 §2）喵
public class IncineratorBlockEntityRenderer implements BlockEntityRenderer<IncineratorBlockEntity> {
    private static final ResourceLocation GLOW_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/misc/glow.png");
    private static final ResourceLocation WHITE_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/misc/white.png");
    // Mindustry Incinerator.flameColor = #ffad9d 喵
    private static final int FLAME_R = 0xff, FLAME_G = 0xad, FLAME_B = 0x9d;
    // IncineratorBuild.draw 参数（Mindustry 8 单位 = 1 格）喵
    private static final float G = 0.3f;                  // 呼吸系数喵
    private static final float R = 0.06f;                 // 随机亮度抖动幅度喵
    private static final float BREATH_SCL = 8f;           // 呼吸周期 8s 喵
    private static final float OUTER_R = 2f / 8f;         // 外圈半径 2 单位 = 0.25 格喵
    private static final float INNER_R = 1f / 8f;         // 内圈半径 1 单位 = 0.125 格喵

    public IncineratorBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public AABB getRenderBoundingBox(IncineratorBlockEntity be) {
        BlockPos anchor = be.hasAnchor() ? be.getAnchor() : be.getBlockPos();
        int size = be.getSize();
        return new AABB(anchor.getX(), anchor.getY(), anchor.getZ(),
                anchor.getX() + size, anchor.getY() + size, anchor.getZ() + size).inflate(2.0);
    }

    @Override
    public void render(IncineratorBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return; // size 1 恒锚点，保留守卫喵
        float heat = be.getHeat();
        if (heat <= 0f) return; // IncineratorBuild.draw: heat>0 才画喵
        float time = (be.getLevel().getGameTime() + partialTick) / 20f; // Mindustry Time.time（秒）喵

        pose.pushPose();
        pose.setIdentity(); // 去掉方块本地平移，顶点用「相对相机」世界坐标（坑/BER渲染.md §5）喵
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        BlockPos anchor = be.hasAnchor() ? be.getAnchor() : be.getBlockPos();
        // 火焰在 1×1 块中心偏上（相机相对坐标）喵
        Vec3 flamePos = new Vec3(anchor.getX() + 0.5 - cam.x, anchor.getY() + 0.5 - cam.y, anchor.getZ() + 0.5 - cam.z);

        // 外圈 alpha：((1-g) + absin(time,8,g) + rand(r) - r) × heat 喵
        float absin8 = Math.abs((float) Math.sin(time * (Math.PI * 2f / BREATH_SCL)));
        float randJitter = (float) Math.random() * R;
        float outerAlpha = ((1f - G) + absin8 * G + randJitter - R) * heat;
        if (outerAlpha <= 0.01f) {
            pose.popPose();
            return;
        }
        int aOuter = (int) (255 * outerAlpha);
        // 内圈 alpha：Draw.color(1,1,1,heat) → 纯白固定 heat 喵
        int aInner = (int) (255 * heat);

        Matrix4f matrix = pose.last().pose();
        VertexConsumer glowVC = buffer.getBuffer(RenderType.entityTranslucent(GLOW_TEX));
        // 外圈火焰（#ffad9d 染）喵
        drawBillboard(glowVC, matrix, flamePos, OUTER_R, FLAME_R, FLAME_G, FLAME_B, aOuter);
        // 内圈白焰喵
        VertexConsumer whiteVC = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEX));
        drawBillboard(whiteVC, matrix, flamePos, INNER_R, 255, 255, 255, aInner);
        pose.popPose();
    }

    // 相机朝向 billboard：中心 p（相对相机）、半径 r、染 rgba，双面（entityTranslucent 无 cull）喵
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

    // 光晕/白圆纹理顶点：UV 全图覆盖，全亮 + NO_OVERLAY 喵
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
