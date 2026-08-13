# 坑：BER 渲染 / 光照 / overlay 喵

## 1. 手绘 quad 必须全亮 + NO_OVERLAY 喵
- `RenderType.entityCutout/entityTranslucent` 手绘 quad 顶点：`setLight(0xF000F0)`（FULL_BRIGHT）+ `setOverlay(OverlayTexture.NO_OVERLAY)` 喵
- **坑**：透传 BER 的 `light` 参数，白天地表 blockLight=0 时极暗，entity shader 采样暗光贴图把深色像素压黑（炮管黑、钻机叶片暗、核心 base 层黑的根因）。mod 内全部渲染器须全亮喵（见 `炮管黑.md`）
- **坑**：`RenderType.solid()`（方块图集）在 BER 手绘同样要 FULL_BRIGHT——solid 顶点格式含 UV2(light)，透传一样暗喵

## 2. setOverlay(0xFFFFFF) 越界采样染黑 喵
- `setOverlay(0xFFFFFF)` 不是「无 overlay」——解码成越界 UV 采样到透明黑把整片染黑，必须用 `OverlayTexture.NO_OVERLAY`（见 `PowerNode激光黑色.md`）喵

## 3. 多 quad 叠画共面要 y 偏移 喵
- 多张 quad 同一水平面（如炮塔转盘+双炮管）会共面，entityCutout 写深度 + GPU 浮点噪声重叠区互相渗色成黑阴影；给每张 quad 不同微小 y（如 0/0.004/0.008）消除（见 `炮塔黑色阴影.md`）喵

## 4. 交错使用多个 RenderType 会崩溃 喵
- 同一帧渲染不要交错多个 RenderType（lines + entityTranslucent 等），sorted 类型会 flush 前面的 buffer 导致 `BufferBuilder: Not building!` 崩溃；整段只用一种喵

## 5. 其它 vertex/渲染坑 喵
- `RenderType.lines()` vertex format 含 Normal，画线须 `setNormal(...)`，否则崩「Missing elements in vertex: Normal」喵
- `RenderType.lightning()` 单面有 cull，绕序/朝向不对整面不可见（细到看不见）；不如 entityTranslucent 双面稳喵
- 激光/线框从 LevelRenderer 拿的 pose 已含 `translate(blockPos-cam)`，手绘世界坐标顶点须先 `pushPose + setIdentity` 防双重偏移喵
- 方块模型 UV 是 16×16 模型空间（见 `方块模型.md`），改大会采样 atlas 里其他方块贴图（杂乱+卡顿）喵
