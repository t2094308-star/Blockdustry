---
taskId: organize-docs-blockdustry
role: librarian
tier: 2
keywords: ["docs整理dryRun预演","dryRun 预演","分类归位","命名与目录对齐","归档属性","临时文件挪走","去 ^ 前缀","类别目录","archived true","archive YYYY-MM","34 个 json","docs/_临时","legacy 无头旧档","tier 0","任务号判不出","档级不可派生","引用同步","librarian_relocate","librarian_sweep","librarian_archive","搬迁","归档","命名","引用","ag","34","rd","根层","目录","移动","数据"]
relatedFiles: ["D:\\\\Blockdustry\\\\仓库\\\\docs","D:\\\\Blockdustry\\\\仓库\\\\docs\\\\产出","D:\\\\Blockdustry\\\\仓库\\\\docs\\\\坑","D:\\\\Blockdustry\\\\仓库\\\\docs\\\\核心数据库\\\\派生态索引.md","D:\\\\Blockdustry\\\\仓库\\\\docs\\\\核心数据库\\\\核心数据库.md","D:\\\\Blockdustry\\\\仓库\\\\docs\\\\archive","D:\\\\Blockdustry\\\\仓库\\\\docs\\\\_临时","D:\\\\Blockdustry\\\\仓库\\\\docs\\\\产出\\\\L1\\\\organize-docs-blockdustry_docs整理dryRun预演_L1.md","D:\\\\Blockdustry\\\\任务\\\\organize-docs-blockdustry.md","EXP-RELOCATE_..._坑.md","T52_待办路线图研究.md","P2_..._v1.md","README.md","修改-钻头侧面贴图应用.md","研究-渲染与模型坑.md","渲染与模型坑索引.md","_审查报告.md","整合清单.md","传送带上下坡.md","T16_上下坡带.md","T17_上下坡带模型.md","project.json","派生态索引.md","T3_灵魂出窍.md","炮塔黑色阴影.md","D:\\\\Blockdustry\\\\仓库\\\\docs\\\\产出\\\\T14_火焰炮电弧.md"]
createdAt: 2026-10-03T13:56:59.953Z
librarianTouchedAt: 2026-10-04T06:16:24.832Z
librarianChanges: ["订正 ^ 的表述（归档语义已改到元数据）","复核 keywords（新增 0 个）","引用修复（指向现存档）","复核 keywords（新增 0 个）","引用修复（指向现存档）","复核 keywords（新增 1 个：数据）"]
fullDetail: D:\Blockdustry\仓库\docs\产出\L1\organize-docs-blockdustry_治理预演报告（sweep-dryRun-+-类别索引清单-·-零落盘）_L1.md
---

> **速览** 本项目：Blockdustry
> **速览** 本档（L2）给：需要细节的执行者
> **速览** 细节见 D:\Blockdustry\仓库\docs\产出\L1\organize-docs-blockdustry_docs整理dryRun预演_L1.md
# organize-docs-blockdustry —— docs 整理 dryRun 预演（二级文档 / 发主代理）

> 一级文档（逐条细账、真值引用面、规范缺口逐组判定）：`D:\Blockdustry\仓库\docs\产出\L1\organize-docs-blockdustry_docs整理dryRun预演_L1.md`
`^` 前缀**已废弃**：归档语义写在文档头部 `archived: true`（不再靠文件名前缀）

## 结论

**预演全出齐，158 项待搬（124 md + 34 json）、冲突 0、可归档 3 篇、需新建 4 个类别目录，本轮零落盘。** 但 **118 条命名违规里任务号只有 100 条判得出、档级一条都判不出**，所以「补类别后缀 / 补 `_L<n>`」本轮**一条也执行不了**——这是最大的阻塞点。

| 动作 | 项数 | 冲突 | 工具报影响引用 |
|---|---|---|---|
| 根层 → `研究\` | 18 | 0 | 18 |
| 根层 → `修改\` / `坑\` / `核心数据库\` | 3 | 0 | 8 |
| `AG\` 去 `^` 原地改名 | 99 | 0 | 0 |
| `AG\T99_*_L*` → `AG\L1|L2|L3\` | 4 | 0 | 6 |
| 34 个 `_lang_*.json` → `TMP` | 34 | 0 | 34 |
| **合计** | **158** | **0** | **66** |
| 归档 → `archive/<YYYY-MM>/` | 3 | 0 | — |

## 依据

**目录与文件（本轮 glob 实测）**：`RD\` 根层 23 md（19 带 `^` + 4 不带；比研究员报的 22 多 1，是本次任务自己在根层新增的盘点档）· `RD\坑\` 18 md · `AG\` 根层 106 md + 34 json · `AG\L1|L2|L3\` 3+2+2 = 7 md（目录**已存在**）· `RD\核心数据库\` 1 md · `RD\archive\` **0（空）** · 合计 **155 md + 34 json = 189**（研究员报 184/150 md，md 少 5）。非 `.md` **只有 34 个 json**（17 组 `^P1_*_lang_{en,zh}.json`），`.png/.tmp/.bak/.py/.txt/.log` 命中 0。

**元数据现状**：全树只有 **15 篇有 front-matter**（4 篇 T99、4 篇本次盘点档、3 篇 T53 预演档、`EXP-RELOCATE_..._坑.md`、`AG\^T52_待办路线图研究.md`、`AG\P2_..._v1.md` 且 `taskId: null`）；**其余 140 篇是 legacy 无头旧档**。`^` 条目 **152 个**（根层 19 + `AG\` 99 + json 34；研究员报 153，其 `^T* md` 计 72、实测 71）。

**关键机制裁定**：`AG\` 99 篇是 `AG\README.md` L3/L5-23 定义的「自有单档格式」（目标 / 结论·产出 / 占用与交接 / 异常），**不是三档产出档** → 一律留 `tier: 0`，不猜 L1/L2/L3、不进 `L*/`。先例：`AG\^T52_待办路线图研究.md` 头部已是 `tier: 0`，其 `librarianChanges` 自述「tier 判不出档级，按派生器旧档约定记 0 并上报」。

## 细节（变量 / 对象 / 状态）

**① 分类归位**：根层 `^研究-*` 16 篇 + `^单位工厂与单位` + `^电力系统` → `RD\研究\`（去 `^`、去冗余 `研究-`）；`^修改-钻头侧面贴图应用.md` → `RD\修改\钻头侧面贴图应用.md`；`研究-渲染与模型坑.md` → `RD\坑\渲染与模型坑索引.md`（它是坑库索引/重复档，不是研究档）。

**② 需新建目录 4 个**：`RD\研究\`、`RD\审查\`、`RD\整合清单\`、`RD\修改\`（均不存在）。⚠️ `审查\`/`整合清单\` **本轮无迁入对象**——真正的审查报告与整合清单是 `AG\^P1_批*_审查报告.md`（2 篇）与 `AG\^P1_批*_*整合清单.md`（28 篇），属子 agent 产出档，按规范留 `AG\`；建议这两个目录惰性创建或登记为预留类别，否则造出两个永久空目录。

**③ 归档属性**：候选 3 篇（只认正文自标废弃）：`RD\传送带上下坡.md`（L3 自标 2026-08-13）、`AG\^T16_上下坡带.md`、`AG\^T17_上下坡带模型.md`（后两篇 L3 自标「已废弃（T19 回滚）」）。`monthOf: obsolete` 下前者落 `archive\2026-08\`、后两者落 `archive\2026-10\`——**同批落两个月**；`monthOf: now` 则全落 `2026-10`。`.agent-contract\project.json` **无 `audit` 段 → `audit.archiveAfterDays` 读不到**，档龄边界不可判。工具会写入头部 `archived: true` / `archivedAt` 并重写整块 YAML。

**④ 临时文件**：34 json → `TMP\<原名>`，17 组（`批1A_A3`、`批1B_container`、`批1B_itemBridge`、`批1C_phaseWeaver`、`批1C_plastaniumCompressor`、`批1C_pulverizerIncinerator`、`批1C_pyratiteMixer`、`批1C_siliconSmelter`、`批1D_blastDrill`、`批1D_laserDrill`、`批1D_pneumaticDrill`、`批1E_diodeSurgeTower`、`批1E_powerNodeLargeBatteryLarge`、`批1F_copperScrapWall`、`批1F_titaniumWallDoor`、`批2A_高级墙体`、`批2B_menderForceProjector`），只搬不删。⚠️ 落点在 `RD\` 之外，会切断 34 处登记引用与台账内路径，文件名还带废弃的 `^`。

**⑤ 规范缺口（118 条）**：`AG\` 99 条 任务号 99/99 可判、档级 0/99 可判；`坑\` 18 条 任务号 1/18 可判（仅 `EXP-RELOCATE_..._坑.md`，且它的名字恰好已合规）、17 条主题档无任务号；`核心数据库\` 1 条（`派生态索引.md`，派生物）0 可判。**合计：100 可判 / 18 不可判；档级 0 可判 / 118 不可判。** 另根层 23 篇审计记为 `doc_misfiled` 不计入这 118，任务号 0/23 可判。

**⑥ 影响面真值（工具回执偏窄，只认完整路径字面量）**：工具报 relocate 66 处；人工 grep 实查根层 `^研究-*` 正文真实引用 ≥ 5 文件 6 处（`AG\^T3_灵魂出窍.md` L227、`RD\坑\炮塔黑色阴影.md` L59/L214、`AG\D:\Blockdustry\仓库\docs\产出\T14_火焰炮电弧.md` L94、`^修改-钻头…` L39/L40）；`研究-渲染与模型坑.md` 被 6 文件 7 处引；`D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md` 8 文件 9 处。**本次任务的盘点档自身也是引用方**（`RD\D:\Blockdustry\仓库\docs\产出\L1\organize-docs-blockdustry_docs整理前现状_全量逐条清单.md` L41/L222 + `AG\L1|L2\` 两篇），落地时必须同批改。

**⑦ 工具链校准**：`librarian_archive` 本次签名**已带 `dryRun`（默认 true）**，与上一轮 T53「无 dryRun、误调即真落盘 3 篇」不同；本轮显式传 `dryRun: true`，事后 glob 校验 `RD\archive\` 仍为空 ✓。**但事故先例仍在，落地时必须显式传参。**

## 风险与待确认

1. **`RD\D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md` 移动政策**：移动破正文 L554 自述固定路径 + 8 文件 9 处引用；三口径（移动+改 1 行 / 移动+不改 / 不移动登记例外）待拍板 —— 预演已含该项，**建议本轮不移动**。
2. **归档 vs 去 `^` 相斥**：`^T16/^T17` 两篇同时出现在两份清单里，须先定顺序（先归档则不必去 `^`）。
3. **归档分片口径**：同批落两月；`archiveAfterDays` 缺失。
4. **json 落点**：搬出 `RD\` 会断 34 处引用，且是否同时去 `^` 未定。
5. **legacy 任务号来源未定** → 118 条命名违规一条都改不了。选项：(a) 从 `任务\` 反查任务卡回填；(b) 登记为规范例外，`AG\` 只去 `^`；(c) 视为 L1 让派生器兜底。
6. **`审查\` / `整合清单\` 空目录**是否建。
7. 只上报未改：`RD\坑\` 17 篇主题档命名、`RD\坑\README.md`、`RD\核心数据库\派生态索引.md`（派生物）、`AG\README.md`、`AG\P2_..._v1.md`、`AG\T28_雷光光效研究.md`（建议归 `研究\` 但会断 2 处引用）、`坑\炮塔黑色阴影.md` vs `炮塔黑色阴影2.md` 去重、6 个旧名断引用、`TMP\EXP-RELOCATE-实验\` 6 篇、`AG\L1|L2|L3\` 7 篇、本次任务 5 篇盘点档。

## 下一步

1. 上级把风险 1~6 拿去给用户确认，重点是「legacy 任务号从哪来」。
2. 拍板后落地顺序：建 4 目录 → `T99` 四篇归位 → `AG\` 99 篇去 `^` → 根层 19+3 篇归位 → 34 json 搬迁 → 补引用（含盘点档自身）→ `sweep` 落地索引与台账。
3. 归档单列一批，先定 `archiveAfterDays` 与分片口径。
4. 建议把「坑档命名例外」「`审查/整合清单` 空目录」直接登记成规范例外，否则每轮审计重复报。
