---
taskId: organize-docs-blockdustry
role: null
tier: 0
keywords: ["blockdustry","10","规范","正文","role","2026","23","务卡"]
relatedFiles: ["D:\\Blockdustry\\任务\\organize-docs-blockdustry.md","D:\\Blockdustry\\任务\\organize-docs-blockdustry-交接卡.md","D:\\Blockdustry\\任务\\organize-docs-blockdustry-落地.md","D:\\Blockdustry\\任务\\T9_三维物流.md","D:\\Blockdustry\\任务\\T27_物流_A2.md","D:\\Blockdustry\\任务\\T27_物流_A3.md","D:\\Blockdustry\\任务\\T31_雷光特效实现.md","D:\\Blockdustry\\任务\\T35_硅冶炼厂.md","D:\\Blockdustry\\任务\\T36_窖炉.md","D:\\Blockdustry\\任务\\T39_审查批1CD.md","D:\\Blockdustry\\任务\\T40_1E_A_太阳能电池板.md","D:\\Blockdustry\\任务\\T41_1E_B_大型节点电池.md","D:\\Blockdustry\\任务\\T42_1E_C_二极管涌电塔.md","D:\\Blockdustry\\任务\\T43_1F_A_铜墙废料墙.md","D:\\Blockdustry\\任务\\T44_1F_B_钛墙门.md","D:\\Blockdustry\\任务\\T45_硫化物混合器.md","D:\\Blockdustry\\任务\\T46_粉碎机焚烧炉.md","D:\\Blockdustry\\任务\\T47_相织布织机.md","D:\\Blockdustry\\任务\\T49_维修场力场.md","D:\\Blockdustry\\任务\\T50_修复批1CD审查中危.md","D:\\Blockdustry\\任务\\T51_分裂炮台像素绘制研究.md","D:\\Blockdustry\\任务\\T53_docs规范化整理.md","D:\\Blockdustry\\任务\\T99_契约流程冒烟测试.md","D:\\Blockdustry\\.agent-contract\\backups\\T23-revert-fm-2026-10-03"]
createdAt: 2026-10-03T17:09:18.251Z
librarianTouchedAt: 2026-10-04T06:23:01.864Z
librarianChanges: ["补充「null 占位与回填循环」定论（新增 结论/依据/风险与待确认/下一步 四节，原有三节一字未改）","补 keywords（blockdustry、10、规范、正文、role、2026、23、务卡）"]
---

## 成因

`librarian_sweep` 一轮治理（2026-10-03T16:38:07.512Z）在「引用修复（指向现存档）」过程中，对 23 张**任务卡**（`任务\*.md`）写入了 YAML front-matter 头部。判据：全部 58 张任务卡中带头的 23 张，其 `taskId` 全为 `null`，无一张是「本来就有头」；备份时间戳 `16-38-07-512Z` 与该次 sweep 同秒。头块形状高度一致：第 1 行 `---`、第 2–7 行 `taskId/role/tier/keywords/relatedFiles/createdAt` 全 `null`、第 8 行 `librarianTouchedAt`、第 9 行 `librarianChanges`、第 10 行 `---`、第 11 行空行。任务卡在本项目是**另一套格式**（`# 标题` + `上级/占用文件或资源/状态/阶段产出/异常`），规范只要求「产出档 / 研究档 / 审查 / 整合清单 / 坑 / 修改 / 核心数据库」等**文档**带 front-matter，故这 23 处头属误注入。风险：全 null 的 `taskId/role/tier` 会让审计与台账把任务卡误判为文档档，从而按文档规范反复报莫名其妙告警。

## 修复

按用户拍板「回退」执行：① 先出逐文件对照（每张卡的头形状 + 唯一实义字段 + 头后首个原行）交用户确认；② 逐张 `Copy-Item` 备份到 `.agent-contract\backups\T23-revert-fm-2026-10-03\`（23 份）；③ 逐张删除第 1–10 行头块 + 紧随的 1 个空行（共 -11 行），用 `UTF8Encoding($false)` 写回（无 BOM）；④ 复核：删后每张 `-11` 行、首行恢复为原标题行；全量扫描「仍带 front-matter 的任务卡 = 0」；与 23 份备份逐行比对「正文不一致 = 0」，证明只砍头、正文分毫未动。

唯一实义信息处置：22 张的 `librarianChanges` 只是「引用修复（指向现存档）」时间戳，随头删除；`T53_docs规范化整理.md` 那张写的是「订正 `^` 的表述（归档语义已改到元数据）」，属**语义订正**性质，删除时该订正对应的正文改动**保留在正文内**未被回退。

注：用户随后确认「插件修好了，不会乱 sweep」，故本次回退属**一次性清理**，不是对插件行为的规避。

## 涉及文件

- D:\Blockdustry\任务\organize-docs-blockdustry.md
- D:\Blockdustry\任务\organize-docs-blockdustry-交接卡.md
- D:\Blockdustry\任务\organize-docs-blockdustry-落地.md
- D:\Blockdustry\任务\T9_三维物流.md
- D:\Blockdustry\任务\T27_物流_A2.md
- D:\Blockdustry\任务\T27_物流_A3.md
- D:\Blockdustry\任务\T31_雷光特效实现.md
- D:\Blockdustry\任务\T35_硅冶炼厂.md
- D:\Blockdustry\任务\T36_窖炉.md
- D:\Blockdustry\任务\T39_审查批1CD.md
- D:\Blockdustry\任务\T40_1E_A_太阳能电池板.md
- D:\Blockdustry\任务\T41_1E_B_大型节点电池.md
- D:\Blockdustry\任务\T42_1E_C_二极管涌电塔.md
- D:\Blockdustry\任务\T43_1F_A_铜墙废料墙.md
- D:\Blockdustry\任务\T44_1F_B_钛墙门.md
- D:\Blockdustry\任务\T45_硫化物混合器.md
- D:\Blockdustry\任务\T46_粉碎机焚烧炉.md
- D:\Blockdustry\任务\T47_相织布织机.md
- D:\Blockdustry\任务\T49_维修场力场.md
- D:\Blockdustry\任务\T50_修复批1CD审查中危.md
- D:\Blockdustry\任务\T51_分裂炮台像素绘制研究.md
- D:\Blockdustry\任务\T53_docs规范化整理.md
- D:\Blockdustry\任务\T99_契约流程冒烟测试.md
- D:\Blockdustry\.agent-contract\backups\T23-revert-fm-2026-10-03

---

## 结论（补充 · 2026-10-04 · `null` 占位与「回填循环」定论）

**用户已拍板**：产出档头部一律按**两条路**走 —— **有头的档填真值、无头的档整段删**；**不再采用「只删 `null` 占位行」**。

## 依据（补充）

1. 2026-10-04 治理落地轮按「B 组只删 `null` 行」执行，删掉了 `产出\L1\organize-docs-blockdustry_收尾+返工轮-dryRun-预演报告（…）_L1.md` 与 `…治理预演报告（…）_L1.md` 的 `role: null`。
2. 随后 `librarian_tags` 盖章时，**派生器把 `role: null` 原样回填**（两篇复核实证：L3 重新出现 `role: null`），并因此再次触发审计 `doc_tags_stale` ⇒ 形成「**删 → 盖章回填 → 审计再报**」的循环。
3. **破环口径**：缺失字段补**实义值**而非删行 —— 上两篇已改 `role: librarian`（作者真实角色），再盖章定版后不再被回填。
4. **复验**：`产出\` 域 front-matter 内 `^[a-zA-Z]+: null$` = **0**；唯一命中是 `产出\L2\T53_docs规范化整理_docs-规范化整理-dryRun预演-扩充细节_L2.md` 正文 yaml 代码块内的样例文本（**属正文，不动**）。

## 风险与待确认（补充）

- 「无头档」若日后又被派生器补出全 `null` 头，**仍应按同一口径整段删除**（含前后 `---` 与紧随空行），**不要只删字段**，否则会复现本循环。
- 本档原有「成因 / 修复 / 涉及文件」三节为事件当时的记录，**一字未改**；本节仅为事后的口径定论。

## 下一步（补充）

- 本议题已收口，无需再动作；新档一律「有头填真值、无头整段删」。
