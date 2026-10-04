---
taskId: organize-docs-blockdustry
role: librarian
tier: 3
keywords: ["docs整理dryRun预演","dryRun 预演","分类归位","命名与目录对齐","归档属性","临时文件挪走","去 ^ 前缀","158 项","冲突 0","归档 3 篇","新建 4 个类别目录","任务号判不出","档级不可派生","legacy 无头旧档","D:\\Blockdustry\\仓库\\docs\\核心数据库\\核心数据库.md 不移动","archiveAfterDays 缺失","预演","整理","搬迁","归档","dryrun","目录","34","根层","归位","核心","据库","数据","心数"]
relatedFiles: ["D:\\Blockdustry\\仓库\\docs","D:\\Blockdustry\\仓库\\docs\\产出\\L1\\organize-docs-blockdustry_docs整理dryRun预演_L1.md","D:\\Blockdustry\\仓库\\docs\\产出\\L2\\organize-docs-blockdustry_docs整理dryRun预演_L2.md","D:\\Blockdustry\\仓库\\docs\\archive","D:\\Blockdustry\\仓库\\docs\\_临时","D:\\Blockdustry\\任务\\organize-docs-blockdustry.md","D:\\Blockdustry\\仓库\\docs\\核心数据库\\核心数据库.md"]
createdAt: 2026-10-03T13:57:08.611Z
librarianTouchedAt: 2026-10-04T06:01:22.579Z
librarianChanges: ["手工直修（doc_emit 落盘后）：role: null → librarian","正文结论零改动","馆员搬移","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","复核 keywords（新增 0 个）","引用修复（指向现存档）","复核 keywords（新增 0 个）","复核 keywords（新增 0 个）"]
nextTier: D:\Blockdustry\仓库\docs\产出\L2\organize-docs-blockdustry_docs整理dryRun预演_L2.md
fullDetail: D:\Blockdustry\仓库\docs\产出\L1\organize-docs-blockdustry_治理预演报告（sweep-dryRun-+-类别索引清单-·-零落盘）_L1.md
---

> **速览** 本项目：Blockdustry
> **速览** 本档（L3）给：上级 / 主代理（只想知道结论）
> **速览** 细节见 D:\Blockdustry\仓库\docs\产出\L1\organize-docs-blockdustry_docs整理dryRun预演_L1.md
# organize-docs-blockdustry —— docs 整理 dryRun 预演（高度概括，发上级）

## 结论

预演全部出齐，**本轮零落盘**（事后 glob 校核 `archive\` 与四个待建目录均无文件）。合计 **158 项待搬**（124 md + 34 json）、**冲突 0**、**可归档 3 篇**、**需新建 4 个类别目录**：根层→`研究\` 18、根层→`修改\`/`坑\`/`核心数据库\` 3、`子agent\` 去 `^` 99、`T99_*` 归位 `L1|L2|L3\` 4、34 个 `_lang_*.json`→`docs\_临时\`。

最大阻塞：118 条命名违规里**任务号只有 100 条判得出、档级一条都判不出**，规范要求的「补类别后缀 / 补 `_L<n>`」本轮**一条也执行不了**。`D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md` 移动破正文 L554 + 8 文件 9 处引用，建议暂缓；`^T16/^T17` 归档与去 `^` 相斥，须先定顺序。

## 依据

`librarian_sweep --dryRun`×1、`librarian_relocate --dryRun`×5（158 项）、`librarian_archive --dryRun`×1。实测 155 md + 34 json，仅 15 篇有 front-matter、140 篇 legacy 无头；工具报影响引用 66 处，人工实查真值更高。

## 风险与待确认

待拍板 6 条：`D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md` 移动政策、归档与去 `^` 顺序、归档分片口径（`archiveAfterDays` 缺失）、json 落点、legacy 任务号来源、`审查/整合清单` 空目录是否建。

## 下一步

先交用户确认，再按「建目录 → T99 归位 → 99 篇去 `^` → 根层归位 → json 搬迁 → 补引用 → sweep 落地」分批执行；归档单列。

---

- 一级文档（详细归纳，逐条预演 + 规范缺口逐组判定）：`D:\Blockdustry\仓库\docs\产出\L1\organize-docs-blockdustry_docs整理dryRun预演_L1.md`
- 二级文档（扩充细节，发送主代理）：`D:\Blockdustry\仓库\docs\产出\L2\organize-docs-blockdustry_docs整理dryRun预演_L2.md`
