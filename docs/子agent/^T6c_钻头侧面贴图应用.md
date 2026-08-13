# T6c 钻头侧面贴图应用喵

> 任务：把钻头侧面贴图换成用户选定的精致版 `drill_side_preview.png`（512×512）喵。
> 背景：钻头 2×2，四侧面模型 side/down face 引用 `#side` = `textures/block/drill_side.png`（原为 64×64 粗糙版）喵。

## 1. 输入校验结果喵

- `D:\Blockdustry\像素图片绘制尝试\机械钻头\drill_side_preview.png`：512×512、RGBA、全不透明（alpha 极值 255,255）、文件 3772 字节喵。
- 校验通过：尺寸符合预期，无透明区，可用方块 solid 渲染不黑喵。

## 2. 改动文件喵

- 覆盖目标：`src/main/resources/assets/blockdustry/textures/block/drill_side.png` → 由 64×64 覆盖为 512×512 精致版喵。
- 旧 64×64 备份：同目录 `drill_side_old.png`（64×64）喵。
  - 该校验时与覆盖前 `drill_side.png` 哈希一致（92ee8ae6...），备份已存在且有效，未重复覆盖喵。
- 未改任何 Java / 模型 JSON / blockstate 喵。

## 3. 模型 UV 校验结果喵

- 四个模型 `drill_{nw,ne,sw,se}.json` 的 side/down face uv 全部为 `[0,0,16,16]`，校验 OK 喵。
- 说明：MC 方块模型 uv 是 16×16 模型 UV 空间，0..16 即整张贴图，MC 自动映射到 atlas 中该贴图位置喵。
- 若把数值改大会采样 atlas 里其他方块贴图导致杂乱+卡顿（已踩的坑），本次未改动 uv，无需回退喵。

## 4. 贴图尺寸影响喵

- MC 方块 atlas 支持任意尺寸贴图（默认 mipmap 正常生成）喵。
- 512×512 比 64×64 清晰 8 倍，单格面片（16×16 模型 UV 覆盖整张）呈现高分辨率侧面图案喵。
- atlas 尺寸上限：现代 MC 默认 atlas 上限 32768px，即使旧版本也 ≥1024px，单张贴图 512×512 远低于上限，安全喵。
- 注意：全不透明 RGBA，方块 solid 渲染无需换 RenderType，透明区变黑问题不适用喵。

## 5. 验证结论喵

- 贴图已替换、备份完好、模型 UV 未破坏，无需回退喵。
