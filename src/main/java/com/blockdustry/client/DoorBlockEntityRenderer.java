package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.defense.DoorBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

import org.joml.Matrix4f;

// 门渲染（Mindustry Door.draw = Draw.rect(open ? openRegion : region)，`@-open` 开态贴图）喵。
// 1. 门体：整块立方体（size×1×size），关=门贴图（不透明）、开=开门贴图（门洞透明，entityCutoutNoCull 表现透明），全 6 面贴门图喵
// 2. 开关特效（核心动画，Mindustry Fx.dooropen/doorclose，Effect(10)tick）：
//    Lines.square 方块轮廓线——开门外扩（+e.fin()×2）、关门内缩（+e.fout()×2），描边 e.fout()×1.6 淡出喵
// 全亮 + NO_OVERLAY（坑/BER渲染.md §1 §2），整段只用 entityCutoutNoCull 单一渲染系（坑 §4 防交错崩）喵
public class DoorBlockEntityRenderer implements BlockEntityRenderer<DoorBlockEntity> {
    private static final ResourceLocation CLOSED_1 =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/door.png");
    private static final ResourceLocation OPEN_1 =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/door_open.png");
    private static final ResourceLocation CLOSED_2 =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/door_large.png");
    private static final ResourceLocation OPEN_2 =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/door_large_open.png");

    // Mindustry 8 单位 = 1 格；Fx.dooropen 描边 1.6 单位 → 0.2 格；外扩/内缩幅度 2 单位 → 0.25 格喵
    private static final float STROKE_MAX = 1.6f / 8f;
    private static final float GROWTH = 2f / 8f;

    public DoorBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    // 多格建筑 BER 剔除边界：覆盖整组 size×size + 特效外扩喵
    @Override
    public AABB getRenderBoundingBox(DoorBlockEntity be) {
        BlockPos anchor = be.hasAnchor() ? be.getAnchor() : be.getBlockPos();
        int size = be.getSize();
        return new AABB(anchor.getX(), anchor.getY(), anchor.getZ(),
                anchor.getX() + size, anchor.getY() + size, anchor.getZ() + size).inflate(2.0);
    }

    @Override
    public void render(DoorBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return; // 只在锚点格画一次喵
        boolean open = be.isOpen();
        int size = Math.max(1, be.getSize());
        ResourceLocation tex = (size >= 2)
                ? (open ? OPEN_2 : CLOSED_2)
                : (open ? OPEN_1 : CLOSED_1);

        // —— 门体立方体（6 面，全亮 + NO_OVERLAY）——
        pose.pushPose();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(tex));
        Matrix4f mat = pose.last().pose();
        drawCube(vc, mat, size);
        pose.popPose();

        // —— 开关方块轮廓特效（切换后 10 tick 内）——
        long age = be.getEffectAge(be.getLevel().getGameTime() + (long) partialTick);
        if (age >= 0 && age < DoorBlockEntity.EFFECT_DURATION) {
            float t = age / (float) DoorBlockEntity.EFFECT_DURATION;
            float fin = t, fout = 1f - t;
            // 半边长：开门外扩 / 关门内缩（Mindustry rotation*tilesize/2 = size/2 格；large 版同值）喵
            float half = size / 2f + GROWTH * (be.isOpening() ? fin : fout);
            float stroke = Math.max(0.02f, STROKE_MAX * fout);
            int alpha = (int) (255 * fout);
            pose.pushPose();
            VertexConsumer outlineVc = buffer.getBuffer(RenderType.entityCutoutNoCull(tex));
            Matrix4f outlineMat = pose.last().pose();
            drawSquareFrame(outlineVc, outlineMat, size / 2f, 1.02f, size / 2f, half, stroke, alpha);
            pose.popPose();
        }
    }

    // 画尺寸 size×1×size 的立方体（门体），6 面均贴门图（UV 全图，不透明）喵
    private static void drawCube(VertexConsumer vc, Matrix4f mat, int size) {
        float s = size;
        // 顶面（+Y）
        quad(vc, mat, 0, 1, 0, s, 1, 0, s, 1, s, 0, 1, s, 0f, 1f, 0f, 255);
        // 底面（-Y）
        quad(vc, mat, 0, 0, 0, s, 0, 0, s, 0, s, 0, 0, s, 0f, -1f, 0f, 255);
        // 北面（-Z）
        quad(vc, mat, 0, 0, 0, s, 0, 0, s, 1, 0, 0, 1, 0, 0f, 0f, -1f, 255);
        // 南面（+Z）
        quad(vc, mat, 0, 0, s, s, 0, s, s, 1, s, 0, 1, s, 0f, 0f, 1f, 255);
        // 东面（+X）
        quad(vc, mat, s, 0, 0, s, 0, s, s, 1, s, s, 1, 0, 1f, 0f, 0f, 255);
        // 西面（-X）
        quad(vc, mat, 0, 0, 0, 0, 0, s, 0, 1, s, 0, 1, 0, -1f, 0f, 0f, 255);
    }

    // 画方块轮廓框（Mindustry Lines.square 等效）：中心 (cx,cy,cz)、半边长 half、厚度 stroke、白/alpha 淡出喵
    private static void drawSquareFrame(VertexConsumer vc, Matrix4f mat, float cx, float cy, float cz,
                                        float half, float stroke, int alpha) {
        // 四个角
        float x0 = cx - half, x1 = cx + half, z0 = cz - half, z1 = cz + half;
        // 上边（A→B，沿 +X，向内 +Z）
        quad(vc, mat, x0, cy, z0, x1, cy, z0, x1, cy, z0 + stroke, x0, cy, z0 + stroke, 0f, 1f, 0f, alpha);
        // 右边（B→C，沿 +Z，向内 -X）
        quad(vc, mat, x1, cy, z0, x1, cy, z1, x1 - stroke, cy, z1, x1 - stroke, cy, z0, 0f, 1f, 0f, alpha);
        // 下边（C→D，沿 -X，向内 -Z）
        quad(vc, mat, x1, cy, z1, x0, cy, z1, x0, cy, z1 - stroke, x1, cy, z1 - stroke, 0f, 1f, 0f, alpha);
        // 左边（D→A，沿 -Z，向内 +X）
        quad(vc, mat, x0, cy, z1, x0, cy, z0, x0 + stroke, cy, z0, x0 + stroke, cy, z1, 0f, 1f, 0f, alpha);
    }

    // 4 顶点四边形（entityCutoutNoCull 双面，无需绕序；全亮 + NO_OVERLAY）喵
    private static void quad(VertexConsumer vc, Matrix4f mat,
                             float ax, float ay, float az,
                             float bx, float by, float bz,
                             float cx, float cy, float cz,
                             float dx, float dy, float dz,
                             float nx, float ny, float nz, int alpha) {
        vertex(vc, mat, ax, ay, az, 0f, 0f, nx, ny, nz, alpha);
        vertex(vc, mat, bx, by, bz, 1f, 0f, nx, ny, nz, alpha);
        vertex(vc, mat, cx, cy, cz, 1f, 1f, nx, ny, nz, alpha);
        vertex(vc, mat, dx, dy, dz, 0f, 1f, nx, ny, nz, alpha);
    }

    private static void vertex(VertexConsumer vc, Matrix4f mat,
                               float x, float y, float z, float u, float v,
                               float nx, float ny, float nz, int alpha) {
        vc.addVertex(mat, x, y, z)
                .setUv(u, v)
                .setColor(255, 255, 255, alpha)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(nx, ny, nz);
    }
}
