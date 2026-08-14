package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.distribution.ItemBridgeBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

// 传送带桥渲染（忠于原版 ItemBridgeBuild.draw）喵。
// 配对有效时画：端部面板（endRegion，两端）+ 桥面条带（bridgeRegion）+ 沿桥滚动箭头（arrowRegion）喵。
// 原版 bridge-conveyor fadeIn=false → alpha 恒 1；箭头 alpha 用 |sin((a - time/arrowTimeScl)/arrowPeriod·2π)| 滚动喵。
// 数据（原版）：bridgeWidth=6.5（≈0.8125 格）、arrowSpacing=6（0.75 格）、arrowOffset=2（0.25 格）、arrowTimeScl=6.2、arrowPeriod=0.4 喵
public class ItemBridgeBlockEntityRenderer implements BlockEntityRenderer<ItemBridgeBlockEntity> {
    private static final ResourceLocation END_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/bridge_conveyor_end.png");
    private static final ResourceLocation BRIDGE_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/bridge_conveyor_bridge.png");
    private static final ResourceLocation ARROW_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/bridge_conveyor_arrow.png");

    // 原版像素/格换算：1 格 = 8 world units 喵
    private static final float BRIDGE_HALF_W = 6.5f / 8f / 2f;   // bridgeWidth 6.5 半宽喵
    private static final float ARROW_SPACING = 6f / 8f;          // 0.75 格喵
    private static final float ARROW_OFFSET = 2f / 8f;           // 0.25 格喵
    private static final float ARROW_HALF = 0.3f;                // 箭头半宽（贴图透明边裁剪）喵
    private static final float END_HALF = 0.5f;                  // 端部面板半宽 1 格喵
    private static final float ARROW_TIME_SCL = 6.2f;
    private static final float ARROW_PERIOD = 0.4f;

    public ItemBridgeBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(ItemBridgeBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return;
        BlockPos link = be.getLink();
        if (link == null) return;

        double dx = link.getX() - be.getBlockPos().getX();
        double dz = link.getZ() - be.getBlockPos().getZ();
        int dist = (int) Math.max(Math.abs(dx), Math.abs(dz));
        if (dist < 1 || dist > ItemBridgeBlockEntity.RANGE) return; // 配对失效不再画桥喵

        double cx = 0.5, cz = 0.5;
        double ox = dx + 0.5, oz = dz + 0.5;
        double len = Math.sqrt(dx * dx + dz * dz);
        double ux = dx / len, uz = dz / len;   // 桥向单位向量喵
        double px = -uz, pz = ux;              // 垂直方向喵

        // 本体 cube_all 满格方块占 0~1，桥面画在其顶面之上（原版桥面架空于地面之上）喵
        float y = 1.01f;
        float yEnd = 1.005f; // 端部稍低防与桥面共面渗色喵

        // 端部面板（两端，旋转到桥向；原版 i*90+90）喵
        double yaw = Math.toDegrees(Math.atan2(dx, dz));
        drawQuad(pose, buffer, END_TEX, cx, cz, END_HALF, END_HALF, yEnd, yaw, 255);
        drawQuad(pose, buffer, END_TEX, ox, oz, END_HALF, END_HALF, yEnd, yaw, 255);

        // 桥面条带（原版从两端中心各缩进 tilesize/2=半格）喵
        double sx = cx + ux * 0.5, sz = cz + uz * 0.5;
        double ex = ox - ux * 0.5, ez = oz - uz * 0.5;
        drawStrip(pose, buffer, BRIDGE_TEX, sx, sz, ex, ez, px, pz, BRIDGE_HALF_W, y);

        // 沿桥滚动箭头（原版 Lod.l1 分支）喵
        drawArrows(be, pose, buffer, cx, cz, ux, uz, dist, y + 0.01f);
    }

    // 平铺地面 quad（绕 Y 旋转 yaw，法线朝上）喵
    private static void drawQuad(PoseStack pose, MultiBufferSource buffer, ResourceLocation tex,
                                 double cx, double cz, double halfW, double halfH, float y, double yawDeg, int alpha) {
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(tex));
        var m = pose.last().pose();
        double rad = Math.toRadians(yawDeg);
        double cos = Math.cos(rad), sin = Math.sin(rad);
        float a = alpha;
        // 局部 (0,0)~(1,1) 角点，中心为原点，旋转到 yaw，再平移到 (cx,cz) 喵
        addV(vc, m, rotX(-halfW, -halfH, cos, sin) + cx, y, rotZ(-halfW, -halfH, cos, sin) + cz, 0, 0, a);
        addV(vc, m, rotX(-halfW, halfH, cos, sin) + cx, y, rotZ(-halfW, halfH, cos, sin) + cz, 0, 1, a);
        addV(vc, m, rotX(halfW, halfH, cos, sin) + cx, y, rotZ(halfW, halfH, cos, sin) + cz, 1, 1, a);
        addV(vc, m, rotX(halfW, -halfH, cos, sin) + cx, y, rotZ(halfW, -halfH, cos, sin) + cz, 1, 0, a);
    }

    // 桥面条带：起点 (sx,sz) 到终点 (ex,ez)，沿垂直方向 (px,pz) 展宽 ±halfW 喵
    private static void drawStrip(PoseStack pose, MultiBufferSource buffer, ResourceLocation tex,
                                  double sx, double sz, double ex, double ez,
                                  double px, double pz, double halfW, float y) {
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(tex));
        var m = pose.last().pose();
        float a = 255;
        addV(vc, m, sx + px * halfW, y, sz + pz * halfW, 0, 0, a);
        addV(vc, m, sx - px * halfW, y, sz - pz * halfW, 0, 1, a);
        addV(vc, m, ex - px * halfW, y, ez - pz * halfW, 1, 1, a);
        addV(vc, m, ex + px * halfW, y, ez + pz * halfW, 1, 0, a);
    }

    // 沿桥排列的滚动箭头（原版 arrows = dist*tilesize/arrowSpacing；alpha 用 absin 滚动）喵
    private static void drawArrows(ItemBridgeBlockEntity be, PoseStack pose, MultiBufferSource buffer,
                                   double cx, double cz, double ux, double uz, int dist, float y) {
        if (be.getLevel() == null) return;
        // 服务器时间（秒）：tick / 20 喵
        float timeSec = be.getLevel().getGameTime() / 20f;
        int arrows = (int) (dist * 8f / 6f); // dist*tilesize/arrowSpacing = dist*8/6 喵
        double yaw = Math.toDegrees(Math.atan2(ux, uz));
        for (int a = 0; a < arrows; a++) {
            double t = 0.5 + a * ARROW_SPACING + ARROW_OFFSET; // 距本格中心 0.5 + a*0.75 + 0.25 喵
            double ax = cx + ux * t;
            double az = cz + uz * t;
            // Mathf.absin(a - time/arrowTimeScl, arrowPeriod, 1) = |sin((v)/period * 2π)| 喵
            double v = a - timeSec / ARROW_TIME_SCL;
            double alpha = Math.abs(Math.sin(v / ARROW_PERIOD * Math.PI * 2));
            drawQuad(pose, buffer, ARROW_TEX, ax, az, ARROW_HALF, ARROW_HALF, y + 0.01f, yaw, (int) (alpha * 255));
        }
    }

    private static double rotX(double x, double z, double cos, double sin) {
        return x * cos + z * sin;
    }

    private static double rotZ(double x, double z, double cos, double sin) {
        return -x * sin + z * cos;
    }

    private static void addV(VertexConsumer vc, Matrix4f m, double x, float y, double z, float u, float v, float a) {
        vc.addVertex(m, (float) x, y, (float) z)
                .setUv(u, v)
                .setColor(255, 255, 255, a)
                .setLight(0xF000F0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0f, 1f, 0f);
    }
}
