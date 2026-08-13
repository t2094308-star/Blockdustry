# T8b 核心贴图丢失排查与修复喵

## 现象喵

用户测试 T8 改动后，核心（core，3×3×1 占地）在游戏内**贴图丢失**：看不到模型 / 黑紫棋盘格 / 透明喵。
T8 把 9 个 `models/block/core_{corner}.json` 从 1 格高改成 3 格高（单 element `from[0,0,0]→to[16,48,16]`）喵。

## 根因喵

**Minecraft 方块模型元素坐标只允许在 [-16, 32] 范围内，`to.y=48` 超出上限 32，导致 9 个 core 模型全部解析失败**喵。

`BlockElement$Deserializer`（1.21.1）对 `from`/`to` 每个分量做 `-16 ≤ v ≤ 32` 校验，越界抛 `JsonParseException`喵。
游戏日志（`tools/.game_log.txt`）明确记录喵：

```
[ERROR] [minecraft/ModelManager]: Failed to load model blockdustry:models/block/core_c.json
com.google.gson.JsonParseException: 'to' specifier exceeds the allowed boundaries: ( 1.600E+1  4.800E+1  1.600E+1)
```

9 个 `core_{nw,n,ne,w,c,e,sw,s,se}.json` 全部报同样错误，随后 `ModelBakery` 对 blockstate 引用的每个模型报 `Unable to load model ... FileNotFoundException`（解析失败被当作模型缺失）喵。
方块模型缺失 → 该 block 渲染成黑紫/透明/隐形，即用户看到的「贴图丢失」喵。

排查结论（对应任务 5 点）喵：
1. 模型 JSON 的 textures 块齐全（particle/top/side 都定义了 `blockdustry:block/core`），faces 引用的 `#top`/`#side` 也都有定义，UV 全在 0..16 内 → 不是纹理声明/UV 越界问题喵。
2. blockstate 9 个 corner variant 全部引用对应的 `core_{corner}.json`，引用齐全喵。
3. `textures/block/core.png` 与 `textures/entity/core_team.png` 都存在且都是 96×96 RGBA，base 全不透明（9216/9216），team 层十字形半透明（四角透明，N/W/C/E/S 有内容）喵。
4. git diff 对比 T8：唯一导致加载失败的是 `to` 的 y 从 16 改成 48，超出坐标上限喵。
5. `CoreBlockEntityRenderer` 顶面 y+3.001 逻辑本身正确，但依赖的方块模型已加载失败，渲染无意义喵。

## 修复方案喵

由于方块模型单 element 最高只能到 y=32，**无法用方块模型直接渲染 0..48 的 3 格高立方体**（上限 32）喵。
若把 element 改到 `from[0,-16,0]→to[16,32,16]`（48 高合法），会整体下移 1 格，放置在地面时底层埋进地里，只能看到 3×3×2，不是真正 3×3×3 喵。

因此采用「**方块模型置空 + 锚点格 BER 绘制整颗 3×3×3 正方体**」方案喵：
- 9 个 `core_{corner}.json` 改为合法空模型（无 element，不渲染方块本体，保留 particle）喵。
- `CoreBlockEntityRenderer` 在锚点格用方块图集贴图绘制完整 3×3×3 立方体（6 个外表面各贴整张 96×96 core 纹理，3 格拼 1 张、2px/单位、无拉伸），顶面再叠队伍染色层喵。

### 改动 1：9 个模型 JSON 置空喵

`src/main/resources/assets/blockdustry/models/block/core_{nw,n,ne,w,c,e,sw,s,se}.json` 统一改为喵：

```json
{
  "parent": "minecraft:block/block",
  "textures": {
    "particle": "blockdustry:block/core"
  }
}
```

无 `elements` → 合法空模型（不触发坐标校验），方块本体不渲染，由 BER 补全视觉喵。
`blockdustry:block/core` 仍由 item 模型 `models/item/core.json → models/block/core.json(cube_all)` 引用，方块图集内必有该贴图喵。

### 改动 2：渲染器绘制整颗立方体喵

`src/main/java/com/blockdustry/client/CoreBlockEntityRenderer.java` 重写喵：
- base 层：`RenderType.solid()` + 方块图集 sprite（`TextureAtlas.LOCATION_BLOCKS` 取 `blockdustry:block/core`），画 6 个外表面喵。
- 立方体以锚点格本地坐标 0..3 覆盖 3×3 占地（锚点恒为 NW 格，x 0..3 / z 0..3）喵。
- 顶面 y=3 画 base 顶，随后 `entityTranslucent` 队伍染色层画在 y=3.001 防 z-fighting（十字形贴图）喵。
- 用 `light`（方块实体光照）使 base 层正常受光；队伍层保持 `0xF000F0` 全亮喵。

各面顶点顺序已按逆时针（从外侧看）排好，法线正确，朝向无误喵。

## 验证喵

- 9 个模型 JSON 用 python `json.load` 解析合法；blockstate 9 个 variant 引用的模型文件全部存在喵。
- `./gradlew compileJava` 通过（仅既存 `EventBusSubscriber` 弃用警告，与本改动无关）喵。
- 放置核心后应为：地面上一颗 3×3×3 正方体，侧面/底面贴整张灰蓝 core 纹理，顶面 core 纹理 + 队伍色十字层（蓝），选中框 3 格高喵。

## 交接喵

- 占用文件喵：
  - 已写 `src/main/resources/assets/blockdustry/models/block/core_{nw,n,ne,w,c,e,sw,s,se}.json`（9 个，置空）喵
  - 已写 `src/main/java/com/blockdustry/client/CoreBlockEntityRenderer.java`（整颗立方体渲染）喵
- 建议：`runClient` 放置核心目测确认；如需「方块模型可见 + 只让 BER 补顶层」的折中方案，可改回带 element 的 2 格高模型并让 BER 只画顶层（改动更大，当前方案已满足 3×3×3 需求）喵。
