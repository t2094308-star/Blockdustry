---
taskId: T53_docs规范化整理
role: librarian
tier: 3
keywords: ["docs规范化整理_docs-规范化整理-dryRun预演-高度概括","docs-规范化整理-dryRun预演-高度概括","T53","docs 规范化","dryRun 预演","分类归位","去 ^ 前缀","legacy tier 0","librarian_archive 无 dryRun","回滚","归档分片","archived true","临时文件 _临时","引用更新","坑库命名","核心数据库固定路径","搬迁","归档","引用","改名","给建","核心","据库","数据","心数","预演","l1","范化","规范","化整","blockdustry"]
relatedFiles: ["D:\\Blockdustry\\任务\\T53_docs规范化整理.md","D:\\Blockdustry\\仓库\\docs\\产出\\L1\\T53_docs规范化整理_docs-规范化整理-dryRun预演-逐条搬迁表_L1.md","D:\\Blockdustry\\仓库\\docs\\产出\\L2\\T53_docs规范化整理_docs-规范化整理-dryRun预演-扩充细节_L2.md","D:\\Blockdustry\\仓库\\docs\\_临时","D:\\Blockdustry\\仓库\\docs\\核心数据库\\核心数据库.md","project.json","D:\\Blockdustry\\仓库\\docs\\核心数据库\\派生态索引.md","README.md","D:\\Blockdustry\\仓库\\docs\\archive\\2026-08\\传送带上下坡.md"]
createdAt: 2026-10-03T13:08:17.544Z
librarianTouchedAt: 2026-10-04T06:16:24.832Z
librarianChanges: ["馆员搬移","引用更新（搬移）","引用更新（搬移）","复核 keywords（新增 0 个）","引用修复（指向现存档）","复核 keywords（新增 0 个）","引用修复（指向现存档）","复核 keywords（新增 0 个）","引用修复（指向现存档）","复核 keywords（新增 1 个：blockdustry）"]
nextTier: D:\Blockdustry\仓库\docs\产出\L2\T53_docs规范化整理_docs-规范化整理-dryRun预演-扩充细节_L2.md
fullDetail: D:\Blockdustry\仓库\docs\产出\L1\T53_docs规范化整理_docs-规范化整理-dryRun预演-逐条搬迁表_L1.md
---

> **速览** 本项目：Blockdustry
> **速览** 本档（L3）给：上级 / 主代理（只想知道结论）
> **速览** 细节见 D:\Blockdustry\仓库\docs\产出\L1\T53_docs规范化整理_docs-规范化整理-dryRun预演-逐条搬迁表_L1.md
# T53 docs 规范化整理 —— dryRun 预演（高度概括，发上级）

## 结论

四份预演全部出齐，**除本档三件套外零落盘**。合并口径：**124 项待搬（冲突 0）**＝研究 18 + 修改 1 + 坑 1 + 核心数据库 1 + `子agent` 去 `^` 99 + T99 归位 4；**34 个 `_lang_*.json` 可搬 `docs\_临时\`**；**归档候选仅 3 篇**（只认正文自标废弃）；`sweep` 报待办 120、将覆盖 3 篇（均派生物）、改名 0、tags 过期 4、审计红 20→20 / 黄 45→45（无增无减）。

**⚠️ 一处事故已闭环**：`librarian_archive` **没有 `dryRun` 参数**，误调即真落盘 3 篇。我已用 `librarian_relocate` 搬回、剥掉 4 篇被注入的 YAML 头部、删掉 4 行指向不存在路径的日志，**逐篇与备份一字不差**；changelog 保留 3 行真实「搬回」记录留痕。**今后预演归档只能走 `librarian_relocate --dryRun`。**

**关键裁定**：① 99/99 legacy 档是自有单档格式，**档级不可派生 → `tier: 0`**，不猜 L1/L2/L3；② `archived: true` **由工具写入头部**，分片 `<YYYY-MM>` **按归档运行时刻**（不是标废日/createdAt）；③ `坑\` 18 篇主题档**维持现名**；④ `D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md` **本轮不移动**（正文 L554 自述固定路径 + 8 文件 9 处引用）；⑤ 6 个旧名断引用**仍成立**（约 8 文件 19 处）；⑥ **changelog 历史行不改，导引类引用一律改**。

**缺口**：`project.json` 无 `audit` 段 → `archiveAfterDays` 读不到，归档的档龄边界不可判；34 个 json 有 **14 处引用工具漏报**。

## 依据

实测现场：根目录 22 篇、`子agent\` 106 篇 md + 34 json、`坑\` 18 篇（比上轮多 1 篇 EXP-RELOCATE 坑档且**未入索引**）、`核心数据库\` 1 篇、`docs\_临时\` 已存在（含 6 篇 EXP 实验档）、`子agent\L1|L2|L3\` 与 `archive\` 不存在。relocate 各组回执：研究 18/0/0、修改+坑 2/0/0、核心数据库 1/0/1、去 `^` 三批 20+20+17=57（另 42 项见 L1）全部 0 冲突 0 引用、T99 4/0/6、json 8+8/0/0。sweep 预演：将覆盖 `D:\Blockdustry\仓库\docs\核心数据库\派生态索引.md`（+125/-108）、`坑\README.md`（+17/-16）、`D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md`（changelog）；正文零改动 106/106 指纹一致。

## 风险与待确认

待上级拍板 4 项：坑档命名例外、`D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md` 移动政策（含是否破例改 L554 一行）、归档记载冲突（`D:\Blockdustry\仓库\docs\archive\2026-08\传送带上下坡.md` 正文标废 vs `^T19` L44 记「未标废弃」）、json 落点（`D:\Blockdustry\docs\_临时\` 会切断台账内路径）。另注：上轮 L1/L2 产出档已不在盘上（仅备份）。

## 下一步

拍板后按「建 `L1|L2|L3\` → T99 归位 → 99 篇原地去 `^` → 根目录 20 篇归位 → json 搬迁 → 补引用（19 处 + 14 处）→ `sweep` 落地索引台账」分批执行；归档单列一批，先补 `archiveAfterDays`。

---

- 一级文档（详细归纳，124 项逐条搬迁表 + 回滚对账）：`D:\Blockdustry\仓库\docs\产出\L1\T53_docs规范化整理_docs-规范化整理-dryRun预演-逐条搬迁表_L1.md`
- 二级文档（扩充细节，发送主代理）：`D:\Blockdustry\仓库\docs\产出\L2\T53_docs规范化整理_docs-规范化整理-dryRun预演-扩充细节_L2.md`
