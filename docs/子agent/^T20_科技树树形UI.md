# T20 科技树树形 UI 阶段产出喵

## 目标喵
把 ResearchScreen 从「缩进列表」改造为「树状结构」，仿 Mindustry ResearchDialog 的树形布局、父子连线、三态配色与材料消耗显示喵。

## 一、Mindustry ResearchDialog 树 UI 研究结论喵
读 `Mindustry/core/src/mindustry/ui/dialogs/ResearchDialog.java` 喵：
- **布局**：`BranchTreeLayout` 把根的子节点分成左/右两半，左半 top 布局、右半 bottom 布局再 `shift` 对齐，形成 Mindustry 标志性的竖排平衡树；节点为 `nodeSize`(60) 方框喵。
- **连线**（`View.drawChildren`）：父-子之间画 **L 形**——先 `Lines.line(父x,父y, 子x,父y)`（父高度横段）、再 `Lines.line(子x,父y, 子x,子y)`（子 x 竖段）；`lock = 父或子锁定`，锁定=灰、否则 accent 绿，`Draw.z` 分层喵。
- **三态**（`button.update`）：`up` 样式 = 已解锁→buttonOver、锁定且不可 spend→buttonRed、锁定但可 spend→button；图标未解锁白、锁定灰；图标可换锁 Icon 喵。
- **可研究/可消耗**：`canSpend` = objectives 完成 + 至少一项材料有库存；点击 `spend(node)` 从聚合核心库存扣料、`finishedRequirements` 累加、满则 `unlock`（内容解锁 + 父链全解锁）+ uiUnlock 音 + `ResearchEvent` 喵。
- **悬停信息面板**（`infoTable`）：节点右侧浮窗，显示名称、`research.progress` 百分比（value 加权，上限 99%）、逐材料 `图标 名称 剩余/需求`、移动端「研究」按钮喵。
- **滚动/缩放**：滚轮缩放(0.25~1)、拖动平移、`clamp` 边界喵。

## 二、ResearchScreen 改造内容喵
`com.blockdustry.client.ResearchScreen` 重写为树状版喵：
- **树布局**（`layoutTree`/`layoutNode`）：递归按子树宽度布局——叶子按游标横排（`cursor += NODE+X_GAP`），内部节点居中于子树水平区间之上，`y = 深度 * Y_STEP`（根在上、子在下）；返回子树水平区间 `Interval` 喵。
- **连线**（`drawConnections`）：L 形 fill 细矩形（`drawThickLine`），父或子锁定=灰 `0xFF555555`、否则绿 `0xFF55FF55`，2px 喵。
- **节点三态**（`drawNode`）：方框 46px + 物品/方块图标 + 名称；边框——已解锁=绿 `0xFF55FF55`、可研究=灰 `0xFFAAAAAA`、未达前置=红 `0xFFFF5555`；锁定态覆盖半透明黑压暗图标喵。
- **悬停信息面板**（`drawInfoPanel`）：可研究节点右侧浮窗，显示名称、整体进度条+百分比、逐材料 `图标 名称 投入/需求`（需求沿用 `ResearchTree.effectiveRequirements` = ResearchCost 公式结果）、「点击研究（消耗背包材料）」提示喵。
- **交互**：滚轮围绕鼠标缩放(0.25~2)、左键拖动平移（>4px 判定拖动，防误触研究）、左键点击可研究节点 → `ResearchSpendPayload`（服务端扣背包）喵；`mouseScrolled` 4 参数、`mouseDragged`/`mouseReleased` 走 click-vs-drag 判定喵。
- **初始适配**（`fitToScreen`）：整树缩放到屏幕可容纳并居中，`init()` 里调用（此时才有 width/height）喵。

## 三、改动文件清单喵
| 文件 | 改动喵 |
|---|---|
| `src/main/java/com/blockdustry/client/ResearchScreen.java` | 重写：缩进列表 → 树状（布局/连线/三态/信息面板/缩放平移）喵 |
| `src/main/java/com/blockdustry/client/ResearchClientCache.java` | `onUnlock` 加解锁音（EXPERIENCE_ORB_PICKUP，Mindustry uiUnlock 对应）喵 |

保持不动喵：网络同步（ResearchClientCache/ResearchNetwork/payload）、J 键打开（ResearchScreenHandler/ResearchKeyRegistrar）、A 组数据/存档/门控喵。

## 四、编译与注册喵
- `./gradlew compileJava` **BUILD SUCCESSFUL**（仅既有 deprecation 警告）喵。
- 无需新增注册：Screen 由 `ResearchScreenHandler`（J 键）打开，无共享文件改动喵。
- lang 沿用 B 组清单：`screen.blockdustry.research.title` / `key.blockdustry.research` / `blockdustry.research.completed`（主会话已整合）喵。

## 五、风险与待人工排查喵
- 布局为「父居中于子树」的递归布局，不是 Mindustry 左右半平衡布局；对当前 12 叶子树视觉足够，若后续树更大再上 BranchTreeLayout 仿写喵。
- 进度为「件数加权」而非 Mindustry 的「value 加权」；数值观感略异，可后续按 `item cost` 加权喵。
- 连线用 fill 细矩形（无抗锯齿），放大到 2x 时略糙；要平滑可改用 Tesselator 画线（本 mod PowerNode 激光先例）喵。
- 物品图标 16px 无法放大，节点内图标偏小；要放大需 RenderSystem 变换，v2 再议喵。

## 占用与交接喵
- 占用文件（写）：ResearchScreen.java、ResearchClientCache.java、docs/坑/API签名与编译.md（补 §8 playSound 签名）喵。
- 占用文件（读）：Mindustry/core/src/mindustry/ui/dialogs/ResearchDialog.java 喵。
- 交接给：主会话（runClient 实测：J 开树 → 缩放/平移 → 点可研究节点扣背包 → 解锁连线变绿 + 音效）喵。
- 风险/待人工排查：见第五节喵。

## 异常喵
无喵
