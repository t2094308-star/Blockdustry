# T4b 分裂炮贴图与尺寸修正 阶段产出喵

## 目标喵
修正「分裂炮（Scatter，对空）」（`D:\Blockdustry\仓库`，NeoForge 1.21.1）的模型/贴图/尺寸问题：不再复用 duo 的双管转盘贴图，改用 Mindustry scatter 专属单管转盘（scatter-mid），并把 1×1 改为原作 2×2 建筑喵。

## 根因喵
- T4 实现的 `ScatterBlockEntityRenderer` 直接复用 `turret_top.png`（duo 的转盘含双炮管），且 scatter 按 1×1 注册（`SCATTER_ITEM`/`scatterBlock()` size=1），与 duo 完全一致喵。
- Mindustry 原作：scatter 是 2×2 建筑，转盘为单管 flak 炮的专属 `scatter-mid.png`，基座为 `scatter.png`（2×2 底座）喵。
- 素材定位：`D:\Blockdustry\Mindustry\core\assets-raw\sprites\blocks\turrets\scatter\` 下 `scatter-mid.png`（64×64 单管转盘）与 `scatter.png`（64×64 2×2 基座）喵。

## 结论/产出喵

### 1. 专属贴图拷贝（新资源）喵
- `Mindustry/.../scatter-mid.png`（64×64）→ `src/main/resources/assets/blockdustry/textures/entity/scatter_top.png`（单管转盘）喵
- `Mindustry/.../scatter.png`（64×64）→ `src/main/resources/assets/blockdustry/textures/block/scatter_base.png`（2×2 基座）喵

### 2. 渲染器改用专属贴图 + 2×2 缩放（改 `client/ScatterBlockEntityRenderer.java`）喵
- `TEX_TOP` → `textures/entity/scatter_top.png`（不再复用 duo `turret_top`）喵
- 旋转/后坐力逻辑保留：`aimYaw` 绕 Y 旋转、整体后坐力 `pow(top,1.8)*0.5px/8` 沿局部 +Z 位移喵
- 转盘 quad 半宽常量 `HALF=1`：-1..1（2×2），uv 覆盖整张贴图喵
- 定位改到 2×2 顶面中心：`pose.translate(1.0f, 1.02f, 1.0f)`（锚点在 NW 角，中心 +1,+1）喵
- 保持 `entityCutout` + 全亮 `setLight(0xF000F0)` + `NO_OVERLAY`（防炮管黑，见 `docs/研究-炮管黑.md`）喵

### 3. 尺寸 1×1 → 2×2（改注册 + 新模型 + blockstate）喵
- `src/main/java/com/blockdustry/building/BlockdustryBlocks.java`：
  - `SCATTER_ITEM` 的 `BlockdustryBuildingItem` size 1→2 喵
  - `scatterBlock()` 的 `BlockdustryBuildingBlock` size 1→2 喵
- 新模型 `models/block/scatter_nw/ne/sw/se.json`：沿用 drill/graphite_press 的 2×2 四象限裁剪模式，`up` 面取 `scatter_base` 的对应象限 UV（NW[0,0,8,8]/NE[8,0,16,8]/SE[8,8,16,16]/SW[0,8,8,16]），侧/底沿用 `drill_side` 喵
- `blockstates/scatter.json`：9 corner variant 全映射到四象限模型（nw/n/w/c→nw，ne/e→ne，sw/s→sw，se→se），与 drill 一致喵
- `models/block/scatter.json`（物品展示用）顶部贴图 `turret_base` → `scatter_base` 喵

### 4. 开火/瞄准中心对齐 2×2（改 `ScatterBlockEntity.java`）喵
- 覆盖 `turnToward`：以整座建筑中心（锚点 NW +0.5,+0.5）为轴转向，避免从 NW 角瞄准导致炮管偏角喵
- `fireFlak`：开火原点 `center` 由 `worldPosition.getCenter()` 改为建筑中心 `base.getCenter().add(half,0,half)`，与渲染转盘中心对齐喵

### 5. 编译验证喵
- `./gradlew compileJava`：BUILD SUCCESSFUL（13s），仅 4 个既有 `EventBusSubscriber.bus()` 过时警告（与本次改动无关）喵

## 占用与交接喵
- 占用文件（本次改动）:
  - 新: `src/main/resources/assets/blockdustry/textures/entity/scatter_top.png`
  - 新: `src/main/resources/assets/blockdustry/textures/block/scatter_base.png`
  - 改: `src/main/java/com/blockdustry/client/ScatterBlockEntityRenderer.java`
  - 改: `src/main/java/com/blockdustry/building/ScatterBlockEntity.java`
  - 改: `src/main/java/com/blockdustry/building/BlockdustryBlocks.java`（SCATTER_ITEM / scatterBlock size→2）
  - 新: `src/main/resources/assets/blockdustry/models/block/scatter_nw.json`
  - 新: `src/main/resources/assets/blockdustry/models/block/scatter_ne.json`
  - 新: `src/main/resources/assets/blockdustry/models/block/scatter_se.json`
  - 新: `src/main/resources/assets/blockdustry/models/block/scatter_sw.json`
  - 改: `src/main/resources/assets/blockdustry/blockstates/scatter.json`
  - 改: `src/main/resources/assets/blockdustry/models/block/scatter.json`（物品展示顶贴图）
- 交接给: 主会话。需在游戏内实测（`runClient`）：2×2 基座四象限拼接、转盘为单管 scatter-mid、旋转/后坐力正常喵。
- 风险/待人工排查:
  - `nearestLiving` 的索敌 AABB 仍以锚点 NW 格为中心（`new AABB(worldPosition).inflate(range)`），相对 2×2 中心有约 0.5 格偏移；scatter 射程 27.5、大散布，影响可忽略，未改喵
  - 转盘贴图"前"方向校准沿用 duo 的 `atan2(-dx,-dz)+0°`，若实测炮口朝反方向，需在 `ScatterBlockEntityRenderer` 或 `ScatterBlockEntity.turnToward` 加 ±180° 修正喵
  - 2×2 底座 `scatter_base.png` 平台不铺满 64×64（边缘留透明），四象限拼接后符合 Mindustry 视觉（基座悬浮在格内）喵

## 异常喵
无（编译通过，仅既有过时警告）喵
