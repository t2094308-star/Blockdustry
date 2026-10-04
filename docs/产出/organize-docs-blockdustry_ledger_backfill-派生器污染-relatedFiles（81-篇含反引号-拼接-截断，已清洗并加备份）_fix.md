---
taskId: organize-docs-blockdustry
role: null
tier: 0
keywords: []
relatedFiles: ["D:\\Blockdustry\\仓库\\docs\\产出\\T16_提升机底部交接.md","D:\\Blockdustry\\仓库\\docs\\产出\\L2\\organize-docs-blockdustry_docs整理dryRun预演_L2.md","D:\\Blockdustry\\仓库\\docs\\产出\\L1\\organize-docs-blockdustry_治理预演报告（sweep-dryRun-+-类别索引清单-·-零落盘）_L1.md","D:\\Blockdustry\\仓库\\docs\\产出\\L1\\T53_docs规范化整理_docs-规范化整理-dryRun预演-逐条搬迁表_L1.md","D:\\Blockdustry\\仓库\\docs\\研究\\organize-docs-blockdustry-refs_子agent→产出-引用修补分级清单（只读普查）·高度概括_研究.md","D:\\Blockdustry\\仓库\\docs\\_临时\\relatedFiles清洗预演报告.txt","D:\\Blockdustry\\.agent-contract\\backups\\relclean-20261004-143526"]
createdAt: 2026-10-04T06:37:10.071Z
---

## 成因

`ledger_backfill --auto` 的确定性派生器在生成 `relatedFiles` 时**把正文里被反引号包裹的 Markdown 路径原样抓取、未做清洗**（派生器见 `src/ledger/derive.js:180` 的 `extractPaths(source)`）。现场后果：`产出\` 等域 **81 篇**的 `relatedFiles` 行含反引号，最多一篇 56 个；且拼接出三类畸形条目 —— ① 多个路径被并进同一数组元素（如 `D:\...\T15_提升机异常.md`、`docs/坑/D:\...\方块模型.md`）② 路径被截断（`D:\Blockdustry\[Agent进度`、`…（只读普查`）③ 残缺前缀（`坑\D:\Blockdustry\...`）。

插件**自己就定义了这个脏形态**（`src/ledger/progressive.js:80`：`带引号/反引号（派生时没有清洗）`，对应审计项 `dirty_related_path`），但该项**对本批未报** —— 因为 `产出\` 的档都是 `tier: 0` legacy，该检查的有效面未覆盖它们。⇒ **缺陷对审计不可见，数据却是脏的**；而用户对这批历史档的定位正是「查缺补漏与追溯」，脏路径直接摧毁其用途。

## 修复

**第一步（librarian，已完成 27 篇）**：逐篇 `edit` 去首尾引号、丢弃整条残片，但因馆员无 shell/批量替换能力、剩 54 篇工作量约 300 处 edit，无法在合理轮次内完成。

**第二步（主代理用 shell 一次跑完，已完成全部）**：
1. **备份**：`D:\Blockdustry\.agent-contract\backups\relclean-20261004-143526\`（60 篇搬前副本，逐篇可回滚）。
2. **解析**：按 front-matter 定位 `relatedFiles:` 行，逐个**数组元素**解析（元素分隔是 `","`；元素**内部**的路径分隔是顿号 `、`）—— 否则含逗号的合法文件名会被切坏。
3. **清洗（保守口径，经一次失败迭代后定稿）**：
   - 去首尾引号/反引号；斜杠归一化后还原为单反斜杠；
   - 裸盘符补斜杠（`D:Blockdustry` → `D:\Blockdustry`）；
   - 按顿号拆出多条真路径；
   - **丢弃**：括号/方括号不配对的截断残片、空串、单字母残片。
4. **修正的多重路径条目**（4 篇）：形如 `…\核心数据库\D:\Blockdustry\…` 的嵌套拼接，只保留**最后一段真实盘符路径**。
5. 重新序列化为合法 JSON 数组写回（YAML 流式数组语法，双反斜杠转义与原文一致），**只改该一行**。

**试错留痕（重要）**：第一版用了「激进抢救」正则在元素内抓所有路径片段，结果把 `docs/坑/D:\Blockdustry\...` 切成残片 `docs/坑/D`，并让 3 篇 JSON 非法。**改用保守口径（按顿号整体拆分 + 丢残片）后零残片** —— 说明在**源数据已损坏**时，「整体保留可解析条目 + 丢弃不可复原片段」优于「在碎片里抢救」。

**验证**：全库 `relatedFiles` **含反引号 = 0**、**JSON 解析失败 = 0**（125 篇有该字段）；清洗前后条目数基本持平（1352 条保留 / 8 条丢弃）。

## 涉及文件

- D:\Blockdustry\仓库\docs\产出\T16_提升机底部交接.md
- D:\Blockdustry\仓库\docs\产出\L2\organize-docs-blockdustry_docs整理dryRun预演_L2.md
- D:\Blockdustry\仓库\docs\产出\L1\organize-docs-blockdustry_治理预演报告（sweep-dryRun-+-类别索引清单-·-零落盘）_L1.md
- D:\Blockdustry\仓库\docs\产出\L1\T53_docs规范化整理_docs-规范化整理-dryRun预演-逐条搬迁表_L1.md
- D:\Blockdustry\仓库\docs\研究\organize-docs-blockdustry-refs_子agent→产出-引用修补分级清单（只读普查）·高度概括_研究.md
- D:\Blockdustry\仓库\docs\_临时\relatedFiles清洗预演报告.txt
- D:\Blockdustry\.agent-contract\backups\relclean-20261004-143526
