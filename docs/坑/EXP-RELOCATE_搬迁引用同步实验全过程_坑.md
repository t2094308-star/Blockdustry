---
taskId: EXP-RELOCATE
role: researcher
tier: 0
keywords: ["搬迁引用同步实验全过程","librarian_relocate","引用同步","relatedFiles","指针自动改写","馆员搬移","搬迁","relocate","reference sync","路径字面量匹配","台账","载体","体档","引用","实验","agent","l2","搬入","指针"]
relatedFiles: ["D:\\DSH插件\\dsh-agent-contract\\src\\librarian\\duties.js","D:\\DSH插件\\dsh-agent-contract\\src\\ledger\\rebuild.js","D:\\DSH插件\\dsh-agent-contract\\src\\ledger\\derive.js","D:\\DSH插件\\dsh-agent-contract\\scripts\\verify.mjs","D:\\Blockdustry\\仓库\\docs\\核心数据库\\核心数据库.md","D:\\...","D:\\Blockdustry\\仓库\\docs\\子agent\\L2\\EXP-RELOCATE-指针测试_搬迁指针测试乙_L2.md","D:\\DeepSeek","D:\\DSH插件\\dsh-agent-contract","C:\\Users\\flafk\\.dsh\\profiles\\desktop\\package.json","D:\\Blockdustry\\docs\\_临时\\EXP-RELOCATE-实验","指针测试甲\\乙\\丙","agent\\L2\\...L2.md","...甲_L2.md","project.json"]
createdAt: 2026-10-03T12:42:49.538Z
---

> **速览** 本项目：Blockdustry
> **速览** 本档（L0）给：读者
> **速览** 细节见本档 front-matter 的指针（fullDetail 指向 L1，另一档生成后由工具自动补齐）
## 结论

本次对实验性改动「`librarian_relocate` 自动同步 `relatedFiles` 与正文指针」做了四轮真机实验。客观事实如下：

- 该能力**会**改写引用，实例：正对照载体档的 `relatedFiles` 与正文代码块两处指针，均被从 `EXP-RELOCATE-搬入\` 改到 `L2\`，并新增 `librarianTouchedAt` / `librarianChanges: ["引用更新（搬移）"]`。
- 是否命中取决于两个条件：引用方档须在**台账**（`store.listDocs()`，即产出目录 / `任务` / `[Agent进度]`）内；文本中须出现与搬迁入参 `from` **逐字相同**的路径字面量（本次为 Windows 绝对反斜杠路径）。
- 实验共观察到 11 处问题，逐条记于下节。全部为客观记录，不含修复结论。
- 实验装置已收尾：6 个实验档移至 `D:\Blockdustry\docs\_临时\EXP-RELOCATE-实验\`，空的 `EXP-RELOCATE-搬入\` 目录已删，`仓库\docs` 下 `EXP-RELOCATE*` 残留计数为 0。

## 依据

### 一、实验过程（四轮）

| 轮次 | 入参/装置 | 预演三数 | 落盘回执 | 指针实际变化 |
|---|---|---|---|---|
| 1 | 3 个靶子档从 `子agent\L2\` → `子agent\EXP-RELOCATE-搬入\`；观测载体档用 `仓库/docs/...` 斜杠相对指针 | 3/0/0 | 移动 3 项，更新引用 0 处 | 无（观测载体档 9 处指针逐字未动） |
| 2 | 试图把甲从 `L2\` → `搬入\` | 0/1/0 | 移动 0 项，更新引用 0 处 | 不适用（拒收） |
| 3 | 乙从 `L2\` → `搬入\`；精确匹配载体档用绝对反斜杠指针 | — | 移动 1 项，更新引用 1 处 | 头/正文各 1 处被改写 |
| 4 | 3 个靶子档从 `搬入\` → `L2\` | 3/0/2 | 移动 3 项，更新引用 2 篇 | 正对照载体档、精确匹配载体档各 2 处被改写 |

第 4 轮 `refs` 明细原文：`正对照载体档（2 处：relatedFiles 1 + 正文 1）`、`精确匹配载体档（2 处：relatedFiles 1 + 正文 1）`；观测载体档未被判为引用方。

### 二、观察到的 11 个问题

1. **头部路径写法与命中判据不匹配（最主要）**：`doc_emit` 生成的观测载体档，其 `relatedFiles` 由 `extractPaths()` 从正文派生，形态为 `仓库/docs/子agent/L2/...L2.md` 与裸文件名 `...甲_L2.md`；而 `countReferences` / `rewriteReferences` 用 `file.text.includes(from)` 逐字匹配 `from` 原文（绝对反斜杠），因此这一整类写法**不会命中**。

2. **同一字段内两种写法并存**：观测载体档第 6 行同时存在「裸文件名」与「`仓库/docs/...` 斜杠相对路径」两种条目；正对照载体档第 6 行为「`D:\\...` 双反斜杠 YAML 转义」形态，正文第 20/21 行为「`D:\...` 单反斜杠」形态。同一档内写法不一致。

3. **台账收录范围与"引用方"范围不一致**：`rebuild.js` 的 `scanDocs()` 只扫 `deliverablesDir`（`仓库\docs\子agent`），研究 / 坑 / 修改 / 核心数据库四类不在台账内；而 `countReferences` / `rewriteReferences` 遍历范围是 `store.listDocs()`，未使用 `scanAllDocs()` 的磁盘那半边。即：这四类档若写了指针，搬迁时不会被改写（本轮实验装置未覆盖此四类，属源码判读）。

4. **子智能体回报被截断**：第 2 轮委派返回的正文只有 `报告喵 —— **全程只跑了一次 dryRun + 一次落盘 reloc`，实验数据丢失，须由主代理重新读盘核对。

5. **凭证/装置导致的无效轮次**：第 2 轮正对照使用了已搬走的甲作为 `from`，被护栏「目标已存在，拒绝覆盖」拒收（预演 `0/1/0`），该轮数据对判据无判别力，须换乙重做。

6. **`relatedFiles` 会被自动改写而正文不会被自动改写**：第 4 轮中，甲档 `relatedFiles` 里指向乙丙的 `仓库/docs/子agent/L2/...` 条目（字面量不匹配）未变；正对照载体档第 26 行「## 风险与待确认」中的旧结论文字亦未变（该句未含完整绝对路径）。

7. **引用改写是纯字面替换**：第 4 轮正对照载体档第 27 行「若预演仍报『影响引用 0 处』…」这一历史结论句本身未变，因其中不含完整路径；而被替换处仅为含完整路径的行。工具不做语义判断。

8. **回执口径为"篇"、预演口径为"处"**：第 4 轮预演报 `影响引用 2`（按篇计），落盘报 `更新引用 2 篇（共 4 处字面命中）`。两处数字口径不同，需读 `refs` 明细才能对应。

9. **三档产出缺失**：`doc_emit` 每次生成 L2 档后均提示「还缺 L1 档」，本次 4 个载体档**皆只有 L2、无 L1/L3**；`relatedFiles` 中亦未出现 `fullDetail` / `nextTier` 指针（无 L1 可指）。

10. **工作区未初始化提示反复出现**：`contract_status` 与子智能体回报多次提示缺 `仓库/docs/子agent/L1/`、`L3/`、`仓库/docs/研究/` 等 5~6 处目录；`_临时` 目录亦不存在（`project.json` 未记录 `tempDir` 字段）。

11. **馆员未产出登记卡/产出档**：三轮委派均因「禁止顺手干别的」的窄口径，馆员明确回报未写 `[Agent进度]` 登记卡、未产出三档文档、未跑派生器/sweep/归档、未改文件名、未补固定小节。

### 三、步骤级流水（含每次落盘）

1. 主代理 `doc_emit` 生成 3 个靶子档（`taskId: EXP-RELOCATE-指针测试`，`L2`）+ 1 个观测载体档（`taskId: EXP-RELOCATE-载体`），并手工补全 4 档的 `relatedFiles` 为双写法。
2. 委派馆员执行 3 项 move（`L2\` → `EXP-RELOCATE-搬入\`）。回执：预演 `3/0/0`，落盘「移动 3 项，更新引用 0 处」，正文指纹逐篇一致，备份目录 `.agent-contract\backups\2026-10-03T12-37-32-157Z\`。
3. 主代理读盘核对观测载体档：第 6 行 6 条指针 + 第 20~22 行 3 条正文路径，共 9 处逐字未动，且该档无 `librarianTouchedAt`。
4. 主代理 `doc_emit` 生成正对照载体档（`taskId: EXP-RELOCATE-正对照`，绝对反斜杠指针）。
5. 委派搬甲 → 被拒收（目标已存在），`0/1/0`。
6. 委派搬乙 → 回执缺失（回报被截断）。
7. 主代理读盘：甲、乙、丙均在 `搬入\`；正对照载体档指针未变。
8. 委派把乙搬回 `L2\` → 预演 `1/0/0`，落盘「移动 1 项，更新引用 0 处」；正对照载体档仍无留痕。
9. 委派再把乙搬去 `搬入\` → 回执「移动 1 项，更新引用 1 处」；核心数据库 changelog 新增 `2026-10-03T12:40:58.619Z [引用更新] …正对照载体… → 指向 …搬入\…乙_L2.md`。
10. 主代理读盘正对照载体档：第 6 行第 2 条与第 23 行均已改为 `搬入\` 路径；新增 `librarianTouchedAt: 2026-10-03T12:40:58.619Z`、`librarianChanges: ["引用更新（搬移）"]`。
11. 主代理 `doc_emit` 生成精确匹配载体档（`taskId: EXP-RELOCATE-精确匹配`）。
12. 委派把 3 个靶子档从 `搬入\` 搬回 `L2\` → 预演 `3/0/2`，落盘「移动 3 项，更新引用 2 篇」，`搬入\` 目录清空。
13. 主代理读盘精确匹配载体档：第 6 行 → `D:\\Blockdustry\\仓库\\docs\\子agent\\L2\\EXP-RELOCATE-指针测试_搬迁指针测试乙_L2.md`；第 22 行 → 同名单反斜杠绝对路径；新增 `librarianTouchedAt: 2026-10-03T12:41:40.915Z`。
14. 主代理用 pwsh 把 6 个实验档移入 `D:\Blockdustry\docs\_临时\EXP-RELOCATE-实验\`，删除空目录 `EXP-RELOCATE-搬入\`，残留检查为 0。

### 四、源码判据出处

- `D:\DSH插件\dsh-agent-contract\src\librarian\duties.js`：`countReferences`（约 821~834 行）、`rewriteReferences`（约 576~608 行）、`replacePathInMeta`（约 554~573 行）、`relocateBatch`（约 752~818 行）、`relocateDocs`（约 492~545 行）。
- `D:\DSH插件\dsh-agent-contract\src\ledger\rebuild.js`：`scanDocs`（约 33~63 行）。
- `D:\DSH插件\dsh-agent-contract\src\ledger\derive.js`：`extractPaths`（98~113 行）、`relatedFiles` 合并（180~182 行）。
- 参照测试：`D:\DSH插件\dsh-agent-contract\scripts\verify.mjs` 第 6185~6228 行（引用方档用 `relatedFiles: [rlSrc]` 绝对路径，正文写 `相关：${rlSrc}`）。
- 题目中给出的 DSH 检出路径 `D:\DeepSeek Harness\resources\app.asar\dsh` 在本机不存在；实际插件源码在 `D:\DSH插件\dsh-agent-contract`（`C:\Users\flafk\.dsh\profiles\desktop\package.json` 中写作 `"dsh-agent-contract": "link:D:/DSH插件/dsh-agent-contract"`）。

### 五、实验装置残留（客观状态）

- 6 个实验档现存于 `D:\Blockdustry\docs\_临时\EXP-RELOCATE-实验\`：观测载体、正对照载体、精确匹配载体、指针测试甲/乙/丙。
- `仓库\docs\子agent\EXP-RELOCATE-搬入\` 已删除。
- 台账 `docs` 表内这 6 条的记录仍存在（文件已不在产出目录），属陈旧记录。
- 核心数据库 `D:\Blockdustry\仓库\docs\核心数据库.md` 第 580~590 行新增 11 条记录：7 条 `[馆员搬移]`、4 条 `[引用更新]`（原文保留，未回滚）。
- `.agent-contract\backups\` 下新增本轮备份目录若干（含 `2026-10-03T12-37-32-157Z` 等）。
- 本次实验**未**修改任何项目源码。

## 风险与待确认

- 第 2 轮数据无效（装置错误），第 3 轮数据因回报截断而由主代理读盘补全，两轮均无完整工具回执原文。
- 问题 3（研究/坑/修改/核心数据库四类档不被引用同步覆盖）由源码判读得出，**未做真机验证**。
- 问题 10 所述缺目录、缺 `tempDir` 字段的状态，未做 `project_init` 变更，维持原状。
- 上面第 6 个实验档在 `_临时` 内仍保留原始 front-matter，若日后被重新纳入产出目录，会被审计视为重复档（`markDuplicates`）。

## 下一步

- 本文仅作客观记录；如需按三档规范产出（L3/L2/L1），或把上述判据写成可复现的最小实验脚本，需另行指定。
- 台账陈旧记录可由 `ledger_rebuild` 清除；本次未执行。
- `核心数据库.md` 第 580~590 行的 11 条实验记录未回滚；本次未执行回滚。
