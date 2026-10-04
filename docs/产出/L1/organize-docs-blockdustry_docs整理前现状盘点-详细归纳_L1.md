---
taskId: organize-docs-blockdustry
role: researcher
tier: 1
keywords: ["docs整理前现状盘点-详细归纳","docs盘点","整理前现状","命名规范","违规命名","^前缀","archive","引用面","类别目录缺失","全量清单","Blockdustry","存在","目录","153","不存","大小","18","184","34"]
relatedFiles: ["D:\\Blockdustry\\仓库\\docs\\产出\\L1\\organize-docs-blockdustry_docs整理前现状_全量逐条清单.md","D:\\Blockdustry\\仓库\\docs","D:\\Blockdustry\\仓库\\docs\\产出","D:\\Blockdustry\\仓库\\docs\\坑","D:\\Blockdustry\\仓库\\docs\\核心数据库","D:\\Blockdustry\\仓库\\docs\\archive","D:\\Blockdustry\\仓库\\docs\\_临时"]
createdAt: 2026-10-03T13:52:57.836Z
librarianTouchedAt: 2026-10-04T06:01:22.579Z
librarianChanges: ["馆员搬移","引用更新（搬移）","复核 keywords（新增 0 个）","复核 keywords（新增 0 个）","复核 keywords（新增 0 个）"]
detailLevel: full
---

> **速览** 本项目：Blockdustry
> **速览** 本档（L1）给：长期留档与全量核查
> **速览** 本档即最详细的一档（终点，没有下钻指针）
# 全量明细档

`D:\Blockdustry\仓库\docs\产出\L1\organize-docs-blockdustry_docs整理前现状_全量逐条清单.md`（本产出目录根层；含第 1~8 节全部逐条路径、判定口径与工具边界）

## 结论

`仓库\docs\` 实测 **184 文件 = 150 md + 34 json**，顶层目录 4 个；**4 个类别目录不存在**、`archive\` 存在但空；非 md **只有 34 个 json**；`^` 前缀文件 **153 个**；**91 个 md 互引**（590 处）。逐条见明细档。

## 依据

`glob 仓库/docs/**/*` → 184 条路径；`read <目录>` 判据（`not a regular file` = 存在 / `not found` = 不存在）确认 `archive\` 存在且空、`研究|审查|整合清单|修改\` 不存在；`grep *.md` 命中 91 文件 / 590 行。

## 风险与待确认

1. **文件字节大小全部未验证**（无 bash，工具不返回 size）→ 「路径+大小+扩展名」中大小列空。
2. 空目录判据属间接证据（错误文案语义），与 `glob` 无命中互证。
3. `坑\` 条目数存在 16/17/18 多口径，本轮只报实测 **18**。
4. `^` 条目初算 126 → 复核 **153**，以 153 为准。
5. 本档不含任何整理建议与裁定。

## 下一步

待上级按清单取舍裁定；本任务不再产出判断类内容。
