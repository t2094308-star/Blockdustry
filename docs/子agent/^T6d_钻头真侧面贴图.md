# T6d 钻头真侧面贴图 阶段产出喵

> 任务变更：停止把 `drill_side_preview.png`（顶面图案局部）当侧面贴图，改为**重新自绘真正的钻头底座侧面**喵。
> 结论：新贴图为 512×512、RGBA、全不透明，覆盖 `textures/block/drill_side.png`，不改模型/Java 喵。

## 1. 变更背景喵

- 用户确认：之前 `drill_side.png`（64×64 粗糙版）与 `drill_side_preview.png`（512×512）的内容**都不是侧面贴图**，只是机械钻头顶面贴图 `drill.png` 合成图的一部分，绘制方向错误喵。
- Mindustry 参考图 `core/assets-raw/sprites/block/drills/mechanical-drill.png` 为 64×64 **顶视图**（6 色：深灰金属 #4a4b53/#6e7074/#989aa4/#b0bac0 + 铜色 #c9a58f/#8f665b），并非侧面视角，因此**只用其视觉语言**（深灰金属外壳 + 铜色点缀），从 3D 侧面视角重新绘制喵。

## 2. 新贴图内容描述喵

512×512 侧视设计（单块面 = 一整个 2×2 底座中每个方块的侧脸，整幅映射 uv [0,0,16,16]）喵：

- **深色金属外壳**：`#4a4b53` 基底 + 2% 暗/亮颗粒，上下盖接缝（上亮下暗顶光倒角），左右边缘暗缝 → 横向无缝拼合（2 块拼起来每块间呈暗缝，读作「两台机器段接合」）喵。
- **中央凹槽面板**：96..415 凹陷维护面板，内凹倒角（上/左压暗、下/右受光）喵。
- **螺栓**：面板四角 4 颗圆头螺栓（亮头+暗环+十字槽）+ 外壳顶/底带 4 颗 + 铆钉排喵。
- **通风孔**：面板上部凸起铭牌板上 3×2 通风栅格（暗缝 + 下缘受光）喵。
- **接缝**：面板上下各一条贯穿全宽的横向接缝线；左右边缘接缝用于无缝拼合喵。
- **中央舱盖**：圆形检修舱盖（r40）+ 大十字槽 + 边缘 4 颗小螺栓喵。
- **状态灯/铜色点缀**：面板中部 4 颗圆指示灯（2 亮 2 暗）、舱盖下方刻字铭牌线、下部铜色警示条（呼应钻头顶面的 Mindustry 铜色钻头）喵。

## 3. 命名决策喵

- 选 `drill_side.png`（保持原名），**不改模型 JSON**喵。
- 理由：`models/block/drill_{nw,ne,sw,se}.json` 的 `side`/`particle` 已引用 `blockdustry:block/drill_side`；保持同名零改动、零风险，符合「不改模型 JSON」约束喵。
- 若用语义名 `drill_side_base.png` 需同步改 4 个模型 JSON 的 side/particle 引用，收益低、风险高，故不选喵。

## 4. 绘制方法喵

- 脚本：`D:\Blockdustry\像素图片绘制尝试\draw_drill_side_base.py`（PIL 12.x 可复用）喵。
- 尺寸 512×512，调色板取自 Mindustry 参考图（8 主色 + 2 中间调，共 10 色）喵。
- 结构：基底+颗粒 → 上下盖倒角 → 左右接缝 → 凹槽面板 → 铭牌板+通风栅格 → 中央舱盖 → 指示灯/铜条/螺栓/铆钉喵。

## 5. 校验结果（PIL）喵

| 项 | 结果喵 |
|---|---|
| 尺寸 | 512×512 ✓喵 |
| 模式 | RGBA ✓喵 |
| Alpha | min=max=255，全不透明 ✓喵 |
| 色数 | 10 色、非单色 ✓喵 |
| 与旧备份 | 与 `drill_side_old.png`（64×64）尺寸/像素均不同，确认为新图 ✓喵 |
| 布局抽查 | 顶盖高光、左右接缝、凹槽面板、通风栅格、舱盖十字槽、指示灯/铜条/角螺栓像素均符合预期 ✓喵 |

## 6. 模型 UV 复核喵

- `drill_{nw,ne,sw,se}.json` 的 side/down face uv 全部为 `[0,0,16,16]`，程序化校验通过，未改动喵。

## 7. 改动文件清单喵

| 文件 | 动作喵 |
|---|---|
| `src/main/resources/assets/blockdustry/textures/block/drill_side.png` | 覆盖：512×512 新自绘真侧面贴图喵 |
| `src/main/resources/assets/blockdustry/textures/block/drill_side_old.png` | 保留：原 64×64 旧图备份（未动）喵 |
| `docs/子agent/T6d_钻头真侧面贴图.md` | 新增：本文档喵 |
| `D:\Blockdustry\像素图片绘制尝试\draw_drill_side_base.py` | 新增：绘制脚本（仓库外草稿区）喵 |
| `D:\Blockdustry\像素图片绘制尝试\机械钻头\drill_side_base_2x_preview.png` / `drill_side_base_tiled_2x.png` | 新增：单块 2× 放大与 2 块拼合预览（仓库外草稿区）喵 |

未改：Java、`models/block/drill*.json`、blockstate、顶面/旋转贴图喵。
说明：上一任务（取消）错应用的 512 preview 已随覆盖移除，其源文件仍在草稿区 `机械钻头\drill_side_preview.png` 可追溯喵。

## 8. 交接喵

- 需主会话在 `runClient` 确认侧面观感（`#side` 引用已存在，无需改模型）喵。
- 风险：侧面 4 朝向（north/south 互为镜像）显示同一设计与镜像，属 MC 方块标准行为喵。
