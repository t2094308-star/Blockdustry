package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.BlastDrillBlockEntity;
import com.blockdustry.building.LaserDrillBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

// laser-drill 渲染：锚点格上方叠加 Mindustry rotator 旋转钻头 + top 盖。
// 原版当前版本无独立激光光束绘制，视觉 = 底座（方块模型顶面 laser_drill.png 含紫色激光能量装饰）+ rotator 旋转 + top 盖喵
public class LaserDrillBlockEntityRenderer implements BlockEntityRenderer<LaserDrillBlockEntity> {
    private static final ResourceLocation ROTATOR =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/laser_drill_rotator.png");
    private static final ResourceLocation TOP =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/laser_drill_top.png");
    private static final ResourceLocation ITEM =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/laser_drill_item.png");
    private static final float ROTATE_SPEED = 2f; // 原版 Drill.rotateSpeed=2（度数，timeDrilled×2）喵

    public LaserDrillBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    // 多格建筑 BER 剔除边界：从锚点默认 1×1×1 扩到覆盖整组 3×3，防余光整颗消失（坑/碰撞箱.md §3）喵
    @Override
    public AABB getRenderBoundingBox(LaserDrillBlockEntity be) {
        BlockPos anchor = be.hasAnchor() ? be.getAnchor() : be.getBlockPos();
        int size = be.getSize();
        return new AABB(anchor.getX(), anchor.getY(), anchor.getZ(),
                anchor.getX() + size, anchor.getY() + size, anchor.getZ() + size).inflate(2.0);
    }

    @Override
    public void render(LaserDrillBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return; // 只在锚点格渲染一次，避免九格各转一个喵
        // 原版 Drawf.spinSprite(rotatorRegion, x, y, timeDrilled * rotateSpeed)：旋转钻头（角度为度数）喵
        float angle = be.getSpin() * ROTATE_SPEED;
        // 3×3 建筑中心在锚点格 +1.5,+1.5；贴顶面上方，rotator 与 top 各给不同 y 偏移防共面渗色（坑/BER渲染.md §3）喵
        drawQuad(pose, buffer, ROTATOR, 1.01f, 0.8f, angle);
        drawQuad(pose, buffer, TOP, 1.015f, 1.1f, 0f);
        // mine item：当前矿物矿团 tint 层（原版 drawMineItem：Draw.color(dominantItem.color)+Draw.rect(itemRegion)）喵
        if (be.getDominantItem() != null) {
            float[] c = BlastDrillBlockEntity.oreColor(be.getDominantItem());
            drawTintedQuad(pose, buffer, ITEM, 1.02f, 0.5f, 0f,
                    (int) (c[0] * 255f), (int) (c[1] * 255f), (int) (c[2] * 255f));
        }
    }

    private void drawQuad(PoseStack pose, MultiBufferSource buffer, ResourceLocation tex, float y, float half, float angle) {
        drawTintedQuad(pose, buffer, tex, y, half, angle, 255, 255, 255);
    }

    // 带 tint 色的顶面 quad：translate 到锚点格本地 (1.5,y,1.5)，绕 Y 旋转 angle 度，染 rgb（mine item 层）喵
    private void drawTintedQuad(PoseStack pose, MultiBufferSource buffer, ResourceLocation tex, float y, float half,
                                float angle, int r, int g, int b) {
        pose.pushPose();
        pose.translate(1.5f, y, 1.5f);
        if (angle != 0f) {
            // 原版 spinSprite 角度是度数（timeDrilled×rotateSpeed），必须用 rotationDegrees 而非 rotation（弧度）喵
            pose.mulPose(Axis.YP.rotationDegrees(angle));
        }
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(tex));
        var matrix = pose.last().pose();
        // 全亮 + NO_OVERLAY：透传 light 白天地表极暗会把深色像素压黑（坑/BER渲染.md §1，T6 钻机叶片同款坑）喵
        vc.addVertex(matrix, -half, 0f, -half).setUv(0f, 0f).setColor(r, g, b, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, -half, 0f, half).setUv(0f, 1f).setColor(r, g, b, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, half, 0f, half).setUv(1f, 1f).setColor(r, g, b, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, half, 0f, -half).setUv(1f, 0f).setColor(r, g, b, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        pose.popPose();
    }
}
