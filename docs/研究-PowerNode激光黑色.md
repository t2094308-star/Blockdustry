# 研究：电力节点激光渲染成黑色（entityTranslucent 全黑）喵

## 结论摘要喵

- 根因：`PowerNodeBlockEntityRenderer.vertex()` 里用了 `.setOverlay(0xFFFFFF)` 喵。`0xFFFFFF` 不是「无 overlay」的正确常量；它会被拆成 UV1=(65535, 255)，对 16×16 的 overlay 纹理做 `texelFetch(Sampler1, (65535,255), 0)` 越界采样，返回**透明黑 (0,0,0,0)**，片元着色器再 `color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a)`（alpha=0 时输出 overlay 的黑色分量）把光束颜色整体替换成黑色喵
- 修复：把 `.setOverlay(0xFFFFFF)` 换成 `.setOverlay(OverlayTexture.NO_OVERLAY)`（= `pack(0,10)` = `0x000A0000`，采到 overlay 纹理第 10 行第 0 列的全白「无 overlay」像素），与 `BlockdustryBulletRenderer` 的写法完全一致喵
- 与纹理、灯光、顶点颜色均无关：自建白纹理确认是纯白 RGBA(255,255,255,255)；`setLight(0xF000F0)` 就是 `LightTexture.FULL_BRIGHT`（=15728880）满亮；颜色 lerp 计算在 sat∈[0,1] 时远大于 0，不会是黑喵
- 对比成功经验：`BlockdustryBulletRenderer` 用 `RenderType.entityTranslucent` + `setOverlay(OverlayTexture.NO_OVERLAY)` 正常染色，证明这个 RenderType 本身能染色，唯一差异就是激光的 overlay 常量写错喵

---

## 1. 现象与排查排除项喵

现象：用 `RenderType.entityTranslucent(白纹理)` 画实心 quad 光束后，颜色全黑；之前用 `RenderType.lines()` 颜色正常喵。

逐项排除喵：

| 排查项 | 结论喵 |
|---|---|
| 白纹理路径 | 自建 `assets/blockdustry/textures/misc/white.png` 解析为 1×1 RGBA(255,255,255,255) 纯白，且尝试过原版 `minecraft:textures/misc/white.png` 一样黑，排除纹理喵 |
| 顶点颜色 | `setColor(int,int,int,int)` 在 `VertexConsumer` 里就是最终 byte 值（float 重载只是乘 255 再转 int），ri/gi/bi 由 `lerp(1f, AMBER, t)` 算出，sat∈[0,1] 时约 (252,217,124)，不为 0 喵 |
| 光照 | `setLight(0xF000F0)` = `LightTexture.FULL_BRIGHT` = 15728880，`UV2/16=(15,15)` 采到 16×16 光贴图最亮白像素，光贴图乘法是恒等喵 |
| 方向光 | `light.glsl` 的 `minecraft_mix_light` 有 `MINECRAFT_AMBIENT_LIGHT(0.4)` 保底，且 `Lighting.DIFFUSE_LIGHT_0/1` 都有正 Y 分量，法线 (0,1,0) 时 `lightAccum=1.0` 全亮，不会黑喵 |
| 几何/剔除 | `entityTranslucent` 带 `NO_CULL`，双面可见，光束位置正确（能看到黑色光束），排除喵 |
| 雾 | 激光顶点已 `subtract(cam)`，`Position`=世界坐标-相机，`fog_distance` 就是到相机距离，雾正确喵 |

---

## 2. 根因：overlay 越界采样把颜色压成黑色喵

### 2.1 关键文件与 shader 源码喵

文件：`D:\Blockdustry\仓库\src\main\java\com\blockdustry\client\PowerNodeBlockEntityRenderer.java` 第 108-113 行喵：

```java
vc.addVertex(matrix, (float) p.x, (float) p.y, (float) p.z)
        .setColor(r, g, bl, alpha)
        .setUv(0f, 0f)
        .setOverlay(0xFFFFFF)          // ← 问题所在喵
        .setLight(0xF000F0)
        .setNormal(0f, 1f, 0f);
```

`RenderType.entityTranslucent()` 的定义（`RenderType.java` 第 153-163 行）明确带有 `setOverlayState(OVERLAY)` 与 `setLightmapState(LIGHTMAP)`，即 entityTranslucent 着色器**一定会采样 overlay 纹理**喵：

```java
// RenderType.java ENTITY_TRANSLUCENT 喵
RenderType.CompositeState.builder()
    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
    .setTextureState(new TextureStateShard(location, false, false))
    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
    .setCullState(NO_CULL)
    .setLightmapState(LIGHTMAP)   // 绑定 16×16 光贴图到 Sampler2 喵
    .setOverlayState(OVERLAY)     // 绑定 16×16 overlay 贴图到 Sampler1 喵
    .createCompositeState(false)
```

`rendertype_entity_translucent.vsh`（客户端 jar 内）顶点着色器喵：

```glsl
uniform sampler2D Sampler1;   // overlay 喵
uniform sampler2D Sampler2;   // lightmap 喵
...
vertexColor  = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color);
lightMapColor = texelFetch(Sampler2, UV2 / 16, 0);   // 光贴图采样喵
overlayColor  = texelFetch(Sampler1, UV1, 0);        // overlay 采样喵
texCoord0     = UV0;
```

`rendertype_entity_translucent.fsh` 片元着色器喵：

```glsl
vec4 color = texture(Sampler0, texCoord0);
if (color.a < 0.1) { discard; }
color *= vertexColor * ColorModulator;
color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);  // ← 关键：overlay.a 越低越显示 overlay 自身颜色喵
color *= lightMapColor;
fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
```

### 2.2 数值推导喵

`setOverlay` 的实现（`VertexConsumer.java` 第 67-69 行）喵：

```java
default VertexConsumer setOverlay(int packedOverlay) {
    return this.setUv1(packedOverlay & 65535, packedOverlay >> 16 & 65535);
}
```

传入 `0xFFFFFF` 时喵：

```
u = 0xFFFFFF & 0xFFFF  = 0xFFFF  = 65535
v = 0xFFFFFF >> 16 & 0xFFFF = 0xFF = 255
→ UV1 = (65535, 255)
```

`OverlayTexture`（`OverlayTexture.java`）是 `new DynamicTexture(16, 16, false)`，即 **16×16** 纹理喵。`texelFetch(Sampler1, ivec2(65535, 255), 0)` 坐标远超边界，GLSL 规范定义为未定义行为，在该用户 GPU 上返回**透明黑 (0,0,0,0)** 喵。

片元着色器执行喵（GLSL `mix(x,y,a)=x*(1-a)+y*a`）喵：

```
color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a)
          = overlayColor.rgb * (1 - 0) + color.rgb * 0     // overlay.a = 0 喵
          = overlayColor.rgb                               // = 黑喵
```

越界采样得到的 `overlayColor = (0,0,0,0)`：alpha=0 让 `mix` 完全输出 overlay 的黑色分量，**无论光束原本什么颜色，都被替换成黑色**喵。这与「换白纹理都黑」的现象完全吻合喵。

### 2.3 正确的「无 overlay」常量喵

`OverlayTexture.java` 第 14 行喵：

```java
public static final int NO_WHITE_U = 0;
public static final int RED_OVERLAY_V = 3;
public static final int WHITE_OVERLAY_V = 10;
public static final int NO_OVERLAY = pack(0, 10);   // = 0 | (10 << 16) = 0x000A0000 喵
```

- `NO_OVERLAY = pack(0, 10)` → `setOverlay(NO_OVERLAY)` → UV1=(0, 10)，在 16×16 纹理内喵
- overlay 纹理第 10 行（i=10 ≥ 8）第 0 列像素：`k=(int)((1.0F - 0/15*0.75)*255)=255`，写入 RGBA(255,255,255,255) 纯白喵
- 片元着色器 `mix(white, color.rgb, 1.0) = color.rgb`，颜色保持不变喵

---

## 3. 为什么灯光、纹理、颜色都不是凶手喵

- **纹理**：`minecraft_mix_light` 和 overlay 都在光贴图乘法之后才影响最终色，纹理只提供 `Sampler0` 的白底；白纹理 × 顶点色 = 顶点色，纹理本身不可能把颜色变黑喵
- **灯光**：`LightTexture` 是 16×16，`setLight(0xF000F0)` → `setUv2(240,240)` → `texelFetch(Sampler2, (240/16, 240/16)=(15,15))` 采到最亮白像素；且 `Lighting.setupLevel()` 用的 `DIFFUSE_LIGHT_0/1 = normalize(±0.2, 1, ∓0.7)` 对法线 (0,1,0) 的 dot 都为正，`lightAccum = min(1, (1+1)*0.6+0.4)=1.0` 全亮喵
- **顶点颜色**：`setColor(int)` 是 `VertexConsumer` 的最终写入（`setColor(float)` 只是乘 255 转 int），激光 ri/gi/bi 约 (252,217,124)，alpha=230，绝不为 0 喵

---

## 4. 对比 BlockdustryBulletRenderer 的成功染色喵

`D:\Blockdustry\仓库\src\main\java\com\blockdustry\client\BlockdustryBulletRenderer.java` 同样用 `RenderType.entityTranslucent` 画染色 quad，且**正常显示彩色**喵：

```java
// BlockdustryBulletRenderer.java 第 60 行喵
vc.addVertex(matrix, (float) a.x, y, (float) a.z)
        .setUv(0f, 0f).setColor(r, g, bl, 1f)
        .setOverlay(OverlayTexture.NO_OVERLAY)   // ← 正确写法喵
        .setLight(light).setNormal(0f, 1f, 0f);
```

两者逐项对比喵：

| 项目 | BlockdustryBulletRenderer（正常）喵 | PowerNodeBlockEntityRenderer（黑色）喵 |
|---|---|---|
| RenderType | `entityTranslucent` 喵 | `entityTranslucent` 喵 |
| 纹理 | bullet.png（白剪影）喵 | white.png（纯白）喵 |
| 顶点颜色 | float 0-1 喵 | int 0-255（等效）喵 |
| 法线 | (0,1,0) 喵 | (0,1,0) 喵 |
| **overlay** | **`OverlayTexture.NO_OVERLAY`**（界内，白色）喵 | **`0xFFFFFF`**（越界，黑色）喵 |
| light | 动态喵 | `0xF000F0` 满亮（等效更亮）喵 |

唯一实质性差异就是 **overlay 常量**喵：炮弹用 `OverlayTexture.NO_OVERLAY` 采到全白「无 overlay」像素所以颜色保留；激光用 `0xFFFFFF` 越界采样到透明黑 (0,0,0,0) 所以全黑喵。

`DrillBlockEntityRenderer`、`TurretBlockEntityRenderer` 则直接透传 BER 的 `overlay` 参数（`BlockEntityRenderDispatcher` 传的就是 `OverlayTexture.NO_OVERLAY`），也一直正常喵。

---

## 5. 修复方案喵

最小改动：把 `PowerNodeBlockEntityRenderer.java` 的 `.setOverlay(0xFFFFFF)` 换成 `.setOverlay(OverlayTexture.NO_OVERLAY)`，并补 import 喵。

修改后的 `vertex()` 喵：

```java
import net.minecraft.client.renderer.texture.OverlayTexture;

private static void vertex(VertexConsumer vc, Matrix4f matrix, Vec3 p,
                           int r, int g, int bl, int alpha) {
    vc.addVertex(matrix, (float) p.x, (float) p.y, (float) p.z)
            .setColor(r, g, bl, alpha)
            .setUv(0f, 0f)
            .setOverlay(OverlayTexture.NO_OVERLAY) // 正确：采全白「无 overlay」像素，颜色不被覆盖喵
            .setLight(0xF000F0)                    // 满亮（LightTexture.FULL_BRIGHT）喵
            .setNormal(0f, 1f, 0f);                // 朝上：方向光全亮喵
}
```

其余代码（`pose.setIdentity()` + 世界坐标减去相机、billboard 法线、双面 quad、白纹理染色）均无需改动喵。

### 可选说明喵

- `0xFFFFFF` 来自老习惯想当然当成「无 overlay」，但 `OverlayTexture.NO_OVERLAY` 的正确值是 `pack(0,10)=0x000A0000`，两者完全不同喵
- 若想省一个 import，也可以写 `setOverlay(655360)`，但用常量可读性好且与 `BlockdustryBulletRenderer` 一致喵

---

## 6. 参考文件喵

- `D:\Blockdustry\仓库\src\main\java\com\blockdustry\client\PowerNodeBlockEntityRenderer.java`（待修复，第 111 行）喵
- `D:\Blockdustry\仓库\src\main\java\com\blockdustry\client\BlockdustryBulletRenderer.java`（正确写法对照）喵
- `D:\Blockdustry\仓库\build\neoForm\neoFormJoined1.21.1-20240808.144430\steps\transformSource\transformed\net\minecraft\client\renderer\RenderType.java`（ENTITY_TRANSLUCENT 带 OVERLAY/LIGHTMAP 状态）喵
- 同路径 `...\com\mojang\blaze3d\vertex\VertexConsumer.java`（setOverlay/setColor/setLight 实现）喵
- 同路径 `...\net\minecraft\client\renderer\texture\OverlayTexture.java`（NO_OVERLAY = pack(0,10)，16×16 纹理）喵
- 同路径 `...\net\minecraft\client\renderer\LightTexture.java`（FULL_BRIGHT = 0xF000F0，16×16 光贴图）喵
- 同路径 `...\com\mojang\blaze3d\platform\Lighting.java`（DIFFUSE_LIGHT_0/1 灯方向）喵
- 客户端 jar `D:\Blockdustry\仓库\build\jars\extra\client\1.21.1\client-extra.jar` 内 `assets/minecraft/shaders/core/rendertype_entity_translucent.vsh/.fsh` 与 `shaders/include/light.glsl`、`fog.glsl` 喵
- `D:\Blockdustry\仓库\src\main\resources\assets\blockdustry\textures\misc\white.png`（已确认纯白，非问题源）喵
