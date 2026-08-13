# T8c 核心基座黑 + 无碰撞箱 + 分裂炮基座黑 修复喵

## 现象喵

T8b 修复核心为「模型置空 + BER 画 3×3×3 立方体」后，用户反馈喵：
1. **核心基座变黑**：3×3×3 立方体的 base 层（方块图集 `blockdustry:block/core` 深灰纹理）整体发黑喵。
2. **核心没有实体碰撞箱**：视觉 3 格高，但玩家可走进/穿过 y=1、y=2 那两格的视觉区域喵。
3. **分裂炮（scatter）基座变黑**：2×2 四象限模型的顶面/侧面发黑喵。

## 根因喵

### 1. 核心 base 层黑 —— BER 手绘 quad 透传 light 太暗（同「炮管黑」根因）喵

`CoreBlockEntityRenderer` 的 base 层用 `RenderType.solid()` 画 6 个外表面，顶点 `setLight(light)` 透传 BER 的 `light` 参数喵。
`light` 来自 `LevelRenderer.getLightColor(blockpos)` = `sky<<20 | block<<4`，白天地表 **block=0、sky=15 → 0xF00000**，光贴图采 **(block=0, sky=15)** 那个格子喵。
按「研究-炮管黑.md」§2 的推算，该格亮度只有满亮的 **三到四成**，深灰 core 纹理 (150,155,165) × 暗光 ≈ 近黑喵。
顶面队伍染色层早已硬编码 `0xF000F0`（全亮）所以不黑，唯独 base 层漏改，导致整个立方体侧面/顶面基座发黑喵。

### 2. 核心无碰撞箱 —— 模型置空 + 基类 getShape 只返回 1×1×1 喵

T8b 把 9 个 `core_{corner}.json` 置空（无 element），方块本体不渲染，但 `BlockdustryBuildingBlock` **没有覆写 `getShape`/`getCollisionShape`**，基类 `Block` 默认每格返回 1×1×1 喵。
核心实际只占 y=0 一层 3×3 方块，视觉 3 高由 BER 画出来，**y=1、y=2 层没有方块**，自然没有碰撞箱 → 玩家直接穿模喵（坑文档 §3「模型置空的后果」）喵。

### 3. 分裂炮基座黑 —— 两个贴图问题叠加喵

- **顶面 `scatter_base.png` 是透明底**（64×64 只有 1218/4096 ≈ 30% 不透明，画的是两条「腿」），方块模型是 solid 渲染，**透明像素的 RGB=(0,0,0) 会直接显示成黑色**（坑文档 §6「透明底贴图在 solid 下变黑」），顶面大片黑喵。
- **侧面 `drill_side.png` 被工作区改动成近黑**（avgRGB (62,63,68)，从 HEAD 的 (112,109,115) 中灰调暗），scatter 模型侧面用 `#side`=drill_side，整个侧面近黑喵。

## 修复喵

### 改动 1：`client/CoreBlockEntityRenderer.java` —— base 层全亮喵

把 6 个 base 层 quad 的 `setLight(light)` 改为 `setLight(LightTexture.FULL_BRIGHT)`（=0xF000F0），与顶面队伍层、燃烧发电机、电力节点、炮塔等 mod 内已修复的渲染器对齐喵。
`solid()` 是块体渲染，顶点格式含 UV2(light)，FULL_BRIGHT → UV2=(15,15) 采光贴图最亮白像素，base 层不再被暗光压黑喵。
`overlay` 参数保留（接口签名），solid 格式无 overlay 元素，setOverlay 无副作用喵。

### 改动 2：`building/BlockdustryBuildingBlock.java` —— 多格建筑整组碰撞箱 + 高度喵

- 新增 `private final int height;` 字段，保留 3 参构造（默认 `height=1`），新增 4 参构造 `(properties, entityType, size, height)` 喵。
- 新增 `getHeight()` 访问器喵。
- 覆写 `getShape`：`size>1` 时用 blockstate 的 `corner` 属性反推该格在组内的 dx/dz，返回**整组包围盒** `Block.box(-dx*16, 0, -dz*16, (size-dx)*16, height*16, (size-dz)*16)` 喵。
  每格都返回覆盖整组的 AABB（非锚点格用负偏移），世界坐标上所有格重合为同一个大 AABB，等价于整座建筑一个实心碰撞体，且无需依赖锚点格加载喵。
  核心 height=3 → 3×3×3 实心碰撞；drill/scatter/graphite_press 等 2×2 与 unit_factory 3×3 → 整组 2×2×1 / 3×3×1 实心碰撞喵。
- 新增 `cornerDx/cornerDz`（`cornerFor` 的互逆映射），支持 2×2（四象限）与 3×3（九宫格）喵。

### 改动 3：`building/BlockdustryBlocks.java` —— 核心高度传 3 喵

`coreBlock()` 改用 4 参构造 `(properties, entityType, 3, 3)`，核心碰撞 3 格高喵。其余建筑保持 3 参（高 1）喵。

### 改动 4：贴图修复喵

- `textures/block/drill_side.png`：`git checkout HEAD --` 恢复为中灰 (112,109,115)，scatter 侧面不再近黑（drill 侧面也同步恢复正常）喵。
- `textures/block/scatter_base.png`：用 PIL 把 2878 个透明像素填充为不透明深灰 (88,89,95,255)，solid 渲染不再出现黑色透明区；保留原「腿」的不透明像素（亮部 112 仍可见），基座整体呈深灰金属色喵。

## 验证喵

- 75 个资源 JSON 全部 `json.load` 合法喵。
- `./gradlew compileJava` BUILD SUCCESSFUL（改动后增量编译通过）喵。
- `./gradlew processResources` 后 src 与 build 的 `drill_side.png`/`scatter_base.png`/`core.png` 逐字节一致，build `scatter_base.png` 全不透明、`drill_side.png` avgRGB=(112,109,115) 喵。
- 放置核心预期：3×3×3 灰蓝立方体 base 层全亮不再黑，顶面队伍色十字正常，整颗立方体有实心碰撞箱（站得上去、穿不过去）喵。
- 放置分裂炮预期：基座顶面深灰实心、侧面中灰，转盘 scatter_top 全亮，不再整座发黑喵。

## 坑补充（已并入 docs/研究-渲染与模型坑.md）喵

- 坑 §1 追加：**`RenderType.solid()` 手绘 quad 同样要 FULL_BRIGHT**（不止 entityCutout/entityTranslucent）；solid 顶点格式含 UV2(light)，透传 light 在 (block=0,sky=15) 一样暗喵。
- 坑 §3 追加：模型置空后除 `getShape` 外，多格建筑要返回**整组包围盒**（含高度），否则视觉高于占地时上层无碰撞箱喵。

## 交接喵

- 占用文件喵：
  - 已改 `src/main/java/com/blockdustry/client/CoreBlockEntityRenderer.java`
  - 已改 `src/main/java/com/blockdustry/building/BlockdustryBuildingBlock.java`
  - 已改 `src/main/java/com/blockdustry/building/BlockdustryBlocks.java`
  - 已改 `src/main/resources/assets/blockdustry/textures/block/drill_side.png`（恢复 HEAD）
  - 已改 `src/main/resources/assets/blockdustry/textures/block/scatter_base.png`（透明填充）
  - 已写本文件 `docs/子agent/T8c_核心分裂黑箱修复.md`
- 建议 `runClient` 放置核心与分裂炮目测确认喵。
