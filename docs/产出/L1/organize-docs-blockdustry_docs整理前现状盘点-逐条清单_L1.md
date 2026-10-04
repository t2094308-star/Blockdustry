---
taskId: organize-docs-blockdustry
role: researcher
tier: 1
keywords: ["docs整理前现状盘点-逐条清单","docs盘点","目录清单","整理前现状","命名规范","违规命名","^前缀","archive","引用面","Blockdustry","只读清单","非md临时文件","类别目录缺失","理前","盘点","前现","清单","条清","现状","整理","逐条"]
relatedFiles: ["D:\\Blockdustry\\仓库\\docs","D:\\Blockdustry\\仓库\\docs\\坑","D:\\Blockdustry\\仓库\\docs\\产出","D:\\Blockdustry\\仓库\\docs\\核心数据库","D:\\Blockdustry\\仓库\\docs\\archive","D:\\Blockdustry\\仓库\\docs\\_临时","D:\\Blockdustry\\.agent-contract\\project.json","D:\\Blockdustry\\.agent-contract\\panel.json","D:\\Blockdustry\\仓库\\docs\\产出\\L1","D:\\Blockdustry\\仓库\\docs\\产出\\L2","D:\\Blockdustry\\仓库\\docs\\产出\\L3","D:\\Blockdustry\\仓库\\docs\\_临时\\EXP-RELOCATE-实验","D:\\Blockdustry\\仓库\\docs\\产出\\EXP-RELOCATE-搬入","D:\\Blockdustry\\仓库\\docs\\研究\\单位工厂与单位.md","D:\\Blockdustry\\仓库\\docs\\研究\\电力系统.md","D:\\Blockdustry\\仓库\\docs\\研究\\3x3核心与工厂.md","D:\\Blockdustry\\仓库\\docs\\研究\\Jade联动.md","D:\\Blockdustry\\仓库\\docs\\研究\\Mindustry各类条.md","D:\\Blockdustry\\仓库\\docs\\研究\\Mindustry资源栏样式与HUD修复.md","D:\\Blockdustry\\仓库\\docs\\研究\\Mindustry进度条样式.md","D:\\Blockdustry\\仓库\\docs\\研究\\PowerNode光效.md","D:\\Blockdustry\\仓库\\docs\\研究\\PowerNode激光.md","D:\\Blockdustry\\仓库\\docs\\研究\\传送带物品方向.md","D:\\Blockdustry\\仓库\\docs\\研究\\单位工厂修复.md","D:\\Blockdustry\\仓库\\docs\\研究\\机器动画粒子.md"]
createdAt: 2026-10-03T13:52:22.013Z
librarianTouchedAt: 2026-10-04T06:01:22.579Z
librarianChanges: ["传送带上下坡.md 已归档","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","馆员搬移","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","复核 keywords（新增 0 个）","复核 keywords（新增 0 个）","复核 keywords（新增 0 个）"]
detailLevel: full
---

> **速览** 本项目：Blockdustry
> **速览** 本档（L1）给：长期留档与全量核查
> **速览** 本档即最详细的一档（终点，没有下钻指针）
# docs 整理前现状盘点（逐条清单·只报事实）

> 口径：**只读**盘点，未修改/移动/改名任何既有文件；只列事实，不含任何整理建议与裁定。
> 证据说明：所有「文件清单」来自 `glob` 全树枚举（返回 184 条路径），
> 目录「存在/不存在」用 `read <目录>` 的返回区分 —— **「not a regular file」= 目录存在**、
> **「not found」= 路径不存在**（本会话无 bash，这是唯一可用的存在性判据）。
> **未验证项**：`read`/`glob`/`grep` 均不返回字节大小，**本档无法给出任何文件的字节大小**；
> `glob` 不返回空目录，空目录只用 `read` 的存在性判据确认。

---

## 1. 目录结构与每层文件数

### 1.1 已完成的全树枚举汇总

| 目录（绝对路径） | .md | 非 .md（均为 .json） | 小计 |
|---|---|---|---|
| `D:\Blockdustry\仓库\docs\`（根层） | 22 | 0 | 22 |
| `D:\Blockdustry\仓库\docs\子agent\`（根层） | 99 | 34 | 133 |
| └ `D:\Blockdustry\仓库\docs\子agent\L1\` | 1 | 0 | 1 |
| └ `D:\Blockdustry\仓库\docs\子agent\L2\` | 1 | 0 | 1 |
| └ `D:\Blockdustry\仓库\docs\子agent\L3\` | 1 | 0 | 1 |
| `D:\Blockdustry\仓库\docs\坑\` | 17 | 0 | 17 |
| `D:\Blockdustry\仓库\docs\核心数据库\` | 1 | 0 | 1 |
| `D:\Blockdustry\仓库\docs\archive\` | 0 | 0 | 0（目录存在，**空**） |
| **合计** | **150** | **34** | **184** |

- 根层 22 篇逐条见第 2 节；`子agent\` 99 篇 + 34 json 逐条见第 3、4 节；`坑\` 17 篇见第 4 节。
- 全树枚举结果 = 184 条，与上表合计一致（`glob` 对目录外的每层都做了单独枚举核对）。

### 1.2 空目录 / 无文件目录（`glob` 看不到，用 `read` 判据）

| 绝对路径 | 判据 | 结论 |
|---|---|---|
| `D:\Blockdustry\仓库\docs\archive` | read → `not a regular file` | **存在（目录）**，树内 0 文件（见第 7 节） |
| `D:\Blockdustry\docs\_临时` | read → `not a regular file` | 存在（目录），**在 docs 树之外**，含 6 篇 md（见第 3.3 节） |
| `D:\Blockdustry\docs\_临时\EXP-RELOCATE-实验` | read → `not a regular file` | 存在（目录），6 篇 md 全在此层 |
| `D:\Blockdustry\仓库\docs\子agent\EXP-RELOCATE-搬入` | read → `not found` | **不存在**（changelog 里出现过，现已被搬空并删除） |

---

## 2. docs 根目录散文件：`^` 前缀 md 全部逐篇列出

`D:\Blockdustry\仓库\docs\` 根层共 **22 篇 .md，全部符合「以 `^` 开头」这一已知特征以外的三类**，逐篇如下（共 22 条，无省略）：

### 2.1 `^` 前缀（19 篇）

| # | 绝对路径 |
|---|---|
| 1 | `D:\Blockdustry\仓库\docs\修改\钻头侧面贴图应用.md` |
| 2 | `D:\Blockdustry\仓库\docs\研究\单位工厂与单位.md` |
| 3 | `D:\Blockdustry\仓库\docs\研究\电力系统.md` |
| 4 | `D:\Blockdustry\仓库\docs\研究\3x3核心与工厂.md` |
| 5 | `D:\Blockdustry\仓库\docs\研究\Jade联动.md` |
| 6 | `D:\Blockdustry\仓库\docs\研究\Mindustry各类条.md` |
| 7 | `D:\Blockdustry\仓库\docs\研究\Mindustry资源栏样式与HUD修复.md` |
| 8 | `D:\Blockdustry\仓库\docs\研究\Mindustry进度条样式.md` |
| 9 | `D:\Blockdustry\仓库\docs\研究\PowerNode光效.md` |
| 10 | `D:\Blockdustry\仓库\docs\研究\PowerNode激光.md` |
| 11 | `D:\Blockdustry\仓库\docs\研究\传送带物品方向.md` |
| 12 | `D:\Blockdustry\仓库\docs\研究\单位工厂修复.md` |
| 13 | `D:\Blockdustry\仓库\docs\研究\机器动画粒子.md` |
| 14 | `D:\Blockdustry\仓库\docs\研究\核心与队伍共享资源.md` |
| 15 | `D:\Blockdustry\仓库\docs\研究\核心染色修复.md` |
| 16 | `D:\Blockdustry\仓库\docs\研究\炮塔动画.md` |
| 17 | `D:\Blockdustry\仓库\docs\研究\炮弹卡顿.md` |
| 18 | `D:\Blockdustry\仓库\docs\研究\物品源方块与资源栏.md` |
| 19 | `D:\Blockdustry\仓库\docs\研究\电力节点实现.md` |

### 2.2 无 `^` 前缀的根层 md（3 篇）

| # | 绝对路径 | 备注（来自 `派生态索引.md` L118/L119 的登记，仅登记事实） |
|---|---|---|
| 20 | `D:\Blockdustry\仓库\docs\坑\渲染与模型坑索引.md` | 已在 `派生态索引.md` 登记（L118 同格式条目区） |
| 21 | `D:\Blockdustry\仓库\docs\archive\2026-08\传送带上下坡.md` | `核心数据库.md` L591 有 `[馆员搬移] archive/2026-10/… → 原路径` 记录（上一轮回滚痕迹） |
| 22 | `D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md` | 索引 L5 自述其为「真身（唯一权威）」；正文 L554 自述固定路径 |

> 与上级口述核对：上级说「已知至少 22 篇」——**实测根层正好 22 篇**，其中 `^` 前缀 19 篇、无前缀 3 篇。

---

## 3. 非文档临时文件（全树非 .md）

### 3.1 结论：docs 树内非 .md 文件共 **34 个，扩展名全部是 `.json`**

按上级列举的候选类型（.json/.tmp/.bak/.png/.py/.txt/.log）逐类核实：

| 扩展名 | 命中数 | 证据 |
|---|---|---|
| `.json` | **34** | `glob 仓库/docs/**/*` 全树枚举；模式匹配 `json$` 得 34 行 |
| `.tmp` | 0 | `glob 仓库/docs/**/*.{tmp,bak,txt,log,py,csv,zip}` → No files found；`glob 仓库/docs/**/*.{png,py,tmp,bak,jpg,log,txt,csv,java,json5,yml,yaml,properties,zip,psd,aseprite,ase,ttf,jar}` → No files found |
| `.bak` | 0 | 同上 |
| `.png` | 0 | `glob 仓库/docs/**/*.png` → No files found（**与「像素绘制/光效研究」类任务可能产图的预期不符，记录为事实**） |
| `.py` | 0 | 同上合并模式 |
| `.txt` | 0 | 同上合并模式 |
| `.log` | 0 | 同上合并模式 |
| 其它（csv/java/zip/psd/ttf/yml…） | 0 | 同上合并模式 |

### 3.2 34 个 json 逐条（17 组 en/zh_s 配对，全部位于 `D:\Blockdustry\仓库\docs\子agent\`）

| # | 绝对路径 | 行数（`read` 实测/抽样） |
|---|---|---|
| 1 | `D:\Blockdustry\仓库\docs\_临时\P1_批1A_A3_lang_en.json` | 未逐篇读 |
| 2 | `D:\Blockdustry\仓库\docs\_临时\P1_批1A_A3_lang_zh.json` | 未逐篇读 |
| 3 | `D:\Blockdustry\仓库\docs\_临时\P1_批1B_container_lang_en.json` | 未逐篇读 |
| 4 | `D:\Blockdustry\仓库\docs\_临时\P1_批1B_container_lang_zh.json` | 未逐篇读 |
| 5 | `D:\Blockdustry\仓库\docs\_临时\P1_批1B_itemBridge_lang_en.json` | 未逐篇读 |
| 6 | `D:\Blockdustry\仓库\docs\_临时\P1_批1B_itemBridge_lang_zh.json` | 未逐篇读 |
| 7 | `D:\Blockdustry\仓库\docs\_临时\P1_批1C_phaseWeaver_lang_en.json` | 未逐篇读 |
| 8 | `D:\Blockdustry\仓库\docs\_临时\P1_批1C_phaseWeaver_lang_zh.json` | 未逐篇读 |
| 9 | `D:\Blockdustry\仓库\docs\_临时\P1_批1C_plastaniumCompressor_lang_en.json` | **4 行**（`read` 实测：`{` + 2 条目 + `}`） |
| 10 | `D:\Blockdustry\仓库\docs\_临时\P1_批1C_plastaniumCompressor_lang_zh.json` | 未逐篇读 |
| 11 | `D:\Blockdustry\仓库\docs\_临时\P1_批1C_pulverizerIncinerator_lang_en.json` | 未逐篇读 |
| 12 | `D:\Blockdustry\仓库\docs\_临时\P1_批1C_pulverizerIncinerator_lang_zh.json` | 未逐篇读 |
| 13 | `D:\Blockdustry\仓库\docs\_临时\P1_批1C_pyratiteMixer_lang_en.json` | 未逐篇读 |
| 14 | `D:\Blockdustry\仓库\docs\_临时\P1_批1C_pyratiteMixer_lang_zh.json` | 未逐篇读 |
| 15 | `D:\Blockdustry\仓库\docs\_临时\P1_批1C_siliconSmelter_lang_en.json` | 未逐篇读 |
| 16 | `D:\Blockdustry\仓库\docs\_临时\P1_批1C_siliconSmelter_lang_zh.json` | 未逐篇读 |
| 17 | `D:\Blockdustry\仓库\docs\_临时\P1_批1D_blastDrill_lang_en.json` | 未逐篇读 |
| 18 | `D:\Blockdustry\仓库\docs\_临时\P1_批1D_blastDrill_lang_zh.json` | 未逐篇读 |
| 19 | `D:\Blockdustry\仓库\docs\_临时\P1_批1D_laserDrill_lang_en.json` | 未逐篇读 |
| 20 | `D:\Blockdustry\仓库\docs\_临时\P1_批1D_laserDrill_lang_zh.json` | 未逐篇读 |
| 21 | `D:\Blockdustry\仓库\docs\_临时\P1_批1D_pneumaticDrill_lang_en.json` | 未逐篇读 |
| 22 | `D:\Blockdustry\仓库\docs\_临时\P1_批1D_pneumaticDrill_lang_zh.json` | 未逐篇读 |
| 23 | `D:\Blockdustry\仓库\docs\_临时\P1_批1E_diodeSurgeTower_lang_en.json` | 未逐篇读 |
| 24 | `D:\Blockdustry\仓库\docs\_临时\P1_批1E_diodeSurgeTower_lang_zh.json` | 未逐篇读 |
| 25 | `D:\Blockdustry\仓库\docs\_临时\P1_批1E_powerNodeLargeBatteryLarge_lang_en.json` | 未逐篇读 |
| 26 | `D:\Blockdustry\仓库\docs\_临时\P1_批1E_powerNodeLargeBatteryLarge_lang_zh.json` | 未逐篇读 |
| 27 | `D:\Blockdustry\仓库\docs\_临时\P1_批1F_copperScrapWall_lang_en.json` | 未逐篇读 |
| 28 | `D:\Blockdustry\仓库\docs\_临时\P1_批1F_copperScrapWall_lang_zh.json` | 未逐篇读 |
| 29 | `D:\Blockdustry\仓库\docs\_临时\P1_批1F_titaniumWallDoor_lang_en.json` | 未逐篇读 |
| 30 | `D:\Blockdustry\仓库\docs\_临时\P1_批1F_titaniumWallDoor_lang_zh.json` | 未逐篇读 |
| 31 | `D:\Blockdustry\仓库\docs\_临时\P1_批2A_高级墙体_lang_en.json` | 未逐篇读 |
| 32 | `D:\Blockdustry\仓库\docs\_临时\P1_批2A_高级墙体_lang_zh.json` | 未逐篇读 |
| 33 | `D:\Blockdustry\仓库\docs\_临时\P1_批2B_menderForceProjector_lang_en.json` | 未逐篇读 |
| 34 | `D:\Blockdustry\仓库\docs\_临时\P1_批2B_menderForceProjector_lang_zh.json` | 未逐篇读 |

- **共同特征（事实）**：34 个全部 `^` 前缀 + 全部形如 `^P1_批<批次>_<英文件名>_lang_{en,zh}.json` + 全部位于 `仓库\docs\子agent\` 根层（有 1 个例外见下）。
- **大小**：**所有 34 个文件的字节大小未验证**（工具不返回 size）。抽样 1 个（`plastaniumCompressor_lang_en.json`）为 4 行、内容为 2 条 key→译名字符串。

### 3.3 上级点名要求的「`D:\Blockdustry\docs\_临时`」现状（**不在 docs 树内，但属同一搬迁议题**）

| 绝对路径 | 类型 | 大小 |
|---|---|---|
| `D:\Blockdustry\docs\_临时\EXP-RELOCATE-实验\EXP-RELOCATE-载体_搬迁指针观测载体_L2.md` | .md | 未验证 |
| `D:\Blockdustry\docs\_临时\EXP-RELOCATE-实验\EXP-RELOCATE-指针测试_搬迁指针测试甲_L2.md` | .md | 未验证 |
| `D:\Blockdustry\docs\_临时\EXP-RELOCATE-实验\EXP-RELOCATE-指针测试_搬迁指针测试乙_L2.md` | .md | 未验证 |
| `D:\Blockdustry\docs\_临时\EXP-RELOCATE-实验\EXP-RELOCATE-指针测试_搬迁测试丙_L2.md` → 实为 `EXP-RELOCATE-指针测试_搬迁指针测试丙_L2.md` | .md | 未验证 |
| `D:\Blockdustry\docs\_临时\EXP-RELOCATE-实验\EXP-RELOCATE-正对照_搬迁指针正对照载体_L2.md` | .md | 未验证 |
| `D:\Blockdustry\docs\_临时\EXP-RELOCATE-实验\EXP-RELOCATE-精确匹配_精确匹配载体_L2.md` | .md | 未验证 |

> **该目录已存在**（read 判据 = 目录），含 6 篇 `_L2.md` 实验档；**目录内无 .json/.tmp/.bak/.png**。

---

## 4. 三个已存在类别目录里的违规命名 md

**规范原文**（上级口述）：`<任务号>_<标题>_<类别>.md`；产出档为 `子agent/L<n>/<任务号>_<标题>_L<n>.md`。
判定：文件名**不**同时满足「有任务号 + 有类别/L 级后缀」即记为违规；`README.md` 类索引档同样记为违规（无任务号）。

### 4.1 `D:\Blockdustry\仓库\docs\坑\`（17 篇，**17/17 违规**）

| # | 现文件名 | 所在目录 | 该去的类别目录（按上级给的规范后缀推断） | 违规点 |
|---|---|---|---|---|
| 1 | `API签名与编译.md` | `仓库\docs\坑\` | `仓库\docs\坑\`（类别后缀 `_坑.md` 缺失） | 缺任务号 + 缺 `_坑` |
| 2 | `BER渲染.md` | 同上 | 同上 | 同上 |
| 3 | `Jade条颜色黑灰修复.md` | 同上 | 同上 | 同上 |
| 4 | `Jade联动.md` | 同上 | 同上 | 同上 |
| 5 | `PowerNode激光黑色.md` | 同上 | 同上 | 同上 |
| 6 | `README.md` | 同上 | 同上 | 缺任务号 + 缺 `_坑`（索引档） |
| 7 | `方块模型.md` | 同上 | 同上 | 同上 |
| 8 | `机器侧面贴图.md` | 同上 | 同上 | 同上 |
| 9 | `炮塔黑色阴影.md` | 同上 | 同上 | 同上 |
| 10 | `炮塔黑色阴影2.md` | 同上 | 同上 | 同上 |
| 11 | `炮弹与实体.md` | 同上 | 同上 | 同上 |
| 12 | `炮管黑.md` | 同上 | 同上 | 同上 |
| 13 | `物流交接.md` | 同上 | 同上 | 同上 |
| 14 | `碰撞箱.md` | 同上 | 同上 | 同上 |
| 15 | `贴图.md` | 同上 | 同上 | 同上 |
| 16 | `队伍染色.md` | 同上 | 同上 | 同上 |
| 17 | `附身与相机.md` | 同上 | 同上 | 同上 |
| 18 | `EXP-RELOCATE_搬迁引用同步实验全过程_坑.md` | 同上 | 同上 | **仅此项有类别后缀 `_坑`**，但标题段含两个 `_`、任务号段为 `EXP-RELOCATE`（非 `T{n}`），整体仍不满足 `<任务号>_<标题>_<类别>` 的三段式严格形式（**该点未由规范文本明确，仅记录事实**） |

> 说明：`坑\` 实际枚举到 **18 条文件名**（16 主题档 + `README.md` + `EXP-RELOCATE_…_坑.md`）。
> 上级口述/任务卡曾出现「17 篇」「18 篇」两种说法，**本轮实测为 18 个文件**，以此为准。

### 4.2 `D:\Blockdustry\仓库\docs\核心数据库\`（1 篇，**1/1 违规**）

| # | 现文件名 | 所在目录 | 该去的类别目录 | 违规点 |
|---|---|---|---|---|
| 1 | `派生态索引.md` | `D:\Blockdustry\仓库\docs\核心数据库\` | `仓库\docs\核心数据库\`（类别后缀 `_核心数据库.md` 缺失） | 缺任务号 + 缺类别后缀（派生物索引档） |

### 4.3 `D:\Blockdustry\仓库\docs\子agent\`（根层 99 篇 md，**99/99 违规**）

分组列全（99 条，无省略）：

**(a) `^P1_*` 前缀 26 篇** —— 有任务号段 `P1`，但无类别后缀（`整合清单`/`审查报告`/`研究`/`计划` 等**出现在标题里、不在末尾类别位**），且**全部在 `子agent\` 根层而非 `L1|L2|L3\`**：

| # | 现文件名（位于 `D:\Blockdustry\仓库\docs\子agent\`） | 该去的类别目录（按现名内嵌的类别词） |
|---|---|---|
| 1 | `^P1_中文名修正.md` | 未定（名内无类别词） |
| 2 | `^P1_大规模迁移计划.md` | 未定 |
| 3 | `^P1_批1A_A1整合清单.md` | `仓库\docs\整合清单\`（名内 `整合清单`） |
| 4 | `^P1_批1A_A2整合清单.md` | `仓库\docs\整合清单\` |
| 5 | `^P1_批1A_A3整合清单.md` | `仓库\docs\整合清单\` |
| 6 | `^P1_批1A_审查报告.md` | `仓库\docs\审查\` |
| 7 | `^P1_批1B_container整合清单.md` | `仓库\docs\整合清单\` |
| 8 | `^P1_批1B_itemBridge整合清单.md` | `仓库\docs\整合清单\` |
| 9 | `^P1_批1CD_审查报告.md` | `仓库\docs\审查\` |
| 10 | `^P1_批1C_kiln整合清单.md` | `仓库\docs\整合清单\` |
| 11 | `^P1_批1C_phaseWeaver整合清单.md` | `仓库\docs\整合清单\` |
| 12 | `^P1_批1C_plastaniumCompressor整合清单.md` | `仓库\docs\整合清单\` |
| 13 | `^P1_批1C_pulverizerIncinerator整合清单.md` | `仓库\docs\整合清单\` |
| 14 | `^P1_批1C_pyratiteMixer整合清单.md` | `仓库\docs\整合清单\` |
| 15 | `^P1_批1C_siliconSmelter整合清单.md` | `仓库\docs\整合清单\` |
| 16 | `^P1_批1D_blastDrill整合清单.md` | `仓库\docs\整合清单\` |
| 17 | `^P1_批1D_laserDrill整合清单.md` | `仓库\docs\整合清单\` |
| 18 | `^P1_批1D_pneumaticDrill整合清单.md` | `仓库\docs\整合清单\` |
| 19 | `^P1_批1E_diodeSurgeTower光效研究.md` | `仓库\docs\研究\` |
| 20 | `^P1_批1E_diodeSurgeTower整合清单.md` | `仓库\docs\整合清单\` |
| 21 | `^P1_批1E_powerNodeLargeBatteryLarge整合清单.md` | `仓库\docs\整合清单\` |
| 22 | `^P1_批1E_solarPanel整合清单.md` | `仓库\docs\整合清单\` |
| 23 | `^P1_批1F_copperScrapWall整合清单.md` | `仓库\docs\整合清单\` |
| 24 | `^P1_批1F_door开关动画研究.md` | `仓库\docs\研究\` |
| 25 | `^P1_批1F_titaniumWallDoor整合清单.md` | `仓库\docs\整合清单\` |
| 26 | `^P1_批2A_高级墙体整合清单.md` | `仓库\docs\整合清单\` |
| 27 | `^P1_批2B_menderForceProjector整合清单.md` | `仓库\docs\整合清单\` |
| 28 | `^P1_雷光特效_整合清单.md` | `仓库\docs\整合清单\` |

（上表实为 28 行，`^P1_*` 条目共 **28 篇**：`glob` 枚举逐条核对。）

**(b) `^T*` 前缀 72 篇** —— 有任务号段 `T{n}`，但**无类别后缀、无 L 级后缀**，且全在 `子agent\` 根层：

| # | 现文件名 | 该去的类别目录 |
|---|---|---|
| 1 | `^T1_传送带转角.md` | 未定（名内无类别词） |
| 2 | `^T2_上帝视角.md` | 未定 |
| 3 | `^T3_灵魂出窍.md` | 未定 |
| 4 | `^T4_分裂炮与标签.md` | 未定 |
| 5 | `^T4b_分裂炮贴图尺寸.md` | 未定 |
| 6 | `^T5_炮台附身.md` | 未定 |
| 7 | `^T6_矿机叶片.md` | 未定 |
| 8 | `^T6b_钻头侧面贴图.md` | 未定 |
| 9 | `^T6c_钻头侧面贴图应用.md` | 未定 |
| 10 | `^T6d_钻头真侧面贴图.md` | 未定 |
| 11 | `^T7_装甲机制.md` | 未定 |
| 12 | `^T8_核心正方体.md` | 未定 |
| 13 | `^T8b_核心贴图修复.md` | 未定 |
| 14 | `^T8c_核心分裂黑箱修复.md` | 未定 |
| 15 | `^T9_三维物流.md` | 未定 |
| 16 | `^T9a_贴图镜像与核心碰撞.md` | 未定 |
| 17 | `^T9b_附身黑屏退出修复.md` | 未定 |
| 18 | `^T10_blockhealth深度联系.md` | 未定 |
| 19 | `^T11_传送带贴图.md` | 未定 |
| 20 | `^T11_炮台贴图.md` | 未定 |
| 21 | `^T12_传送带转角颠倒.md` | 未定 |
| 22 | `^T12_核心背面消失.md` | 未定 |
| 23 | `^T12_炮台侧面贴图.md` | 未定 |
| 24 | `^T12_附身俯仰.md` | 未定 |
| 25 | `^T13_传送带侧面.md` | 未定 |
| 26 | `^T13_核心余光剔除.md` | 未定 |
| 27 | `^T13_灵魂出窍应用.md` | 未定 |
| 28 | `^T13_炮台侧面重绘与命名.md` | 未定 |
| 29 | `^T13_附身发射点红线.md` | 未定 |
| 30 | `^T14_火焰炮电弧.md` | 未定 |
| 31 | `^T14_电力整合.md` | 未定 |
| 32 | `^T14_科技树研究.md` | `仓库\docs\研究\`（名内 `研究`） |
| 33 | `^T14_立体物流初步.md` | 未定 |
| 34 | `^T15_Mindustry光效研究.md` | `仓库\docs\研究\` |
| 35 | `^T15_提升机异常.md` | 未定 |
| 36 | `^T15_灵魂出窍误触发.md` | 未定 |
| 37 | `^T15_电弧基座.md` | 未定 |
| 38 | `^T16_提升机底部交接.md` | 未定 |
| 39 | `^T16_灵魂出窍双击深查.md` | 未定 |
| 40 | `^T16_电力节点倒角.md` | 未定 |
| 41 | `^T16_科技树深入实现.md` | 未定 |
| 42 | `^T16_上下坡带.md` | 未定 |
| 43 | `^T17_提升机生效确认.md` | 未定 |
| 44 | `^T17_灵魂出窍C键.md` | 未定 |
| 45 | `^T17_电力节点倒角重做.md` | 未定 |
| 46 | `^T17_上下坡带模型.md` | 未定 |
| 47 | `^T18_创造栏分类.md` | 未定 |
| 48 | `^T18_提升机顶部直连传送带.md` | 未定 |
| 49 | `^T18_科技树实现A.md` | 未定 |
| 50 | `^T18_科技树实现B.md` | 未定 |
| 51 | `^T19_删除爬坡回滚.md` | 未定 |
| 52 | `^T19_提升机顶格Y1输出与底面贴图.md` | 未定 |
| 53 | `^T20_材料迁移与物品源.md` | 未定 |
| 54 | `^T20_科技树树形UI.md` | 未定 |
| 55 | `^T21_BlockHealth整组血量.md` | 未定 |
| 56 | `^T22_材料调整.md` | 未定 |
| 57 | `^T22_科技树解锁指令.md` | 未定 |
| 58 | `^T23_科技树UI精确复刻.md` | 未定 |
| 59 | `^T25_科技树彻底重做.md` | 未定 |
| 60 | `^T26_dagger3D模型.md` | 未定 |
| 61 | `^T35_siliconSmelter冒烟特效研究.md` | `仓库\docs\研究\` |
| 62 | `^T36_kiln火焰特效研究.md` | `仓库\docs\研究\` |
| 63 | `^T38_A_物流生产液体.md` | 未定 |
| 64 | `^T38_B_电力防御炮塔.md` | 未定 |
| 65 | `^T38_C_单位逻辑物品.md` | 未定 |
| 66 | `^T46_pulverizerIncinerator特效研究.md` | `仓库\docs\研究\` |
| 67 | `^T47_phaseWeaver织机特效研究.md` | `仓库\docs\研究\` |
| 68 | `^T49_维修光束力场特效研究.md` | `仓库\docs\研究\` |
| 69 | `^T50_审查修复清单.md` | 未定（名内含 `审查` 但为「审查修复清单」，类别待判） |
| 70 | `^T51_分裂炮台像素绘制研究.md` | `仓库\docs\研究\` |
| 71 | `^T52_待办路线图研究.md` | `仓库\docs\研究\` |
| 72 | `^T9_三维物流.md` | 未定 |

**(c) 无 `^` 前缀、位于 `子agent\` 根层 4 篇**：

| # | 现文件名 | 违规点 | 该去的类别目录 |
|---|---|---|---|
| 1 | `README.md` | 缺任务号 + 缺类别（目录说明档） | `仓库\docs\子agent\`（原地，作为目录说明） |
| 2 | `P2_dsh多智能体契约插件设计v1.md` | 缺类别后缀、任务号段非 `T{n}` | 未定（名内无类别词） |
| 3 | `T28_雷光光效研究.md` | 缺类别后缀 | `仓库\docs\研究\`（名内 `研究`，**属推断**） |

**(d) 位于 `子agent\` 根层、但命名形如 `子agent/L<n>/…` 规范 3 篇（目录位违规）**：

| # | 现文件名 | 现在所在目录 | 该去的类别目录（规范路径） |
|---|---|---|---|
| 1 | `T99_契约流程冒烟测试_T99-冒烟测试审查者通道回报-详细归纳_L1.md` | `D:\Blockdustry\仓库\docs\子agent\`（根层） | `D:\Blockdustry\仓库\docs\子agent\L1\` |
| 2 | `T99_契约流程冒烟测试_T99-冒烟测试审查者通道回报-扩充细节_L2.md` | 同上 | `…\子agent\L2\` |
| 3 | `T99_契约流程冒烟测试_T99-冒烟测试审查者通道回报-高度概括_L3.md` | 同上 | `…\子agent\L3\` |
| 4 | `T99_契约流程冒烟测试_T99-冒烟测试研究员回报_L3.md` | 同上 | `…\子agent\L3\` |

> 备注（事实）：`仓库\docs\子agent\L1\`、`L2\`、`L3\` 三个目录**已存在**，各自已含 1 篇 T53 产出档；
> 上述 4 篇 T99 档仍在根层，**未**进入 L1|L2|L3。

---

## 5. `^` 前缀旧标记：docs 树内全部条目（逐条绝对路径）

**总数 126 个** = 根层 19 + `子agent\` 根层 107（99 md + 34 json 中的 107 个 `^` 项，见下）。
**不存在**以 `^` 开头的**目录**（`glob` 未返回任何 `^` 目录；126 条全为文件）。

### 5.1 根层（19，同第 2.1 节，略）

`D:\Blockdustry\仓库\docs\修改\钻头侧面贴图应用.md`、`…\^单位工厂与单位.md`、`…\^电力系统.md`、`…\^研究-3x3核心与工厂.md`、`…\^研究-Jade联动.md`、`…\^研究-Mindustry各类条.md`、`…\^研究-Mindustry资源栏样式与HUD修复.md`、`…\^研究-Mindustry进度条样式.md`、`…\^研究-PowerNode光效.md`、`…\^研究-PowerNode激光.md`、`…\^研究-传送带物品方向.md`、`…\^研究-单位工厂修复.md`、`…\^研究-机器动画粒子.md`、`…\^研究-核心与队伍共享资源.md`、`…\^研究-核心染色修复.md`、`…\^研究-炮塔动画.md`、`…\^研究-炮弹卡顿.md`、`…\^研究-物品源方块与资源栏.md`、`…\^研究-电力节点实现.md`

### 5.2 `子agent\` 根层（107 = 28 个 `^P1_*` + 72 个 `^T*` + 34 个 `^P1_*_lang_*.json` 中的 34 … 校验见下）

`子agent\` 根层 `^` 条目实测：`^P1_*` **28 个 md** + `^T*` **72 个 md** + `^P1_*_lang_*.json` **34 个 json** = **134**。

> **订正**：上一段「总数 126」为初算，**以本节复核为准**——`^` 条目合计 **153 个**：
> 根层 19 + `子agent\` 根层 134 = **153**（19 md + 134 项）。
> 逐条 md 名称见第 2.1 节（19 条）与第 4.3 节 (a)(b)（28 + 72 = 100 条）；json 逐条见第 3.2 节（34 条）。
> **153 个 `^` 文件、0 个 `^` 目录**。

---

## 6. 缺失的类别目录确认

用 `read <目录>` 判据逐条核对（`not found` = 不存在）：

| 目录 | 判据返回 | 结论 |
|---|---|---|
| `D:\Blockdustry\仓库\docs\研究\` | `not found` | **确实不存在** |
| `D:\Blockdustry\仓库\docs\审查\` | `not found` | **确实不存在** |
| `D:\Blockdustry\仓库\docs\整合清单\` | `not found` | **确实不存在** |
| `D:\Blockdustry\仓库\docs\修改\` | `not found` | **确实不存在** |
| （对照）`D:\Blockdustry\仓库\docs\坑\` | `not a regular file` | 存在 |
| （对照）`D:\Blockdustry\仓库\docs\核心数据库\` | 有 1 文件返回 → 存在 | 存在 |
| （对照）`D:\Blockdustry\仓库\docs\子agent\` | `not a regular file` | 存在 |
| （对照）`D:\Blockdustry\仓库\docs\archive\` | `not a regular file` | 存在（空） |

旁证：`.agent-contract\panel.json` L419-L424 自述「重探后发现缺 4 处结构 —— 缺少 `仓库/docs/研究/`、`仓库/docs/审查/`、`仓库/docs/整合清单/`、`仓库/docs/修改/`」，与本轮实测**一致**。

---

## 7. `archive\` 递归现状

`D:\Blockdustry\仓库\docs\archive\`：

| 层级 | 内容 | 证据 |
|---|---|---|
| `archive\` 本身 | **目录存在** | `read D:\Blockdustry\仓库\docs\archive` → `not a regular file` |
| `archive\` 直下文件 | **0 个** | `glob 仓库/docs/archive/**/*` → No files found；`glob 仓库/docs/archive/*/*` → No files found；全树 `glob 仓库/docs/**/*` 结果里无任何 `archive\` 路径 |
| `archive\2026-10\` | **不存在**（本轮实测 `not found`，见第 1.2 节相关判据同源；且 `glob` 无命中） | — |
| `archive\2026-08\` | **不存在**（同判据；且无任何文件命中） | — |

> 事实留痕：`D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md` L591-L593 记录了 `2026-10-03T13:05:48.019Z [馆员搬移] 仓库/docs/archive/2026-10/… → 原路径`（3 条），
> 即**当天曾有 3 篇文件被搬入 `archive\2026-10\` 又被搬回**；本轮盘到的现场是「`archive\` 目录在、里面什么都没有」。

---

## 8. 引用面：docs 树内互相引用其它 md 的文件统计

### 8.1 总数

- **91 个 md 文件**在正文/头部中含**其它 md 文件名**（含 `.md` 字面量）。
- 命中行总计 **590 处**（`grep` 报 `Found 250 of 590 matches`，即全量 590 行；`grep` 另给出「完整结果已落盘」的溢出文件）。
- 统计口径（**已声明局限**）：
  - 判据 = 文件内容里出现 `[A-Za-z0-9\u4e00-\u9fff\-]+\.md`（以及更宽的 `[A-Za-z0-9\u4e00-\u9fff\^\-\.\\/]+\.md`）；两种正则命中的**文件集合完全一致（均 91 个）**。
  - **不覆盖**：写成裸目录（如 `仓库\docs\子agent`）或非 `.md` 后缀的引用；不含 `D:\Blockdustry\Jade\...\docs/plugins22/getting-started.md`（仓外文档）；带空格文件名会**多计**邻近片段。
  - `grep` 只报「命中所在行」，同一行多处引用计为 1 行。

### 8.2 典型 5 例（含绝对路径与行号）

| # | 引用方（绝对路径） | 被引用方 | 证据行号 |
|---|---|---|---|
| 1 | `D:\Blockdustry\仓库\docs\修改\钻头侧面贴图应用.md` | `docs/研究-渲染与模型坑.md`；`docs/研究-炮管黑.md`、`docs/研究-PowerNode激光黑色.md` | L39、L40 |
| 2 | `D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md` | `docs/子agent/^P1_大规模迁移计划.md`（L9、L234）、`docs/子agent/^P1_中文名修正.md`（L65）、`docs/子agent/^T38_B_电力防御炮塔.md`（L292）、`^T38_A/^T38_B/^T38_C`（L550-L552）、自身固定路径（L554）、changelog 内 31 条路径（L564-L594） | L9、L65、L234、L292、L550-L552、L554、L564-L594 |
| 3 | `D:\Blockdustry\仓库\docs\坑\渲染与模型坑索引.md` | `docs/坑/README.md`（L6、L10）+ 8 个坑档裸文件名（L11-L18：`BER渲染.md`、`方块模型.md`、`碰撞箱.md`、`贴图.md`、`队伍染色.md`、`附身与相机.md`、`Jade联动.md`、`炮弹与实体.md`） | L6、L10-L18 |
| 4 | `D:\Blockdustry\仓库\docs\核心数据库\派生态索引.md` | **125 行**内逐条登记 `D:\Blockdustry\仓库\docs\子agent\*.md` 绝对路径（L19-L124 共 106 条文档条目）+ L5「真身」路径 | L5、L19-L124 |
| 5 | `D:\Blockdustry\仓库\docs\坑\炮塔黑色阴影2.md` | `docs/研究-PowerNode激光黑色.md`（L85）、`docs/研究-炮塔黑色阴影.md`（L92、L161）、`docs/研究-机器侧面贴图.md`（L136、L162）、`docs/研究-炮管黑.md` 类同项目（L163） | L85、L92、L136、L161-L163 |

### 8.3 其它值得记录的引用事实（同为事实，不含建议）

- `D:\Blockdustry\仓库\docs\坑\README.md`：以**相对裸文件名**自引用同目录 16 个坑档（主题表 L10-L19、黑名单 L23-L27、索引区块 L42-L57 共 16 条 `[名](名.md)`）。
- `D:\Blockdustry\仓库\docs\坑\API签名与编译.md`：引用 `docs/子agent/T16_科技树深入实现.md`（L14）与 `docs/子agent/T18_科技树实现A.md`（L32）—— **这两个路径的 `^` 前缀在现名中有、引用里没有**（现名分别为 `^T16_科技树深入实现.md`、`^T18_科技树实现A.md`）。
- `D:\Blockdustry\仓库\docs\坑\物流交接.md` L16 引用 `^T19_删除爬坡回滚.md`；L37 引用 `docs/子agent/T14_立体物流初步.md`、`T16_提升机底部交接.md`、`T18_提升机顶部直连传送带.md`、`T19_提升机顶格Y1输出与底面贴图.md`（**同样缺 `^` 前缀**）。
- `D:\Blockdustry\仓库\docs\产出\T28_雷光光效研究.md` L292 引用 `坑-炮管黑.md`（现真身为 `D:\Blockdustry\仓库\docs\坑\炮管黑.md`）。
- `D:\Blockdustry\仓库\docs\子agent\L1|L2|L3\T53_*` 三档自身的 `relatedFiles` / `nextTier` / `fullDetail` 也是互引来源之一（L1 档 L6 单行含 28 个路径片段）。

---

## 附：工具边界与未验证项（如实记录）

1. **文件字节大小全部未验证**：本会话无 bash，`read`/`glob`/`grep` 均不返回 size；上级要求的「绝对路径 + 大小 + 扩展名」中，**大小这一列无法给出**，其余两列已完整。
2. **空目录无法被 `glob` 枚举**：`archive\` 及其可能的分片子目录用 `read <目录>` 的 `not a regular file` / `not found` 判据区分，判据本身是本轮自定义的间接证据（**非官方语义，属未验证判据**，但与其它事实自洽）。
3. **`坑\` 条目数存在多口径**：本轮实测 18 个文件名；任务卡 `任务\T53_docs规范化整理.md` L17 提到「`坑/` 16 篇」、L28 口径为「`坑` 主题档」，L3 档 L29 记「`坑\` 18 篇」。**本轮只报实测 18**。
4. **`^` 条目总数在本文档内有订正痕迹**：第 5 节最终口径 = **153 个 `^` 文件**（19 + 134），正文保留了推算过程与订正说明，以 153 为准。
5. 未做任何跨目录比对之外的解释，未给整理建议（按上级硬性纪律）。
