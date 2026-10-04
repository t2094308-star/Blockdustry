---
taskId: organize-docs-blockdustry-refs2
role: researcher
tier: 0
keywords: ["产出档改名派生实证与99篇对照表","taskid_unknown","派生","parseDocFileName","命名规范","类型后缀","_L1","_L2","_L3","doc_kind_mismatch","missing_progressive_link","doc_meta_missing","front-matter taskId","假任务号","P1 批次号","渐进式披露","nextTier","fullDetail","detailLevel","librarian_relocate","改名对照表","推荐","不推","p1_","无卡","修改","清单","整合","taskid"]
relatedFiles: ["D:\\Blockdustry\\任务\\organize-docs-blockdustry-refs2.md","D:\\DSH插件\\dsh-agent-contract\\src\\ledger\\naming.js","D:\\DSH插件\\dsh-agent-contract\\src\\ledger\\derive.js","D:\\DSH插件\\dsh-agent-contract\\src\\ledger\\progressive.js","D:\\DSH插件\\dsh-agent-contract\\src\\audit\\checks.js","D:\\DSH插件\\dsh-agent-contract\\src\\ledger\\docarea.js","D:\\Blockdustry\\仓库\\docs\\产出\\索引.md","D:\\Blockdustry\\仓库\\docs\\产出\\T52_待办路线图研究.md","D:\\Blockdustry\\仓库\\docs\\产出\\L3\\T99_契约流程冒烟测试_T99-冒烟测试研究员回报_L3.md","D:\\Blockdustry\\仓库\\docs\\坑\\EXP-RELOCATE_搬迁引用同步实验全过程_坑.md","D:\\Blockdustry\\任务\\organize-docs-blockdustry-元数据补全.md","D:\\DSH插件\\dsh-agent-contract","T16_提升机底部交接.md","T52_待办路线图研究.md","_fix.md","README.md","organize-docs-blockdustry_docs整理前现状_全量逐条清单.md","T16_科技树实现.md","T16_科技树深入实现.md","T38_A_物流生产液体_研究.md","T38_A_物流生产液体.md","核心数据库.md","派生态索引.md","EXP-RELOCATE_搬迁引用同步实验全过程_坑.md"]
createdAt: 2026-10-04T06:32:51.898Z
---

> **速览** 本项目：Blockdustry
> **速览** 本档（L0）给：读者
> **速览** 细节见本档 front-matter 的指针（fullDetail 指向 L1，另一档生成后由工具自动补齐）
## 结论

**口径总判（3 条可派生路径，全部对着确定性代码核过）**

1. **`taskId` 只从文件名派生，与「有无同名任务卡」无关** —— 证实上级判断。三处硬门槛：文件名尾部必须能认出类型后缀（`_研究/_审查/_整合清单/_坑/_修改/_核心数据库`）或 `_L1|_L2|_L3`，否则 `parseDocFileName` 返回 `null`（`naming.js:203-230`）。
2. **实测纠正上级卡片里的一处预期**：`T16_提升机底部交接.md` → `…_研究.md` 后派生出的 `taskId` 是 **`T16`**，**不是** `T16_提升机底部交接`。因为无显式 taskId 时走 `/^(.+?)_(.+)$/`（**非贪婪=第一个下划线**，`naming.js:216-218`）。`T16_提升机底部交接` 这种全串只在 **front-matter 显式写了 `taskId`** 时出现（`T52` 就是这种，见依据②）。
3. **三方案判定**：
   - **方案甲（加类型后缀）**：能派生，但**必须同时搬进对应类别目录**，否则新增黄条 `doc_kind_mismatch`（`checks.js:905-926`）。`tier` 记 0（`naming.js:208-218`）⇒ **不触发** `doc_meta_missing` / `missing_progressive_link` / `doc_tags_stale`（三条都显式跳过 `tier===0`，`checks.js:526/553/878`）。**可行，但要求逐篇裁决内容类别**。
   - **方案乙（加 `_L<n>`）**：能派生（`tier`=1/2/3），**但代价最大、明确不推荐**：tier≥1 立刻打开三条检查，其中 ①`doc_meta_missing`（要求固定小节 `结论/依据/风险与待确认/下一步`，`docmeta.js:15`）——**99 篇里 0 篇有这四个小节**（实测：`产出` 根 99 篇内 `^## 结论` 等零命中，61 处命中全在 `L1/L2/L3` 三档档内）；②`missing_progressive_link`（L1 要 `detailLevel: full`，L2/L3 要 `fullDetail`，`progressive.js:55-63`）⇒ 一篇档凭空变出 2~3 条黄。
   - **方案丙（只在 front-matter 补 `taskId`，不改名、不搬目录）＝ 第三条路，成立且最省**。`deriveFromFile` 把 `meta.taskId` 当 `overrides.taskId` 传给派生器，`derive.js:151` 的 `overrides.taskId || (named ? …)` 让 front-matter **优先**，于是 `taskid_unknown` 不再报（`derive.js:152`）。**有既成的真机结果作证**：`T52_待办路线图研究.md` 与两篇 `_fix.md` 文件名按规则都派生不出（`_fix` 被 `kindFromFileName` 判成 `产出档` ⇒ 也返回 `null`，`naming.js:154/209`），但它们的台账索引条目**已无「待办 taskid_unknown」标记**（`产出\索引.md:124/137/138`）。
4. **推荐的落地口径**：**69 篇 `T*`（真实任务号在文件名首段）+ 2 篇 `P*…研究`（如果要动）走方案丙**；**28 篇 `P1_*/P2_*` 是批次号，任何方案都只会派生出假任务号，建议保持不可派生**（`任务\` 下无任何 `P1_*/P2_*` 卡，实测 58 张卡里零命中）。`README.md`、`索引.md` **无法用甲/乙派生**（名字里没有下划线 ⇒ 正则不匹配 ⇒ `null`）；`索引.md` 是派生物，正确动作是**清掉它的 front-matter**（`checks.js:786-792` 把「派生物索引却有头」判黄），不是改名。

**汇总数字（现场 `glob`/`grep` 实测，基准 102 篇根档）**

| 项 | 数字 |
|---|---|
| `仓库\docs\产出\` 根层 `.md` | **102** |
| 被报 `taskid_unknown`（= fm `taskId: null`） | **99**（102 − T52 − 2 篇 `_fix`） |
| 其中 `T*` / `P1_*` / `P2_*` / README / 索引 | **69 / 27 / 1 / 1 / 1** |
| 方案甲可派生 | **97**（README、索引 各 1 不可派生；其中 **28 篇派生出假任务号 P1/P2**） |
| 方案乙可派生 | **97**（同上；但附带 ≈97×2 条新黄） |
| 方案丙可消 `taskid_unknown` | **99**（含 README/索引，但这两篇不该动 ⇒ 净 **97**；再排除 28 篇假任务号 ⇒ **69**） |
| 仍不可派生（甲/乙） | **2**（`README.md`、`索引.md`） |
| 假任务号清单 | **28 篇 P\***（见附录 B） |
| 同名任务卡命中 | **23 篇**（doc 主干 == 卡名，方案丙可直接用卡名当 taskId）＋ **8 篇「同任务、名不同」**；其余 68 篇无同名卡 |

## 依据

**① 源码（只读；插件 checkout `D:\DSH插件\dsh-agent-contract`）**
- `src\ledger\naming.js:147-156` `kindFromFileName`：类型后缀表 `KIND_SUFFIXES`（`:104-113`）+ `/_L[123]$/i → 产出档` + `/_fix$/i → 产出档`，否则 `null`。
- `src\ledger\naming.js:203-230` `parseDocFileName(fileName, taskId?)`：无 `_L<n>` 时 `if (!kind || kind === '产出档') return null`（`:209`）⇒ 类型档才有 tier 0；有 `_L<n>` 时 `tier=Number(...)`（`:220`）。
- `src\ledger\naming.js:68-81` `stripTaskPrefix` + `:212-218`：**传了显式 taskId 就按它精确剥前缀，没传才退回「第一个下划线」**。
- `src\ledger\derive.js:144-152` `deriveDocMeta`：`named = parseDocFileName(fileName, overrides.taskId)`；`taskId = overrides.taskId || (named ? named.taskId : null)`；`!taskId` 才推 `taskid_unknown`。
- `src\ledger\derive.js:205-225` `deriveFromFile`：`overrides.taskId = 非空字符串的 meta.taskId` ⇒ **front-matter 被采信**（方案丙成立）。
- `src\ledger\derive.js:154-161`：`tier = named ? named.tier : (metaTier 1~3 ? metaTier : 0)`；`legacy && !named` 才报 `legacy_no_tier`（99 篇都有头 ⇒ `legacy=false`，不会报这条）。
- `src\audit\checks.js:905-926` `doc_kind_mismatch`：`declared=kindFromFileName(name)` 与所在目录 `row.kind` 不符即黄（`产出` 根 = `产出档`）。
- `src\audit\checks.js:873-892` `missing_progressive_link` + `src\ledger\progressive.js:55-63`：tier 1 要 `detailLevel`，tier 2/3 要 `fullDetail`。
- `src\audit\checks.js:523-531` `doc_meta_missing`、`:550-570` `doc_tags_stale`：**都 `Number(doc.tier)===0` 直接跳过**；`src\ledger\docmeta.js:15` 固定小节 = `结论/依据/风险与待确认/下一步`。
- `src\ledger\docarea.js:242-275` `scanDocAreas`：产出根与 `L1|L2|L3` 子目录都算 `产出档`（`L<n>` 只影响 `tier`，不影响 kind 比对）。
- `src\ledger\derive.js:18-23`：`taskid_unknown` 是 **needs_librarian 待办码**，不是 audit 红黄项（它在 `checks.js` 的 `CHECK_LEVELS` 表里不存在，`:36-72`）⇒ 「清待办」的收益是**关掉馆员唤醒闸门**（`tools.js:1089/1110`），不是清红黄。

**② 台账既成事实（真机运行结果，非推断）**
- `仓库\docs\产出\T52_待办路线图研究.md:2` `taskId: T52_待办路线图研究`，`:9` `librarianChanges` 自述「补 front-matter（taskId/role/tier/…）」，时间是 `2026-10-03T16:28`；而 `产出\索引.md:124` 该条**没有**「待办 taskid_unknown」（同页 38-123 行的 86 条几乎全带）⇒ 补头确实消掉了待办。
- 两篇 `…_fix.md`（`:137`/`:138`）同样带 fm taskId、同样无待办标记 —— 而它们的文件名按 `naming.js:154/209` **必然派生为 null** ⇒ 这是方案丙被采信的第二处独立证据。
- 反例（证明「无后缀就判不出」）：`产出\L1\organize-docs-blockdustry_docs整理前现状_全量逐条清单.md` 同样无后缀、`taskId: null`，索引第 24 行仍带「待办 taskid_unknown」。

**③ 现场统计**
- `glob 仓库/docs/产出/*.md` = 102；`glob 产出/**/*.md` = 119（含 17 篇 `L1|L2|L3`）。
- `grep ^taskId:` 在 `产出` 树 = 119 命中，根层只有 3 篇非 null（T52 + 2 × `_fix`）。
- `grep ^tier:` 在 `产出` 树 = 120 命中，根层 99 篇全为 `tier: 0`。
- 固定小节零命中（`^##\s+(结论|依据|风险与待确认|下一步)$` 在 99 篇根档里 0 条）。
- 任务卡引用面抽样：`任务\*.md` 里命中 82 处「`仓库\docs\产出\<T*/P*>.md` 绝对路径」（**约 50 篇被任务卡以「写/阶段产出」引用**，例如 `T16_科技树实现.md:24` → `T16_科技树深入实现.md`）；`docs` 内部互引**本轮未逐篇重算**（见风险）。

## 风险与待确认

1. **未做真机运行**：本会话无 shell、白名单内无 `ledger_backfill`/`audit_scan`/`librarian_*`（上级明确「判不准只上报」）。故甲/乙两条是**源码级实证**（确定性代码，置信高），丙是**源码 + 台账既成事实双重实证**。一键复核方式：`ledger_backfill --dryRun`（应报「将补 0 篇」，因 99 篇 fm 全为 `taskId: null`，不构成 meta 值）或对任一篇改名后跑 `ledger_rebuild` + `audit_scan`。
2. **甲、乙方案的派生 taskId 会被「第一个下划线」截断**：`T38_A_物流生产液体_研究.md → taskId=T38`（不是 `T38_A`，而 `任务\T38_A_物流生产液体.md` 这张卡的主干是 `T38_A`）。多段任务号（`T38_A/B/C`、`T27_物流_A1`、`P1_批1A_A1`）都受此影响。**未验证**：台账是否会因此报 `duplicate_suspect`（`T38_A/B/C` 三篇若都取 `T38` 且 tier/标题不同，按 `derive.js:265-284` 不会重复）。
3. **引用面**：约 50 篇被任务卡以绝对路径引用；`docs` 内部（`核心数据库.md`、`派生态索引.md`、各 `索引.md`、其他产出档的 `relatedFiles`）的互引**未逐篇重算**（预算内不做全库 grep）。既有实验记录（`仓库\docs\坑\EXP-RELOCATE_搬迁引用同步实验全过程_坑.md:37,41,49`）指出：引用改写是**纯字面匹配**，`仓库/docs/…` 斜杠相对写法与裸文件名**不会命中**，且台账/改写范围与磁盘范围可能不一致 ⇒ **改名前必须先跑 `librarian_relocate` 的 dryRun 看「影响引用 N 处」**。
4. **搬目录的连带**：把档搬进 `研究\`/`整合清单\` 等类别目录后，那些目录的 `索引.md` 会与磁盘不符 ⇒ 需同批跑索引重建，否则 `missing_kind_index` 黄（`checks.js:753-757`）。
5. **判不准项（只上报，不猜）**：69 篇 `T*` 里 H1 字面能定类的仅 **15 篇「研究」**，其余多为「XX 阶段产出 / 修复 / 修正 / 排查」混合体例，`修改` 类别是否接纳「阶段产出」体例**未验证**（`仓库\docs\修改\` 现仅 1 篇正文档），故本档对它们给「判不准 ⇒ 走方案丙」的建议。
6. **`P1`/`P2` 语义**：28 篇的 `P1_批xx_yy` 是**批次号**，卡目录零命中；给它们写任何 `taskId`（无论 `P1` 还是自造）都是编造任务号 —— 与「判不出就不猜」冲突，**建议保持不可派生并在待办里显式豁免**。
7. **与上级卡片的口径不一致处**（按纪律标注）：卡片 §二-1 预期 `taskId` 会变成 `T16_提升机底部交接`，实测规则给出的是 `T16`；`README.md` 用类型后缀**也派生不出**（名字无下划线），卡片未预料这一点。

## 下一步

1. **上级拍板**：① 是否只清 69 篇 `T*` 的待办（走方案丙）？② 28 篇 `P*` 是否**显式豁免**？③ `README.md`/`索引.md` 是否只做「清 `索引.md` 的 front-matter」？
2. **方案丙落地**（推荐、零改名零搬目录）：派 `librarian` 分两批——先 dryRun 列出「将补 taskId 的 69 篇 + 拟填值（= 同名卡主干优先，无卡者用文件名首段）」，落盘后 `ledger_rebuild` 复核 `taskid_unknown` 条数（99 → 30）。
3. **若坚持方案甲**：必须同批做「改名 + 搬类别目录 + `librarian_relocate` 引用同步（预演先看处数）+ 类别索引重建」，并接受 15 篇研究、21 篇整合清单、2 篇审查以外的**内容类别裁决责任**。
4. **方案乙明确不建议**；除非上级同时批准给 97 篇补 `结论/依据/风险与待确认/下一步` 固定小节 + 渐进式披露指针（那是**内容改写**，已超出「整理」范畴）。
5. 复核命令（上级执行一次即可把本档从「源码级」升到「运行级」）：对 `仓库\docs\_临时\` 里复制出的两篇假名档（如 `T16_提升机底部交接_研究.md`、`T16_提升机底部交接_L1.md`）跑 `ledger_rebuild`，看台账 `taskId/tier` 是否分别为 `T16/0`、`T16/1`。**本轮未创建任何临时文件**（未落盘、无残留）。

---

## 附录 A：99 篇对照表

列：`现文件名` ｜ `类别（H1 字面证据）` ｜ `甲：新名 → 目录` ｜ `乙：_L<n>` ｜ `同名任务卡/风险`。
**甲式新名 = 原名 + `_<类别>.md`；乙式新名 = 原名 + `_L1.md`**（规则机械，故「判不准」行不给新名，只给方案丙）。
类别标记：`研`=研究（H1 明确「研究」）｜`整`=整合清单｜`审`=审查报告｜`修`=修改（H1 明确修复/回滚）｜`判`=判不准（阶段产出混合体例，建议走方案丙）。

| # | 现文件名 | 类别 | 甲（类型后缀）→ 目录 | 乙（`_L<n>`） | 卡/风险 |
|---|---|---|---|---|---|
| 1 | T1_传送带转角.md | 判 | — | 不推荐 | 卡✓ |
| 2 | T2_上帝视角.md | 研 | T2_上帝视角_研究.md → 研究\ | T2_上帝视角_L1.md | 卡✓ |
| 3 | T3_灵魂出窍.md | 研 | T3_灵魂出窍_研究.md → 研究\ | T3_灵魂出窍_L1.md | 卡✓ |
| 4 | T4_分裂炮与标签.md | 判 | — | 不推荐 | 卡✓ |
| 5 | T4b_分裂炮贴图尺寸.md | 修 | T4b_分裂炮贴图尺寸_修改.md → 修改\ | 不推荐 | 无卡 |
| 6 | T5_炮台附身.md | 判 | — | 不推荐 | 卡✓ |
| 7 | T6_矿机叶片.md | 判 | — | 不推荐 | 卡✓ |
| 8 | T6b_钻头侧面贴图.md | 判 | — | 不推荐 | 无卡 |
| 9 | T6c_钻头侧面贴图应用.md | 判 | — | 不推荐 | 无卡 |
| 10 | T6d_钻头真侧面贴图.md | 判 | — | 不推荐 | 无卡 |
| 11 | T7_装甲机制.md | 判 | — | 不推荐 | 卡✓ |
| 12 | T8_核心正方体.md | 判 | — | 不推荐 | 卡✓ |
| 13 | T8b_核心贴图修复.md | 修 | T8b_核心贴图修复_修改.md → 修改\ | 不推荐 | 无卡 |
| 14 | T8c_核心分裂黑箱修复.md | 修 | T8c_核心分裂黑箱修复_修改.md → 修改\ | 不推荐 | 无卡 |
| 15 | T9_三维物流.md | 判 | — | 不推荐 | 卡✓ |
| 16 | T9a_贴图镜像与核心碰撞.md | 修 | T9a_贴图镜像与核心碰撞_修改.md → 修改\ | 不推荐 | 无卡 |
| 17 | T9b_附身黑屏退出修复.md | 修 | T9b_附身黑屏退出修复_修改.md → 修改\ | 不推荐 | 无卡 |
| 18 | T10_blockhealth深度联系.md | 判 | — | 不推荐 | 卡✓ |
| 19 | T11_传送带贴图.md | 判 | — | 不推荐 | 无卡 |
| 20 | T11_炮台贴图.md | 判 | — | 不推荐 | 无卡 |
| 21 | T12_传送带转角颠倒.md | 修 | T12_传送带转角颠倒_修改.md → 修改\ | 不推荐 | 无卡 |
| 22 | T12_核心背面消失.md | 修 | T12_核心背面消失_修改.md → 修改\ | 不推荐 | 无卡 |
| 23 | T12_炮台侧面贴图.md | 修 | T12_炮台侧面贴图_修改.md → 修改\ | 不推荐 | 无卡 |
| 24 | T12_附身俯仰.md | 判 | — | 不推荐 | 无卡 |
| 25 | T13_传送带侧面.md | 判 | — | 不推荐 | 无卡 |
| 26 | T13_核心余光剔除.md | 修 | T13_核心余光剔除_修改.md → 修改\ | 不推荐 | 无卡 |
| 27 | T13_灵魂出窍应用.md | 判 | — | 不推荐 | 无卡 |
| 28 | T13_炮台侧面重绘与命名.md | 修 | T13_炮台侧面重绘与命名_修改.md → 修改\ | 不推荐 | 无卡 |
| 29 | T13_附身发射点红线.md | 判 | — | 不推荐 | 无卡 |
| 30 | T14_火焰炮电弧.md | 判 | — | 不推荐 | 无卡 |
| 31 | T14_电力整合.md | 判 | — | 不推荐 | 无卡 |
| 32 | T14_科技树研究.md | 研 | T14_科技树研究_研究.md → 研究\ | T14_科技树研究_L1.md | 卡✓ |
| 33 | T14_立体物流初步.md | 判 | — | 不推荐 | 无卡 |
| 34 | T15_Mindustry光效研究.md | 研 | T15_Mindustry光效研究_研究.md → 研究\ | T15_Mindustry光效研究_L1.md | 卡~（卡名 T15_光效研究） |
| 35 | T15_提升机异常.md | 修 | T15_提升机异常_修改.md → 修改\ | 不推荐 | 无卡 |
| 36 | T15_灵魂出窍误触发.md | 修 | T15_灵魂出窍误触发_修改.md → 修改\ | 不推荐 | 无卡 |
| 37 | T15_电弧基座.md | 修 | T15_电弧基座_修改.md → 修改\ | 不推荐 | 无卡 |
| 38 | T16_提升机底部交接.md | 修 | T16_提升机底部交接_修改.md → 修改\ | 不推荐 | 无卡（甲/乙派生 taskId=T16） |
| 39 | T16_灵魂出窍双击深查.md | 判 | — | 不推荐 | 无卡 |
| 40 | T16_电力节点倒角.md | 判 | — | 不推荐 | 无卡 |
| 41 | T16_科技树深入实现.md | 判 | — | 不推荐 | 卡~（卡名 T16_科技树实现；卡内引用它） |
| 42 | T17_提升机生效确认.md | 判 | — | 不推荐 | 无卡 |
| 43 | T17_灵魂出窍C键.md | 修 | T17_灵魂出窍C键_修改.md → 修改\ | 不推荐 | 无卡 |
| 44 | T17_电力节点倒角重做.md | 判 | — | 不推荐 | 无卡 |
| 45 | T18_创造栏分类.md | 判 | — | 不推荐 | 无卡 |
| 46 | T18_提升机顶部直连传送带.md | 修 | T18_提升机顶部直连传送带_修改.md → 修改\ | 不推荐 | 无卡 |
| 47 | T18_科技树实现A.md | 判 | — | 不推荐 | 卡✓ |
| 48 | T18_科技树实现B.md | 判 | — | 不推荐 | 卡✓ |
| 49 | T19_删除爬坡回滚.md | 修 | T19_删除爬坡回滚_修改.md → 修改\ | 不推荐 | 无卡 |
| 50 | T19_提升机顶格Y1输出与底面贴图.md | 修 | T19_提升机顶格Y1输出与底面贴图_修改.md → 修改\ | 不推荐 | 无卡 |
| 51 | T20_材料迁移与物品源.md | 判 | — | 不推荐 | 无卡 |
| 52 | T20_科技树树形UI.md | 判 | — | 不推荐 | 卡✓ |
| 53 | T21_BlockHealth整组血量.md | 判 | — | 不推荐 | 卡✓ |
| 54 | T22_材料调整.md | 判 | — | 不推荐 | 卡~（卡名 T22_科技树解锁指令） |
| 55 | T22_科技树解锁指令.md | 判 | — | 不推荐 | 卡✓ |
| 56 | T23_科技树UI精确复刻.md | 判 | — | 不推荐 | 卡✓ |
| 57 | T25_科技树彻底重做.md | 判 | — | 不推荐 | 卡✓ |
| 58 | T26_dagger3D模型.md | 判 | — | 不推荐 | 无卡（MCP 阻塞报告） |
| 59 | T28_雷光光效研究.md | 研 | T28_雷光光效研究_研究.md → 研究\ | T28_雷光光效研究_L1.md | 卡✓ |
| 60 | T35_siliconSmelter冒烟特效研究.md | 研 | T35_siliconSmelter冒烟特效研究_研究.md → 研究\ | T35_siliconSmelter冒烟特效研究_L1.md | 卡~（卡名 T35_硅冶炼厂） |
| 61 | T36_kiln火焰特效研究.md | 研 | T36_kiln火焰特效研究_研究.md → 研究\ | T36_kiln火焰特效研究_L1.md | 卡~（卡名 T36_窖炉） |
| 62 | T38_A_物流生产液体.md | 研 | T38_A_物流生产液体_研究.md → 研究\ | T38_A_物流生产液体_L1.md | 卡✓；派生 taskId 会被截成 T38 |
| 63 | T38_B_电力防御炮塔.md | 研 | T38_B_电力防御炮塔_研究.md → 研究\ | T38_B_电力防御炮塔_L1.md | 卡✓；同上 |
| 64 | T38_C_单位逻辑物品.md | 判 | — | 不推荐 | 卡✓；同上 |
| 65 | T46_pulverizerIncinerator特效研究.md | 研 | T46_pulverizerIncinerator特效研究_研究.md → 研究\ | T46_pulverizerIncinerator特效研究_L1.md | 卡~（卡名 T46_粉碎机焚烧炉） |
| 66 | T47_phaseWeaver织机特效研究.md | 研 | T47_phaseWeaver织机特效研究_研究.md → 研究\ | T47_phaseWeaver织机特效研究_L1.md | 卡~（卡名 T47_相织布织机） |
| 67 | T49_维修光束力场特效研究.md | 研 | T49_维修光束力场特效研究_研究.md → 研究\ | T49_维修光束力场特效研究_L1.md | 卡~（卡名 T49_维修场力场） |
| 68 | T50_审查修复清单.md | 判 | — | 不推荐 | 卡~（卡名 T50_修复批1CD审查中危） |
| 69 | T51_分裂炮台像素绘制研究.md | 研 | T51_分裂炮台像素绘制研究_研究.md → 研究\ | T51_分裂炮台像素绘制研究_L1.md | 卡✓ |
| 70-90 | 21 篇 `P1_批*/…整合清单.md` | 整 | `…_整合清单.md` → 整合清单\ | 不推荐 | **假任务号 P1**；无卡；被 T27/T29/T30/T32…T49 卡引用 |
| 91 | P1_批1A_审查报告.md | 审 | P1_批1A_审查报告_审查.md → 审查\ | 不推荐 | **假任务号 P1**；被 T39 卡引用 |
| 92 | P1_批1CD_审查报告.md | 审 | P1_批1CD_审查报告_审查.md → 审查\ | 不推荐 | **假任务号 P1**；被 T39/T50 卡引用 |
| 93 | P1_批1E_diodeSurgeTower光效研究.md | 研 | P1_批1E_diodeSurgeTower光效研究_研究.md → 研究\ | 不推荐 | **假任务号 P1**；被 T42 卡引用 |
| 94 | P1_批1F_door开关动画研究.md | 研 | P1_批1F_door开关动画研究_研究.md → 研究\ | 不推荐 | **假任务号 P1**；被 T44 卡引用 |
| 95 | P1_中文名修正.md | 判 | — | 不推荐 | **假任务号 P1**；无卡 |
| 96 | P1_大规模迁移计划.md | 判 | — | 不推荐 | **假任务号 P1**；被 T52 卡只读引用 |
| 97 | P2_dsh多智能体契约插件设计v1.md | 判 | — | 不推荐 | **假任务号 P2**；无卡（正文 H1 写的是「设计 v2」） |
| 98 | README.md | 判 | **不可派生**（名内无下划线） | 不可派生 | 产出口录说明；建议保持现状 |
| 99 | 索引.md | 判 | **不可派生** | 不可派生 | 派生索引；建议**清掉 front-matter**（`null_frontmatter` 黄，`checks.js:786-792`） |

> 第 70-90 行合写：`P1_批1A_A1/A2/A3`、`批1B_container`、`批1B_itemBridge`、`批1C_kiln`、`批1C_phaseWeaver`、`批1C_plastaniumCompressor`、`批1C_pulverizerIncinerator`、`批1C_pyratiteMixer`、`批1C_siliconSmelter`、`批1D_blastDrill`、`批1D_laserDrill`、`批1D_pneumaticDrill`、`批1E_diodeSurgeTower整合清单`、`批1E_powerNodeLargeBatteryLarge`、`批1E_solarPanel`、`批1F_copperScrapWall`、`批1F_titaniumWallDoor`、`批2A_高级墙体`、`批2B_menderForceProjector`、（H1 均为「整合清单」）。逐篇 `甲 = 原名 + _整合清单.md → 整合清单\`（若原名已是 `…整合清单.md` 则**只需补一个下划线**：`P1_批1A_A1_整合清单.md`）。

## 附录 B：假任务号（`P1`/`P2`）清单 —— 建议「保持不可派生」

`P1_中文名修正`、`P1_大规模迁移计划`、`P1_批1A_A1整合清单`、`P1_批1A_A2整合清单`、`P1_批1A_A3整合清单`、`P1_批1A_审查报告`、`P1_批1B_container整合清单`、`P1_批1B_itemBridge整合清单`、`P1_批1CD_审查报告`、`P1_批1C_kiln整合清单`、`P1_批1C_phaseWeaver整合清单`、`P1_批1C_plastaniumCompressor整合清单`、`P1_批1C_pulverizerIncinerator整合清单`、`P1_批1C_pyratiteMixer整合清单`、`P1_批1C_siliconSmelter整合清单`、`P1_批1D_blastDrill整合清单`、`P1_批1D_laserDrill整合清单`、`P1_批1D_pneumaticDrill整合清单`、`P1_批1E_diodeSurgeTower光效研究`、`P1_批1E_diodeSurgeTower整合清单`、`P1_批1E_powerNodeLargeBatteryLarge整合清单`、`P1_批1E_solarPanel整合清单`、`P1_批1F_copperScrapWall整合清单`、`P1_批1F_door开关动画研究`、`P1_批1F_titaniumWallDoor整合清单`、`P1_批2A_高级墙体整合清单`、`P1_批2B_menderForceProjector整合清单`、`P2_dsh多智能体契约插件设计v1`。共 **28 篇**（`P1` 27 + `P2` 1）。判据：`任务\` 下 58 张卡里无任何 `P1_*`/`P2_*`；这些编号是「批次」而非任务。

## 附录 C：23 篇「doc 主干 == 任务卡名」（方案丙可直接取卡名当 taskId）

`T1_传送带转角`、`T2_上帝视角`、`T3_灵魂出窍`、`T4_分裂炮与标签`、`T5_炮台附身`、`T6_矿机叶片`、`T7_装甲机制`、`T8_核心正方体`、`T9_三维物流`、`T10_blockhealth深度联系`、`T14_科技树研究`、`T18_科技树实现A`、`T18_科技树实现B`、`T20_科技树树形UI`、`T21_BlockHealth整组血量`、`T22_科技树解锁指令`、`T23_科技树UI精确复刻`、`T25_科技树彻底重做`、`T28_雷光光效研究`、`T38_A_物流生产液体`、`T38_B_电力防御炮塔`、`T38_C_单位逻辑物品`、`T51_分裂炮台像素绘制研究`（另 `T52` 已完成）。其余 46 篇 `T*` 无同名卡，`taskId` 取值需上级裁定「用文件名首段」还是「用所属卡名」。
