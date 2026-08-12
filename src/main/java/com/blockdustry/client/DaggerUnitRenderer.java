package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.entities.DaggerUnitEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

// Dagger 地面单位渲染：EntityRenderer + 水平平面 quad 铺 dagger.png 精灵贴图，
// quad 前方（贴图顶部）对准移动方向，写法参考 BlockdustryBulletRenderer 喵
public class DaggerUnitRenderer extends EntityRenderer<DaggerUnitEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/entity/dagger.png");
    // 半长半宽（单位约 0.7 格）；y 略抬高避免与地面 z-fighting 喵
    private static final float HALF_SIZE = 0.35f;
    private static final float Y = 0.02f;

    public DaggerUnitRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(DaggerUnitEntity entity, float yaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffer, int light) {
        // 移动方向水平投影做 quad 前方；静止时退回默认朝东 (+X) 喵
        Vec3 vel = entity.getDeltaMovement();
        Vec3 forward = new Vec3(vel.x, 0, vel.z).normalize();
        if (forward.lengthSqr() < 1e-6) {
            forward = new Vec3(1, 0, 0);
        }
        // 与 forward 垂直的水平侧向轴（bullet renderer 同款）喵
        Vec3 right = new Vec3(-forward.z, 0, forward.x);
        // 四角顺序：前左(uv 0,0) → 后左(uv 0,1) → 后右(uv 1,1) → 前右(uv 1,0)，
        // 这样贴图顶部(v=0)对准移动方向、贴图右侧(u=1)在 quad 右侧，法线朝上（顶部可见）喵
        Vec3 p1 = forward.scale(HALF_SIZE).subtract(right.scale(HALF_SIZE));
        Vec3 p2 = forward.scale(-HALF_SIZE).subtract(right.scale(HALF_SIZE));
        Vec3 p3 = forward.scale(-HALF_SIZE).add(right.scale(HALF_SIZE));
        Vec3 p4 = forward.scale(HALF_SIZE).add(right.scale(HALF_SIZE));
        var matrix = pose.last().pose();
        // ⚠️ setLight 固定 0xF000F0 全亮 + NO_OVERLAY：透传 light 白天 blockLight=0 时极暗，
        // 深色贴图 × 暗光 ≈ 纯黑（研究-炮管黑.md 踩过的坑，必须全亮）喵
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        vc.addVertex(matrix, (float) p1.x, Y, (float) p1.z).setUv(0f, 0f).setColor(255, 255, 255, 255).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, (float) p2.x, Y, (float) p2.z).setUv(0f, 1f).setColor(255, 255, 255, 255).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, (float) p3.x, Y, (float) p3.z).setUv(1f, 1f).setColor(255, 255, 255, 255).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, (float) p4.x, Y, (float) p4.z).setUv(1f, 0f).setColor(255, 255, 255, 255).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
    }

    @Override
    public ResourceLocation getTextureLocation(DaggerUnitEntity entity) {
        return TEXTURE;
    }
}
