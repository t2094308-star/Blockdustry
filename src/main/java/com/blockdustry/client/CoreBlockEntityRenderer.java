package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.CoreBlockEntity;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import org.joml.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

// 核心渲染：整颗 3×3×3 正方体由锚点格 BER 绘制（base 贴 blockdustry:block/core，顶面叠队伍染色层）喵
// 背景：T8 曾把方块模型 element 的 to.y 改成 48，但 MC 方块模型元素坐标只允许 [-16,32]，
//       48 超出上限导致 9 个 core_*.json 全部 JsonParseException 加载失败，方块渲染成缺失模型（黑紫/隐形）喵
// 修复：方块模型改为无 element（合法、占位不可见），整颗立方体由 BER 用方块图集贴图绘制，规避坐标上限喵
// 关键：core_team.png 的透明内容在十字形（N/W/C/E/S），四角 NW/NE/SW/SE 全透明。
//       队伍染色层铺满整张 3×3 占地，四角透明透出 base，十字染上队伍色喵
public class CoreBlockEntityRenderer implements BlockEntityRenderer<CoreBlockEntity> {
    // base 贴图走方块图集（blockdustry:block/core 由 item 模型 models/block/core.json 引用，必在方块图集里）喵
    private static final ResourceLocation BASE_TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "block/core");
    // 队伍染色层走实体图集喵
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "textures/entity/core_team.png");

    // 双面方块渲染类型：方块图集贴图 + NO_CULL（关闭背面剔除），修核心立方体背面/贴脸视角消失喵
    // 坑：方块图集系渲染类型（solid/cutout/cutoutMipped/translucent）默认全部开 CULL，没有现成「方块图集+双面」类型，
    //     故用 RenderType.create 自建：BLOCK 顶点格式 + SOLID shader + 方块图集 + 全亮 LIGHTMAP，仅把 cull 关掉喵
    private static final RenderType CORE_BASE_NO_CULL = RenderType.create(
            "bd_core_base_no_cull",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            1536, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_SOLID_SHADER)
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    public CoreBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    // NeoForge IBlockEntityRendererExtension 覆写：渲染剔除边界从锚点格默认 1×1×1 扩到覆盖整颗 3×3×3 立方体喵
    // 坑（T13）：BER 的视锥剔除基于 BE 渲染边界框，核心 3×3×3 视觉由锚点格（1×1）BE 绘制，
    //       默认边界太小 → 玩家余光（视线偏转、立方体仅部分在视锥内）时整颗被 frustum 剔除消失喵
    // 修法：返回覆盖锚点正方向 0..3 立方体的世界坐标 AABB，并 inflate 2 格留余量，余光/边缘视角恒可见喵
    @Override
    public AABB getRenderBoundingBox(CoreBlockEntity be) {
        // 立方体只由锚点格绘制，按锚点算边界；单格（anchor==null）时锚点即自身喵
        BlockPos anchor = be.hasAnchor() ? be.getAnchor() : be.getBlockPos();
        BlockPos max = anchor.offset(3, 3, 3);
        return new AABB(anchor.getX(), anchor.getY(), anchor.getZ(),
                max.getX(), max.getY(), max.getZ()).inflate(2.0);
    }

    @Override
    public void render(CoreBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        if (!be.isAnchor()) return; // 只在锚点格画一次整颗立方体喵
        int teamColor = be.getTeam().getColor(); // ARGB，忠于原作 Team 构造喵
        int a = (teamColor >> 24) & 0xFF;
        int r = (teamColor >> 16) & 0xFF;
        int g = (teamColor >> 8) & 0xFF;
        int b = teamColor & 0xFF;

        pose.pushPose();
        var matrix = pose.last().pose();

        // —— base 层：3×3×3 正方体（锚点格本地坐标 0..3），每个外表面贴整张 96×96 core 纹理（3 格拼 1 张，2px/单位，无拉伸）喵
        // ⚠️ 全亮：CORE_BASE_NO_CULL 也是方块图集手绘 quad，透传的 light 白天 block=0 时采 (block=0,sky=15) 暗光贴图，
        //       把深灰 core 贴图压成黑（研究-炮管黑.md §2），必须像顶面队伍层一样硬编码 FULL_BRIGHT 喵
        // ⚠️ 双面：solid 默认开 CULL，立方体从背面/贴脸视角会被剔除消失，故用自建的无 cull 类型 CORE_BASE_NO_CULL 喵
        TextureAtlasSprite base = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(BASE_TEX);
        VertexConsumer vc = buffer.getBuffer(CORE_BASE_NO_CULL);

        // 顶面 y=3（朝 +y）
        quad(vc, matrix, base, 0, 3, 0, 0, 3, 3, 3, 3, 3, 3, 3, 0,
                0, 0, 0, 1, 1, 1, 1, 0, LightTexture.FULL_BRIGHT, 0, 1, 0);
        // 底面 y=0（朝 -y，通常埋在地里）喵
        quad(vc, matrix, base, 0, 0, 0, 3, 0, 0, 3, 0, 3, 0, 0, 3,
                0, 0, 1, 0, 1, 1, 0, 1, LightTexture.FULL_BRIGHT, 0, -1, 0);
        // 北面 z=0（朝 -z）
        quad(vc, matrix, base, 0, 3, 0, 3, 3, 0, 3, 0, 0, 0, 0, 0,
                0, 0, 1, 0, 1, 1, 0, 1, LightTexture.FULL_BRIGHT, 0, 0, -1);
        // 南面 z=3（朝 +z）
        quad(vc, matrix, base, 3, 3, 3, 0, 3, 3, 0, 0, 3, 3, 0, 3,
                0, 0, 1, 0, 1, 1, 0, 1, LightTexture.FULL_BRIGHT, 0, 0, 1);
        // 西面 x=0（朝 -x）
        quad(vc, matrix, base, 0, 3, 3, 0, 3, 0, 0, 0, 0, 0, 0, 3,
                1, 0, 0, 0, 0, 1, 1, 1, LightTexture.FULL_BRIGHT, -1, 0, 0);
        // 东面 x=3（朝 +x）
        quad(vc, matrix, base, 3, 3, 0, 3, 3, 3, 3, 0, 3, 3, 0, 0,
                1, 0, 0, 0, 0, 1, 1, 1, LightTexture.FULL_BRIGHT, 1, 0, 0);

        // —— 队伍染色层：顶面 y+3.001（原 1.001，防 z-fighting）喵
        // 全亮固定光照，NO_OVERLAY 避免越界采样染黑（参考 CombustionGenerator 渲染器）喵
        VertexConsumer tv = buffer.getBuffer(RenderType.entityTranslucent(TEX));
        tv.addVertex(matrix, 0f, 3.001f, 0f).setUv(0f, 0f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        tv.addVertex(matrix, 0f, 3.001f, 3f).setUv(0f, 1f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        tv.addVertex(matrix, 3f, 3.001f, 3f).setUv(1f, 1f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        tv.addVertex(matrix, 3f, 3.001f, 0f).setUv(1f, 0f).setColor(r, g, b, a).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);

        pose.popPose();
    }

    // 画一个四边形：4 顶点坐标 + 4 归一化 UV + 光照 + 法线（法线兼作光照朝向与背面剔除判定）喵
    private void quad(VertexConsumer vc, Matrix4f matrix, TextureAtlasSprite sprite,
                      float x0, float y0, float z0, float x1, float y1, float z1,
                      float x2, float y2, float z2, float x3, float y3, float z3,
                      float u0, float v0, float u1, float v1, float u2, float v2, float u3, float v3,
                      int light, float nx, float ny, float nz) {
        vc.addVertex(matrix, x0, y0, z0).setUv(sprite.getU(u0), sprite.getV(v0)).setColor(255, 255, 255, 255)
                .setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(nx, ny, nz);
        vc.addVertex(matrix, x1, y1, z1).setUv(sprite.getU(u1), sprite.getV(v1)).setColor(255, 255, 255, 255)
                .setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(nx, ny, nz);
        vc.addVertex(matrix, x2, y2, z2).setUv(sprite.getU(u2), sprite.getV(v2)).setColor(255, 255, 255, 255)
                .setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(nx, ny, nz);
        vc.addVertex(matrix, x3, y3, z3).setUv(sprite.getU(u3), sprite.getV(v3)).setColor(255, 255, 255, 255)
                .setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(nx, ny, nz);
    }
}
