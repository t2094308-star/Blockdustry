package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.entities.FlakBulletEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

// Mindustry flak 霰弹渲染：单层亮黄 quad（flak 弹色）沿运动方向，同炮弹渲染器画法喵
public class FlakBulletRenderer extends EntityRenderer<FlakBulletEntity> {
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/entity/bullet.png");
    // Mindustry flak 弹亮黄喵
    private static final int FLAK_COLOR = 0xFFE08A;

    public FlakBulletRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(FlakBulletEntity entity, float yaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffer, int light) {
        Vec3 vel = entity.getDeltaMovement();
        Vec3 longAxis = new Vec3(vel.x, 0, vel.z).normalize();
        Vec3 wide = new Vec3(-longAxis.z, 0, longAxis.x);
        if (longAxis.lengthSqr() < 1e-6) {
            longAxis = new Vec3(1, 0, 0);
            wide = new Vec3(0, 0, 1);
        }
        drawQuad(pose, buffer.getBuffer(RenderType.entityTranslucent(TEX)),
                longAxis, wide, 0.10f, 0.30f, 0.20f, FLAK_COLOR, light);
    }

    // 水平 quad：中心在实体上方 y，长轴 len、宽轴 wid，染指定颜色（全亮+NO_OVERLAY）喵
    private void drawQuad(PoseStack pose, VertexConsumer vc, Vec3 longAxis, Vec3 wide,
                          float y, float len, float wid, int rgb, int light) {
        Vec3 a = longAxis.scale(len).add(wide.scale(wid));
        Vec3 b = longAxis.scale(len).subtract(wide.scale(wid));
        Vec3 c = longAxis.scale(-len).subtract(wide.scale(wid));
        Vec3 d = longAxis.scale(-len).add(wide.scale(wid));
        var matrix = pose.last().pose();
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float bl = (rgb & 0xFF) / 255f;
        vc.addVertex(matrix, (float) a.x, y, (float) a.z).setUv(0f, 0f).setColor(r, g, bl, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, (float) b.x, y, (float) b.z).setUv(0f, 1f).setColor(r, g, bl, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, (float) c.x, y, (float) c.z).setUv(1f, 1f).setColor(r, g, bl, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, (float) d.x, y, (float) d.z).setUv(1f, 0f).setColor(r, g, bl, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
    }

    @Override
    public ResourceLocation getTextureLocation(FlakBulletEntity entity) {
        return TEX;
    }
}
