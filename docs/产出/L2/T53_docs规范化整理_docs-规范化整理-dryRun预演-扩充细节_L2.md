---
taskId: T53_docs规范化整理
role: librarian
tier: 2
keywords: ["docs规范化整理_docs-规范化整理-dryRun预演-扩充细节","docs-规范化整理-dryRun预演-扩充细节","docs 规范化","T53","dryRun 预演","分类归位","去 ^ 前缀","legacy 档","tier 0","librarian_sweep","librarian_relocate","librarian_archive","archived true","归档分片","临时文件 _临时","引用更新","断引用复核","坑库命名","核心数据库固定路径","搬迁引用同步","回滚","changelog","引用","dryrun","l1","核心","据库","数据","索引","心数"]
relatedFiles: ["D:\\Blockdustry\\任务\\T53_docs规范化整理.md","D:\\Blockdustry\\仓库\\docs","D:\\Blockdustry\\仓库\\docs\\产出","D:\\Blockdustry\\仓库\\docs\\坑","D:\\Blockdustry\\仓库\\docs\\核心数据库\\核心数据库.md","D:\\Blockdustry\\仓库\\docs\\核心数据库\\派生态索引.md","D:\\Blockdustry\\仓库\\docs\\_临时","D:\\Blockdustry\\.agent-contract\\backups\\T53-move-2026-10-03\\move-manifest.csv","D:\\Blockdustry\\仓库\\docs\\产出\\L1\\T53_docs规范化整理_docs-规范化整理-dryRun预演-逐条搬迁表_L1.md","agent\\README.md","lang\\zh_cn.json","EXP-RELOCATE_搬迁引用同步实验全过程_坑.md","研究-渲染与模型坑.md","派生态索引.md","_L2.md","方块模型.md","贴图.md","物流交接.md","BER渲染.md","API签名与编译.md","D:\\Blockdustry\\仓库\\docs\\坑\\碰撞箱.md","修改-钻头侧面贴图应用.md","D:\\Blockdustry\\仓库\\docs\\坑\\渲染与模型坑索引.md","D:\\Blockdustry\\仓库\\docs\\archive\\2026-08\\传送带上下坡.md","L3.md","T52_待办路线图研究.md"]
createdAt: 2026-10-03T13:07:11.104Z
librarianTouchedAt: 2026-10-04T06:16:24.832Z
librarianChanges: ["订正 ^ 的表述（归档语义已改到元数据）","复核 keywords（新增 0 个）","引用修复（指向现存档）","复核 keywords（新增 0 个）","引用修复（指向现存档）","复核 keywords（新增 0 个）"]
fullDetail: D:\Blockdustry\仓库\docs\产出\L1\T53_docs规范化整理_docs-规范化整理-dryRun预演-逐条搬迁表_L1.md
---

> **速览** 本项目：Blockdustry
> **速览** 本档（L2）给：需要细节的执行者
> **速览** 细节见本档 front-matter 的指针（fullDetail 指向 L1，另一档生成后由工具自动补齐）
# T53 docs 规范化整理 —— dryRun 预演（扩充细节）

## 结论

本轮四份预演全部出齐，**除我自己的三档产出档外零落盘**（详见「风险与待确认」第 0 条的例外说明）。

| 工具 | 预演结果 |
|---|---|
| `librarian_sweep --dryRun` | 待办 **120 条**（红 20 / 黄 45 为审计计数）；将归位 **5**、待人工裁定 **261**、将覆盖 **3**、归档 0、去重 0、**改名 0**、**tags 过期 4**（盖章 0）、审计 红 20→20 / 黄 45→45（无增无减）；验收面：将修复引用 **78** 篇 / 待人工裁定 261 篇 / 将标注 100 篇 / 将归位 5 篇 |
| `librarian_relocate --dryRun`（分类归位+命名对齐） | **119 项待搬**：研究 18 + 修改 1 + 坑 1 + 核心数据库 1 + 子agent 原地改名 98（99 篇去 `^` 中的 98 篇，另 1 篇见裁定点）+ 归档态 1；**冲突 0**；影响引用 **8 处**（T99 四篇 6 处 + 核心数据库 1 + 传送带上下坡 1） |
| 非文档临时文件 `--dryRun` | **34 个 `_lang_*.json` → `D:\Blockdustry\docs\_临时\`**：冲突 0、影响引用 **0 处**（工具口径）；但**人工 grep 查到 10 文件 14 处**路径字面量引用（任务卡 5 篇 + 整合清单 5 篇），**工具没报，属盲区** |
| `librarian_archive` | **该工具没有 `dryRun` 参数**——首次调用即真落盘 3 篇，我已**全部回滚**（见 L1 第一节），归档结论以「机制问答 + 3 篇候选清单」形式交付 |

## 依据

### 一、现场账本（本轮实测）

| 位置 | 实测 | 与上轮对比 |
|---|---|---|
| `仓库/docs/` 根目录 | 22 篇 .md（19 篇带 `^`） | 同 |
| `仓库/docs/子agent/` | 99 篇 `^` legacy + `T28_*` + `P2_*设计v1` + `README` + 4 篇 `T99_*_L*` = 106 篇 .md；**34 个 `_lang_*.json`** | 同 |
| `仓库/docs/坑/` | **18 篇** = 16 主题档 + `README.md` + **`EXP-RELOCATE_搬迁引用同步实验全过程_坑.md`**（上轮计入「17 篇」的 `研究-渲染与模型坑.md` 其实在根目录） | 坑库多 1 篇 EXP 实验档，且**不在 README 索引区块里** |
| `仓库/docs/核心数据库/` | 1 篇 `派生态索引.md` | 同 |
| `仓库/docs/archive/` | **本轮回滚后恢复为不存在** | 上轮「不存在」 |
| `D:\Blockdustry\docs\_临时\` | **已存在**，含 `EXP-RELOCATE-实验\` 6 篇 `_L2.md` | 上轮不存在 → 本轮由主代理建好 |
| `子agent/L1|L2|L3/` | **不存在，且上轮的 L1/L2 产出档也不在盘上**（只在 `.agent-contract/backups/2026-10-03T10-54-03-569Z/` 里）——需要新建三级目录 |

### 二、预演 1：`librarian_sweep --dryRun`

- **待办 120 条**，红条目全部是「一次性节点结束但没落档」（`ghost_run` ×11 + `missing_doc` ×9），成员 `T53_docs规范化整理-librarian` 自己占 4 条（红 1、4 号位重复计）——**本档落盘后应自动消掉 2 条**。
- **将覆盖 3 篇**（试算）：`核心数据库/派生态索引.md`（`+125/-108`，旧指纹 5ba77fdc31ef）、`坑/README.md`（`+17/-16`）、`核心数据库.md`（changelog 追加）。三处都是**派生物**，内容没变就不写。
- **将归位 5 篇** = 4 篇 `T99_*_L*` → `L1/L2/L3/` + 1 篇（sweep 只认 `deliverablesDir` 内、按档级可派生的 3 篇 + 1 篇可归位；与 relocate 的 119 项不是同一口径，**不要混用**）。
- **命名规范化候选 0**：sweep 的改名器只处理 `子agent/` 内、档级可派生的档；99 篇 legacy 档级不可派生 → 改名 0（**去 `^` 得走 `librarian_relocate`**）。
- **tags 过期 4 篇**（全是 T99 那 4 篇）：正文 2026-10-03T11:23:58 改过、keywords 停在 2026-10-02T16:34:29 → 需 `librarian_tags` 复核盖章。
- **doc_misfiled 黄 22 条** = 22 篇根目录散文件，与 relocate 的归位清单一一对应。
- **验收报告（预演）**：将修复引用 **78 篇** / 待人工裁定 **261** 篇 / 将标注 **100** 篇 / 将归位 5 篇 / 将覆盖 3 篇；「存置断引用」大头是**坑档相对文件名引用**（`方块模型.md`、`贴图.md`、`物流交接.md`、`BER渲染.md`、`API签名与编译.md`、`D:\Blockdustry\仓库\docs\坑\碰撞箱.md` 等），工具都能唯一匹配到 `坑/` 下的真身。
- 正文零改动：逐篇比对 106 篇，指纹全一致 ✓；引用完整性：全库无旧路径残留 ✓。

### 三、预演 2：`librarian_relocate --dryRun`（分类归位 + 命名对齐）

按上级指定口径构造，逐条清单见 L1；这里给分组与实测回执：

| 组 | 项数 | 冲突 | 影响引用 | 目标 |
|---|---|---|---|---|
| 根目录 `^研究-*`（16）+ `^单位工厂与单位` + `^电力系统` | 18 | 0 | 0 | `研究\`（去 `^`、去冗余 `研究-` 前缀） |
| `^修改-钻头侧面贴图应用.md` | 1 | 0 | 0 | `修改\钻头侧面贴图应用.md` |
| `研究-渲染与模型坑.md` | 1 | 0 | 0 | `坑\D:\Blockdustry\仓库\docs\坑\渲染与模型坑索引.md` |
| `核心数据库.md` | 1 | 0 | **1 处** | `核心数据库\核心数据库.md`（**政策冲突，见裁定点 B**） |
| `D:\Blockdustry\仓库\docs\archive\2026-08\传送带上下坡.md` | 1 | 0 | **1 处** | `archive\2026-08\`（**归档动作，本轮不落地，见预演 3**） |
| `子agent\^*.md`（99 篇 legacy）**原地去 `^`** | 99 | 0 | **0 处** | `子agent\*.md`；99/99 全部可判 = 去 `^`，**无一篇可派生 L1/L2/L3 档级** |
| `子agent\T99_*_L1|L2|L3.md` | 4 | 0 | **6 处** | `子agent\L1|L2|L3\` |

**档级可派生性裁定**：99 篇 legacy 的正文是项目**自有单档格式**（`目标/结论·产出/占用与交接/异常`，见 `子agent/README.md` L7-23），既不是 L1 也不是 L2/L3，文件名里的 `T1/T9a/T38_A/批1E` 等只是任务号不是档级 → **`tier: 0`，不猜 L1/L2/L3**。
**`^T52_待办路线图研究.md` 特例**：全 99 篇里**唯一已有 front-matter** 的一篇（含 `tier`/`taskId`），去 `^` 时只改文件名、**不动头部**，`tier` 保持原值。

### 四、预演 3：`librarian_archive` 机制问答 + 候选清单（**无 dryRun 参数**）

**Q1：`archived: true` 是否由本工具写入头部？** —— **是，且不止它**。实测（已回滚）工具会**整块注入/重写 YAML 头部**：

```yaml
---
taskId: null
role: null
tier: null
keywords: null
relatedFiles: null
createdAt: null
librarianTouchedAt: <运行时刻>
librarianChanges: ["归档到 <YYYY-MM> 分片"]
archived: true
archivedAt: <运行时刻>
---
```

三篇原档**本来没有 YAML 头部**（首行即 `# 标题`），归档后都被加了上面 12 行。**副作用有两条**：（a）顶部多出 6 个 `null` 派生字段；（b）若原档已有**部分**头部（如 `^T9_三维物流.md` 有最小头部），会被**整体替换**成含 `null` 的块——即「正文零改动」保住了，但**头部语义被工具重写**。

**Q2：分片 `<YYYY-MM>` 按什么时间字段判定？** —— 实测**按「运行时刻」**：三篇都落进 `archive/2026-10/`，而 `D:\Blockdustry\仓库\docs\archive\2026-08\传送带上下坡.md` 正文自标废弃日期是 **2026-08-13**。上轮 manifest（`T53-move-2026-10-03`）曾把它规划去 `archive/2026-08/`，与工具实测口径**不一致**。即：**分片 = 归档动作发生时刻，不是文档语义日期（createdAt / 标废日）**。若上级要按语义日期分片，需在落盘后手工 `librarian_relocate` 纠偏，或给工具加时间字段参数。

**候选（只认正文自标废弃，3 篇，逐条 src→dst）**：
1. `仓库\docs\D:\Blockdustry\仓库\docs\archive\2026-08\传送带上下坡.md` → `仓库\docs\archive\2026-10\D:\Blockdustry\仓库\docs\archive\2026-08\传送带上下坡.md`（正文 L3 「已废弃（2026-08-13）」）
2. `仓库\docs\子agent\^T16_上下坡带.md` → `仓库\docs\archive\2026-10\^T16_上下坡带.md`（正文 L3 「已废弃（T19 回滚）」）
3. `仓库\docs\子agent\^T17_上下坡带模型.md` → `仓库\docs\archive\2026-10\^T17_上下坡带模型.md`（同上）

`^` 前缀**已废弃**：归档语义写在文档头部 `archived: true`（不再靠文件名前缀）
**待裁定冲突**：`D:\Blockdustry\仓库\docs\archive\2026-08\传送带上下坡.md` 正文标废弃，但 `^T19_删除爬坡回滚.md` L44 记「未标废弃」——记载互相矛盾，归档前建议上级裁一次。

### 五、预演 4：34 个 `_lang_*.json` → `docs\_临时\`

- 34 项，冲突 0，**只搬不删**，目标父目录 `D:\Blockdustry\docs\_临时\` 已存在（本轮实测）。
- **工具口径「影响引用 0 处」是错的**：这 34 个文件名带 `^`，工具按字面量匹配路径，抓不到 `docs/子agent/^P1_批1D_laserDrill_lang_en.json` 之外的**简写与省略写**；人工 grep 实查 **10 文件 / 14 处**引用：
  - 任务卡 5 篇：`T30_1B_itemBridge.md`、`T33_激光钻头.md`、`T41_1E_B_大型节点电池.md`、`T48_2A_高级墙体.md`、`T49_维修场力场.md`
  - 整合清单 5 篇：`^P1_批1A_A3`、`^P1_批1B_itemBridge`、`^P1_批1D_pneumaticDrill`、`^P1_批1E_powerNodeLargeBatteryLarge`、`^P1_批2A_高级墙体`
- **语义风险**：这些 json 是「lang 片段」，任务卡里是**产出登记**、整合清单里是**并入 `lang/zh_cn.json` 的来源指引**。搬到 `docs\_临时\` 后，未来要并入 `lang/` 的人会找不到；建议上级在落地时二选一：① 接受「归档意味废弃、内容已并入 `lang/`（需确认）」② 改搬去 `仓库\docs\_临时\` 而不是 `D:\Blockdustry\docs\_临时\`，保持在仓库内。

## 风险与待确认

0. **⚠️ 本轮唯一一次意外落盘已回滚**：`librarian_archive` **无 `dryRun` 参数**，首次调用真搬了 3 篇并写了 changelog + 给 `^T9_三维物流.md` 注入了头部。我已用 `librarian_relocate` 把 3 篇搬回原位、手工剥掉 4 篇被注入的 YAML 头部、并删掉 4 行指向不存在路径的归档日志；changelog 里保留 3 行真实的「搬回」记录作为可查痕迹。**逐篇验指纹 = 与运行前一字不差**（L1 第一节有逐条对账）。**教训：`librarian_archive` 不可用于预演，需要预演就走 `librarian_relocate --dryRun`。**
1. **`audit.archiveAfterDays` 缺失** → 归档的时间边界无法判定，只能靠「正文自标废弃」。
2. **`核心数据库.md` 移动 vs 正文固定路径**：正文 L554 自述「主文档命名固定 `D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md`」，另有 8 文件 9 处外部引用（`派生态索引.md`、`任务/核心数据库维护.md` ×3、`任务/T38`、`任务/T52`、`子agent/^T52`，等）。搬进 `核心数据库\` 后 L554 必成断引用，而「正文零改动」不允许改它。
3. **34 个 json 的 14 处引用工具没报** → 要么落地时手工补引用，要么改落点。
4. **99 篇 legacy 档级不可派生** → 需上级给规则（留根 + `tier: 0` / `legacy/` 子目录 / 视为 L1）。
5. **`坑\` 18 篇主题档无任务号** → 命名规则无法套用（见 L1 裁定点 A）。
6. **`派生态索引.md` 是派生物** → 改名/改路径会被重建器覆盖回原路径，不要手工搬。
7. **上轮 L1/L2 产出档不在盘上** → 只有备份副本；本轮产出档落在新建的 `L1|L2|L3\`，与上轮同任务同档级**同名不同目录**，需上级决定旧档是否补回。
8. **`坑\README.md` 索引区块缺 2 篇**：`EXP-RELOCATE_..._坑.md` 未入索引；`D:\Blockdustry\仓库\docs\坑\渲染与模型坑索引.md` 待归位后入索引。

## 下一步

先把 L1 的裁定点 A~D 拍板（坑档命名 / 核心数据库移动 / 断引用复核 / 引用更新政策），再按「建三级目录 → 去 `^` 原地改名 → 分类归位 → 临时文件搬迁 → 补引用 → `librarian_sweep` 落地索引与台账」分批落盘；归档单独一批，且**先补 `audit.archiveAfterDays`**。

---

附：一级文档（详细归纳，含全部 src→dst 逐条表与回滚对账）`D:\Blockdustry\仓库\docs\产出\L1\T53_docs规范化整理_docs-规范化整理-dryRun预演-逐条搬迁表_L1.md`
