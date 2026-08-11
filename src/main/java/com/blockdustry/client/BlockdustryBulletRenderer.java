package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.entities.BlockdustryBulletEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

// Mindustry 炮弹渲染：白色剪影贴图 + 双层染色（back 暗色垫底 + front 亮色），胶囊沿运动方向喵
public class BlockdustryBulletRenderer extends EntityRenderer<BlockdustryBulletEntity> {
    private static final ResourceLocation FRONT =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/entity/bullet.png");
    private static final ResourceLocation BACK =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/entity/bullet_back.png");
    // duo 铜弹颜色：front=#eac1a8 暖米黄，back=#d39169 橙棕喵
    private static final int FRONT_COLOR = 0xEAC1A8;
    private static final int BACK_COLOR = 0xD39169;

    public BlockdustryBulletRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(BlockdustryBulletEntity entity, float yaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffer, int light) {
        // 长轴沿运动方向（水平投影），宽轴垂直喵
        Vec3 vel = entity.getDeltaMovement();
        Vec3 longAxis = new Vec3(vel.x, 0, vel.z).normalize();
        Vec3 wide = new Vec3(-longAxis.z, 0, longAxis.x);
        if (longAxis.lengthSqr() < 1e-6) {
            longAxis = new Vec3(1, 0, 0);
            wide = new Vec3(0, 0, 1);
        }
        // back 大层垫底（y 略低），front 小层（y 略高）避免 z-fighting 喵
        drawQuad(pose, buffer.getBuffer(RenderType.entityTranslucent(BACK)),
                longAxis, wide, 0.08f, 0.44f, 0.30f, BACK_COLOR, light);
        drawQuad(pose, buffer.getBuffer(RenderType.entityTranslucent(FRONT)),
                longAxis, wide, 0.12f, 0.36f, 0.24f, FRONT_COLOR, light);
    }

    // 画一个水平 quad：中心在实体上方 y，长轴长 len、宽轴宽 wid，染指定颜色喵
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
        vc.addVertex(matrix, (float) a.x, y, (float) a.z).setUv(0f, 0f).setColor(r, g, bl, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, (float) b.x, y, (float) b.z).setUv(0f, 1f).setColor(r, g, bl, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, (float) c.x, y, (float) c.z).setUv(1f, 1f).setColor(r, g, bl, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, (float) d.x, y, (float) d.z).setUv(1f, 0f).setColor(r, g, bl, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
    }

    @Override
    public ResourceLocation getTextureLocation(BlockdustryBulletEntity entity) {
        return FRONT;
    }
}
