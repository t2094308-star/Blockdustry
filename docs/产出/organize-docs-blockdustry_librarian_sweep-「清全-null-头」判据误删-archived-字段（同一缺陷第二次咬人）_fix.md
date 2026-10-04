---
taskId: organize-docs-blockdustry
role: librarian
tier: 0
keywords: ["归档","archived","archive","字段","2026","上下","下坡","blockdustry"]
relatedFiles: ["D:\\Blockdustry\\仓库\\docs\\archive\\2026-08\\传送带上下坡.md","D:\\Blockdustry\\仓库\\docs\\archive\\2026-10\\T16_上下坡带.md","D:\\Blockdustry\\仓库\\docs\\archive\\2026-10\\T17_上下坡带模型.md","D:\\Blockdustry\\仓库\\docs\\archive\\2026-10\\organize-docs-blockdustry-refs_子agent→产出-引用修补分级清单（只读普查）_研究.md","D:\\Blockdustry\\仓库\\docs\\核心数据库\\核心数据库.md"]
createdAt: 2026-10-04T06:09:39.897Z
librarianTouchedAt: 2026-10-04T06:23:01.864Z
librarianChanges: ["引用修复（指向现存档）","补 keywords（归档、archived、archive、字段、2026、上下、下坡、blockdustry）"]
---

## 成因

`librarian_sweep` 的「全 null 头」判据**只看派生字段能否判出真值**，不看字段是否已有实义值。当一篇档的 front-matter 仅由 `archived: true` / `archivedAt` + 馆员留痕（`librarianTouchedAt` / `librarianChanges`）构成时，该头部被判为「全 null 头」⇒ 整段删除，**归档标记被连带清掉**。

**第一次咬人（已发生）**：第八轮治理落地时 `sweep` 报「全 null 头：已清 132 处」，把 `archive\2026-08\传送带上下坡.md`、`archive\2026-10\^T16_上下坡带.md`、`^T17_上下坡带模型.md` 三篇旧归档档头部整段删除 —— 而这三篇除 `archived` 外确实是 null 占位，故**从未被察觉**，直到用户点检归档标记时才暴露。

**第二次咬人（已被预演拦下）**：第十一轮补回三篇 `archived` 后，再跑 `librarian_sweep --dryRun`，预演仍报「将清全 null 头 6 处」（3 篇 × 每篇计 2 次）⇒ **落盘会把刚补的归档字段再删一次**，形成「补 → 清 → 再补」循环。判据实测：三篇头部现为 `archived: true` / `archivedAt: …` / `librarianTouchedAt: …` / `librarianChanges: […]`（见 `archive\2026-10\T16_上下坡带.md` L1–6），**含 2 个实义字段**（`archived`、`archivedAt`），不属「全 null」。

**风险**：归档语义自 FIX-69 起规定「**读字段、不看文件名前缀**」（`^` 前缀已废弃）。若 `archived` 字段被清，归档档将失去唯一的机器可读归档标记 ⇒ 台账/审计/检索无法识别其归档状态，且与「归档 = 头部 `archived: true`」的规范直接冲突。本缺陷是**自我抵消型**：用户按规范补字段的动作，会被下一轮治理按同一判据撤销。

## 修复

**本轮采取规避（用户拍板「暂不落盘，等插件修」）**：
1. 保留 `archive\` 下 4 篇的 `archived: true` 字段（已实测全部在位）。
2. **挂起 `librarian_sweep` 落盘**：虽然它另有收益（`D:\Blockdustry\仓库\docs\核心数据库\派生态索引.md` 重算 `+120/-119`、`研究\索引.md` 23→22、`archive\索引.md` 3→4 校正、119 篇引用修复、7 篇台账标题回写），但因会抵消归档标记，**净收益为负**，故不落盘。
3. 归档档去 `^` 改名已单独完成（`librarian_relocate`，原地不换分片，正文指纹一致），不受本缺陷影响。

**建议的插件侧修法（供插件维护者）**：
- 「全 null 头」判据应改为「**头内不存在任何实义字段**」—— 即把 `archived` / `archivedAt` / `librarianTouchedAt` / `librarianChanges` **之外的**字段全部为 `null`/空时，才允许整段删除；
- 更稳妥：**含 `archived: true` 的头一律豁免**（归档标记优先于占位清理）；
- 断言建议：`archive/<月>/` 下任何档在 `sweep` 落盘后仍应保留 `archived: true`（回归测试）。

**验证方式**：`Select-String -Path '仓库\docs\archive\**\*.md' -Pattern '^archived: true'`，四篇归档档应全部命中；`librarian_sweep --dryRun` 的「将清全 null 头」应为 **0 处**（当前报 6 处）。

## 涉及文件

- D:\Blockdustry\仓库\docs\archive\2026-08\传送带上下坡.md
- D:\Blockdustry\仓库\docs\archive\2026-10\T16_上下坡带.md
- D:\Blockdustry\仓库\docs\archive\2026-10\T17_上下坡带模型.md
- D:\Blockdustry\仓库\docs\archive\2026-10\organize-docs-blockdustry-refs_子agent→产出-引用修补分级清单（只读普查）_研究.md
- D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md
