package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.PneumaticDrillBlockEntity;

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
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;

import org.joml.Matrix4f;

// 气动钻头渲染：锚点格上方按原版 Drill.draw() 顺序叠加三张原版 PNG——
//   1) rotator（钻头，绕 Y 旋转，速度受 warmup 驱动，停转时停）喵
//   2) top（顶盖，静态，画在 rotator 上方）喵
//   3) item（正在开采的矿石，drill-item-2 原版贴图按矿色 tint，画最上层）喵
// 模板 DrillBlockEntityRenderer（mechanical-drill），仅把单 rotator 扩展为三层并接入 warmup 喵
public class PneumaticDrillBlockEntityRenderer implements BlockEntityRenderer<PneumaticDrillBlockEntity> {
    private static final ResourceLocation ROTATOR =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/pneumatic_drill_rotator.png");
    private static final ResourceLocation TOP =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/pneumatic_drill_top.png");
    private static final ResourceLocation ITEM =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/block/pneumatic_drill_item.png");

    public PneumaticDrillBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    // 多格建筑 BER 剔除边界：从锚点默认 1×1×1 扩到覆盖整组 2×2，防余光整颗消失（坑/碰撞箱.md §3）喵
    @Override
    public AABB getRenderBoundingBox(PneumaticDrillBlockEntity be) {
        BlockPos anchor = be.hasAnchor() ? be.getAnchor() : be.getBlockPos();
        int size = be.getSize();
        return new AABB(anchor.getX(), anchor.getY(), anchor.getZ(),
                anchor.getX() + size, anchor.getY() + size, anchor.getZ() + size).inflate(2.0);
    }

    @Override
    public void render(PneumaticDrillBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        // 只在锚点格渲染一次，避免四格各转一个喵
        if (!be.isAnchor()) return;
        float warmup = be.getWarmup();
        float angle = (be.getLevel().getGameTime() + partialTick) * 0.3f * warmup;
        pose.pushPose();
        // 锚点格是 2×2 建筑左下角，中心在 +1,+1；贴顶面上方避免 z-fighting 喵
        pose.translate(1.0f, 1.02f, 1.0f);

        // 1) rotator：整幅 2×2 平面，绕 Y 旋转（全亮 + NO_OVERLAY，坑-BER渲染.md）喵
        pose.pushPose();
        pose.mulPose(Axis.YP.rotation(angle));
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(ROTATOR));
        addQuad(vc, pose.last().pose(), 1.0f, 255, 255, 255);
        pose.popPose();

        // 2) top：静态整幅 2×2，略高于 rotator 防共面渗色喵
        pose.translate(0f, 0.004f, 0f);
        VertexConsumer vcTop = buffer.getBuffer(RenderType.entityCutout(TOP));
        addQuad(vcTop, pose.last().pose(), 1.0f, 255, 255, 255);

        // 3) item：1×1 矿团，按矿色 tint，画最上层（Mindustry itemRegion tinted by dominantItem.color）喵
        Item ore = be.getMinedOre();
        if (ore != null) {
            pose.translate(0f, 0.004f, 0f);
            int[] c = PneumaticDrillBlockEntity.mineColor(ore);
            VertexConsumer vcItem = buffer.getBuffer(RenderType.entityCutout(ITEM));
            addQuad(vcItem, pose.last().pose(), 0.5f, c[0], c[1], c[2]);
        }
        pose.popPose();
    }

    // XZ 平面 quad（y=0），half 为半边长，uv 覆盖整张贴图，法线朝上。
    // 全亮 + NO_OVERLAY：透传 light 白天极暗会压黑深色像素（坑-BER渲染.md，T6 同款修复）喵
    private static void addQuad(VertexConsumer vc, Matrix4f matrix, float half, int r, int g, int b) {
        vc.addVertex(matrix, -half, 0f, -half).setUv(0f, 0f).setColor(r, g, b, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, -half, 0f, half).setUv(0f, 1f).setColor(r, g, b, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, half, 0f, half).setUv(1f, 1f).setColor(r, g, b, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, half, 0f, -half).setUv(1f, 0f).setColor(r, g, b, 255).setLight(0xF000F0).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0f, 1f, 0f);
    }
}
