---
taskId: organize-docs-blockdustry
role: researcher
tier: 2
keywords: ["docs整理前现状盘点-扩充细节","docs盘点","目录清单","整理前现状","违规命名","^前缀","archive空目录","引用面","类别目录缺失","非md临时文件","Blockdustry","目录","存在","153","大小","返回","清单","上级","条目"]
relatedFiles: ["D:\\Blockdustry\\仓库\\docs","D:\\Blockdustry\\仓库\\docs\\产出\\L1\\organize-docs-blockdustry_docs整理前现状_全量逐条清单.md","D:\\Blockdustry\\仓库\\docs\\产出","D:\\Blockdustry\\仓库\\docs\\坑","D:\\Blockdustry\\仓库\\docs\\核心数据库","D:\\Blockdustry\\仓库\\docs\\archive","D:\\Blockdustry\\仓库\\docs\\_临时","D:\\Blockdustry\\.agent-contract\\panel.json","D:\\Blockdustry\\仓库\\docs\\修改\\钻头侧面贴图应用.md","D:\\Blockdustry\\仓库\\docs\\核心数据库\\核心数据库.md","D:\\Blockdustry\\仓库\\docs\\坑\\渲染与模型坑索引.md","D:\\Blockdustry\\仓库\\docs\\核心数据库\\派生态索引.md","D:\\Blockdustry\\仓库\\docs\\坑\\炮塔黑色阴影2.md","D:\\Blockdustry\\仓库\\docs\\产出\\L3\\organize-docs-blockdustry_docs整理前现状盘点-高度概括_L3.md","D:\\Blockdustry\\仓库\\docs\\产出\\L1\\organize-docs-blockdustry_docs整理前现状盘点-详细归纳_L1.md","panel.json","D:\\Blockdustry\\仓库\\docs\\archive\\2026-08\\传送带上下坡.md","_坑.md","研究-炮管黑.md","研究-PowerNode激光黑色.md","D:\\Blockdustry\\仓库\\docs\\产出\\P1_大规模迁移计划.md","D:\\Blockdustry\\仓库\\docs\\产出\\P1_中文名修正.md","README.md","研究-炮塔黑色阴影.md","研究-机器侧面贴图.md","_L2.md"]
createdAt: 2026-10-03T13:53:07.874Z
librarianTouchedAt: 2026-10-04T06:16:24.832Z
librarianChanges: ["引用更新（搬移）","引用更新（搬移）","馆员搬移","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","复核 keywords（新增 0 个）","引用修复（指向现存档）","复核 keywords（新增 0 个）","引用修复（指向现存档）","复核 keywords（新增 0 个）","引用修复（指向现存档）","复核 keywords（新增 0 个）"]
fullDetail: D:\Blockdustry\仓库\docs\产出\L1\organize-docs-blockdustry_治理预演报告（sweep-dryRun-+-类别索引清单-·-零落盘）_L1.md
---

> **速览** 本项目：Blockdustry
> **速览** 本档（L2）给：需要细节的执行者
> **速览** 细节见 D:\Blockdustry\仓库\docs\产出\L1\organize-docs-blockdustry_docs整理前现状盘点-详细归纳_L1.md
# docs 整理前现状（扩充细节）

## 结论

`D:\Blockdustry\仓库\docs\` 全树实测 **184 个文件 = 150 个 .md + 34 个 .json**；文件级顶层目录 4 个；`子agent\L1|L2|L3\` 三个子目录**已存在**；`archive\` **目录存在但为空**；`研究\`、`审查\`、`整合清单\`、`修改\` **四个类别目录确实不存在**（`read` 返回 `not found`，与 `panel.json` L419-L424 自述一致）；**全树非 .md 文件仅 34 个 .json**（无 png/tmp/bak/py/txt/log）；`^` 前缀条目 **153 个（全部是文件，无 `^` 目录）**；**91 个 md** 正文互引其它 md，命中 590 行。逐条清单见本目录根层明细档。

## 依据（8 项盘点，逐项给数）

| # | 盘点项 | 实测结果 | 关键证据 |
|---|---|---|---|
| 1 | 目录结构与每层文件数 | 根层 22（22 md/0 json）；`子agent\` 根层 133（99 md/34 json）；`L1\`1、`L2\`1、`L3\`1；`坑\`17；`核心数据库\`1；`archive\`0 | `glob 仓库/docs/**/*` → 184 条 |
| 2 | 根层 `^` 前缀 md | 根层共 22 篇 = **19 篇 `^`** + 3 篇无 `^`（`研究-渲染与模型坑.md`、`D:\Blockdustry\仓库\docs\archive\2026-08\传送带上下坡.md`、`核心数据库.md`）；逐篇见明细档 §2 | 同上枚举 |
| 3 | 非 md 临时文件 | **34 个，全部 `.json`**，全部在 `子agent\` 根层，17 组 en/zh 配对，命名 `^P1_批<批次>_<英名>_lang_{en,zh}.json`；`.tmp/.bak/.png/.py/.txt/.log/.csv/.zip` 命中 **0** | 全树枚举 + `glob 仓库/docs/**/*.png` → No files found |
| 4 | 三目录违规命名 | `坑\` **18/18 违规**（17 篇主题/索引档无 `_坑` 后缀 + `EXP-RELOCATE_…_坑.md` 有后缀但任务号段非 `T{n}`）；`核心数据库\` **1/1 违规**（`派生态索引.md` 无任务号无类别后缀）；`子agent\` 根层 **99/99 违规** | 逐条见明细档 §4 |
| 5 | `^` 前缀全树条目 | **153 个** = 根层 19 + `子agent\` 根层 134（28 个 `^P1_*` md + 72 个 `^T*` md + 34 个 json）；**无 `^` 目录** | 184 条减去 31 条非 `^` 项校验通过 |
| 6 | 四个类别目录确认 | `研究\`、`审查\`、`整合清单\`、`修改\` 全部 `not found` → **确实不存在** | `read` 判据 |
| 7 | `archive\` 递归现状 | `archive\` 存在，**直下 0 文件**；`archive\2026-10\`、`archive\2026-08\` 均无命中；`核心数据库.md` L591-L593 留有 3 条「搬入 `archive/2026-10/` 又搬回」的 changelog | 全树枚举 + changelog |
| 8 | 引用面（互引 md） | **91 个 md 互引**，命中 **590 行**；`核心数据库\派生态索引.md` 单档 L19-L124 即登记 106 条绝对路径 | `grep` 命中文件标题行去重 |

**典型 5 例（含行号）**：
1. `D:\Blockdustry\仓库\docs\修改\钻头侧面贴图应用.md` L39/L40 → `docs/研究-渲染与模型坑.md`、`docs/研究-炮管黑.md`、`docs/研究-PowerNode激光黑色.md`；
2. `D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md` L9/L65/L234/L292/L550-552/L554/L564-594 → `^D:\Blockdustry\仓库\docs\产出\P1_大规模迁移计划.md`、`D:\Blockdustry\仓库\docs\产出\P1_中文名修正.md`、`^T38_A/B/C_*.md`、自身路径、31 条 changelog 路径；
3. `D:\Blockdustry\仓库\docs\坑\渲染与模型坑索引.md` L6/L10-L18 → `docs/坑/README.md` + 8 个坑档裸文件名；
4. `D:\Blockdustry\仓库\docs\核心数据库\派生态索引.md` L5/L19-L124 → 106 条 `子agent\*.md` 绝对路径；
5. `D:\Blockdustry\仓库\docs\坑\炮塔黑色阴影2.md` L85/L92/L136/L161-163 → `docs/研究-PowerNode激光黑色.md`、`docs/研究-炮塔黑色阴影.md`、`docs/研究-机器侧面贴图.md`。

**状态/变量（盘点期间的可变事实）**：
- 本轮判断「目录是否存在」的判据 = `read <目录>`：`not a regular file` → 存在；`not found` → 不存在（**间接判据，未验证**）。
- `子agent\EXP-RELOCATE-搬入\`：现为 `not found`（changelog 中曾出现）。
- `docs\_临时\EXP-RELOCATE-实验\`（**在 docs 树之外**）：已存在，含 6 篇 `_L2.md` 实验档，**无 json/临时文件**。
- 引用里缺 `^` 的实例：`坑\API签名与编译.md` L14/L32 → `docs/子agent/T16_科技树深入实现.md`、`T18_科技树实现A.md`；`坑\物流交接.md` L37 → `T14/T16/T18/T19_*` 四条（现盘上这些真身都带 `^`）。

## 风险与待确认

1. **文件字节大小全部未验证**：本会话无 bash，`read`/`glob`/`grep` 均不返回 size → 上级要求的「路径 + 大小 + 扩展名」中**大小列无法提供**。
2. **空目录判据为间接证据**，非官方语义；`archive\` 为空的结论由「`read` 返回 + `glob` 无命中」双证支撑。
3. **`坑\` 条目数多口径**：任务卡 L17 记「16 篇」、上轮 L3 档 L29 记「18 篇」；本轮实测 **18**。
4. **`^` 条目数存在档内订正**：初算 126 → 复核 **153**（以 153 为准）。
5. 本档**只报事实、不含整理建议与裁定**；「该去的类别目录」仅为按上级给定后缀的机械映射。

## 下一步

- 交上级按本清单取舍与裁定；本任务不再产出判断类内容。

## 附：三档文档路径

- 三级（高度概括）：`D:\Blockdustry\仓库\docs\产出\L3\organize-docs-blockdustry_docs整理前现状盘点-高度概括_L3.md`
- 一级（详细归纳）：`D:\Blockdustry\仓库\docs\产出\L1\organize-docs-blockdustry_docs整理前现状盘点-详细归纳_L1.md`
- 全量逐条明细（附件档）：`D:\Blockdustry\仓库\docs\产出\L1\organize-docs-blockdustry_docs整理前现状_全量逐条清单.md`
