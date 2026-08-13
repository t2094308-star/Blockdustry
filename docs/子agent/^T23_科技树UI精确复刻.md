# T23 科技树 UI 精确复刻（Mindustry ResearchDialog）阶段产出喵

## 目标喵
把 ResearchScreen 从「自创树形」改为「精确复刻 Mindustry ResearchDialog」：BranchTreeLayout 半平衡树布局 + L 形连线 + 三态节点按钮 + 右侧信息面板（大图/名称/需求材料/进度条/研究按钮），让玩家一眼认出是 Mindustry 科技树喵。

## 一、Mindustry ResearchDialog 结构梳理喵
通读 `Mindustry/core/src/mindustry/ui/dialogs/ResearchDialog.java` + `ui/layout/BranchTreeLayout.java` + `ui/layout/TreeLayout.java` + `graphics/Pal.java` + `ui/Styles.java` 喵：
- **布局**：`BranchTreeLayout` = **Reingold-Tilford 树布局**（`firstWalk` 算 prelim + `apportion` 防重叠 + `secondWalk` 落坐标）；`ResearchDialog.treeLayout()` 把根的子节点**分成左右两半**——左半 `rootLocation=top`（往下长）、右半 `rootLocation=bottom`（往上长），再 `shift(leftHalf, root.y - lastY)` 对齐，形成 Mindustry 标志性的左右平衡树（根居中、两翼分列上下）喵。
- **连线**（`View.drawChildren`）：`lock = 父或子锁定`，锁定=Pal.gray、解锁=Pal.accent；`Lines.stroke(4f)`；**L 形**——`line(父.x,父.y, 子.x,父.y)` 再 `line(子.x,父.y, 子.x,子.y)` 喵。
- **节点按钮**：`ImageButton` 60px（`Scl.scl(60)`），`resizeImage(32)`；`up` 样式按状态：未解锁→buttonOver（accent 框）、不可 spend→buttonRed（红框）、可 spend→button（暗框）；图标色：未解锁白 / 锁定+可选 Color.gray / 锁定不可选 Pal.gray；不可选显示锁 Icon；点击 `canSpend && locked` 时 `spend(node)` 喵。
- **信息面板**（`infoTable`）：悬停节点右上方浮窗，`Tex.button` 底；显示名称（不可选时 `[accent]???`）、`research.progress` 百分比（value 加权上限 99%）、逐材料「图标 名称 剩余/需求」（有材料=lightGray，无=scarlet）、移动端「研究」按钮喵。
- **滚动/缩放**：滚轮缩放 0.25~1、拖动平移、`clamp` 边界喵。
- **配色**：`Pal.accent=#ffd37f`、`Pal.gray=#454545`、`Pal.remove=#e55454`、`Pal.darkOutline=#2d2f39`、`Color.lightGray=#c6c6c6`、`Color.scarlet=#ff3619` 喵。

## 二、复刻实现喵
| 文件 | 改动喵 |
|---|---|
| `src/main/java/com/blockdustry/client/ResearchTreeLayout.java` | **新增**：`ResearchTreeLayout.layout(root, nodeSize, spacing)` 完整移植 Reingold-Tilford + 左右半平衡（`LNode`/`BranchLayout` 内部类，字段对齐源码 mode/prelim/change/shift/thread/ancestor/number）喵 |
| `src/main/java/com/blockdustry/client/ResearchScreen.java` | **重写**：用 `ResearchTreeLayout` 布局；L 形连线（锁定灰 `#454545`/解锁 `#ffd37f`、4px）；节点三态（accent 金/亮灰/红 + 暗图标）；右侧信息面板（大图 56px 放大预览、名称、状态、需求「图标 名称 投入/需求」、进度条、accent 边框研究按钮）；滚轮缩放 0.25~1、拖动平移、点击选中、点研究按钮消耗喵 |
| `src/main/java/com/blockdustry/client/ResearchClientCache.java` | 不变（大图/图标复用 `iconFor/nameFor`）喵 |
| `src/main/java/com/blockdustry/research/ResearchManager.java` 等 | 不变（Mindustry 消耗逻辑已就位）喵 |

关键复刻点喵：
- **精确色值**：全用 Mindustry Pal/Color 喵。
- **大图预览**：`g.pose().pushPose()` + `scale(s,s,1f)` + `renderItem(stack, x/s, y/s)` + `popPose()` 放大物品图标（坑见 docs/坑 §9）喵。
- **三态映射**：`unlocked`→accent 金框；`researchable`（父全解锁+未解锁）→亮灰框可点；其余→红框暗图标；悬停白框喵。
- **连线**：L 形（父中心横段 + 子 x 竖段），4px，Mindustry drawChildren 同款喵。
- **交互**：点击节点→右侧面板选中显示详情；点「研究」→ `ResearchSpendPayload`（服务端扣背包+解锁+广播）；滚轮缩放/拖动平移沿用喵。

## 三、编译喵
- `./gradlew compileJava` **BUILD SUCCESSFUL**（仅既有 deprecation 警告）喵。

## 四、注册喵
- 无新增注册：ResearchScreen 仍由 `ResearchScreenHandler`（J 键）打开；`ResearchTreeLayout` 纯工具类被 Screen 调用喵。
- lang 沿用：`screen.blockdustry.research.title` / `key.blockdustry.research` / `blockdustry.research.completed` / `blockdustry.research.locked`（主会话已整合）喵。

## 五、风险与待人工排查喵
- 布局为精确 Reingold-Tilford + 左右半平衡（非近似），与 Mindustry 源码算法一致；节点尺寸 56 vs Mindustry 60（MC 缩放 854x480 下略小以免溢出）喵。
- 进度为「件数加权」，Mindustry 为「value 加权」；观感略异可后续按 item cost 加权喵。
- 连线用 fill 细矩形（无抗锯齿），放大到 1x 略糙；要平滑可改用 Tesselator 画线（本 mod PowerNode 激光先例）喵。
- 大图预览用 PoseStack 缩放，若个别物品模型缩放异常需实测（多数正常）喵。
- 节点图标 16px 原生不可放大，与 Mindustry 32px 图标有差；大图预览已放大补偿喵。

## 占用与交接喵
- 占用文件（写）：ResearchTreeLayout.java、ResearchScreen.java、docs/坑/API签名与编译.md（补 §9）、docs/子agent/^T23_科技树UI精确复刻.md、任务登记 T23_科技树UI精确复刻.md 喵。
- 占用文件（读）：Mindustry ResearchDialog.java / BranchTreeLayout.java / TreeLayout.java / Pal.java / Styles.java 喵。
- 交接给：主会话（runClient 实测：J 开树 → 看左右平衡树/金线与三态 → 点节点看右侧面板 → 点研究扣背包解锁 → 连线变金 + 音效）喵。
- 风险/待人工排查：见第五节喵。

## 异常喵
无喵
