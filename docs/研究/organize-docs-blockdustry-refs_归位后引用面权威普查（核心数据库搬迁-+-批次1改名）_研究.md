---
taskId: organize-docs-blockdustry-refs
role: researcher
tier: 0
keywords: ["归位后引用面权威普查（核心数据库搬迁-+-批次1改名）","引用面普查","断引用","核心数据库.md","docs/核心数据库.md","核心数据库\\核心数据库.md","派生态索引","去 ^ 前缀","改名","归位","relatedFiles","front-matter","P1_lang_json","子agent L1 L2 L3","任务卡资源声明","绝对路径 相对路径 裸文件名","核心","数据","据库","心数","blockdustry","agent","路径","引用"]
relatedFiles: ["D:\\Blockdustry\\仓库\\docs\\核心数据库\\核心数据库.md","D:\\Blockdustry\\仓库\\docs\\核心数据库\\派生态索引.md","D:\\Blockdustry\\仓库\\README.md","D:\\Blockdustry\\待办.md","D:\\Blockdustry\\仓库\\待办.md","D:\\Blockdustry\\[Agent进度]\\organize-docs-blockdustry.md","D:\\Blockdustry\\任务\\核心数据库维护.md","D:\\Blockdustry\\任务\\T38_核心数据库.md","D:\\Blockdustry\\任务\\T52_待办路线图研究.md","D:\\Blockdustry\\仓库\\docs\\产出\\T52_待办路线图研究.md","D:\\Blockdustry\\仓库\\docs\\产出\\L1\\T53_docs规范化整理_docs-规范化整理-dryRun预演-逐条搬迁表_L1.md","D:\\Blockdustry\\仓库","D:\\Blockdustry\\任务","D:\\Blockdustry\\[Agent进度","D:\\Blockdustry\\docs\\_临时","D:\\Blockdustry\\...","72\\205","D:\\Blockdustry\\仓库\\docs\\产出\\P1_批1C_pyratiteMixer整合清单.md","D:\\Blockdustry\\仓库\\docs\\产出\\P1_批1E_solarPanel整合清单.md","D:\\Blockdustry\\仓库\\docs\\产出\\P1_批1F_copperScrapWall整合清单.md","D:\\Blockdustry\\仓库\\docs\\坑\\EXP-RELOCATE_搬迁引用同步实验全过程_坑.md","D:\\Blockdustry\\任务\\organize-docs-blockdustry.md"]
createdAt: 2026-10-03T15:33:18.073Z
librarianTouchedAt: 2026-10-03T16:28:21.557Z
librarianChanges: ["引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）","引用更新（搬移）"]
---

> **速览** 本项目：Blockdustry
> **速览** 本档（L0）给：读者
> **速览** 细节见本档 front-matter 的指针（fullDetail 指向 L1，另一档生成后由工具自动补齐）
# 归位后引用面权威普查

> 只读普查。范围：`D:\Blockdustry\仓库\`（排除 `.agent-contract\backups\`、排除 `仓库\src\`）、`D:\Blockdustry\任务\`、`D:\Blockdustry\待办.md`、`D:\Blockdustry\[Agent进度]\`。
> 本次**未修改任何既有文件**；已核对 `.agent-contract\backups\` 内确实残留大量旧名副本，按要求排除。

## 结论

### 0. 首要事实：任务前提已过期，批次 1 已经落盘
交接卡 L26 写「搬迁前的 dryRun 预演只估到 8 文件 9 处，已证实严重偏低」「落地一步没做」，但现场**批次 1 已执行完毕**：
- `子agent\` 根层 99 个 `^` 文件 → 归位 2 篇归档 + 97 篇去 `^`（`核心数据库.md` changelog L596-L597、L610-L706 逐条留痕）。
- 根层 19 篇 → `研究\` 16 篇 + `修改\` 1 篇 + `坑\` 1 篇（改名为 `渲染与模型坑索引.md`）；T99 四篇 → `子agent\L{1,2,3}\`（changelog L600-L603、L707-L726）。
- 34 个 `^P1_*_lang_{en,zh}.json` → `D:\Blockdustry\docs\_临时\`（changelog L749-L782）；`glob **/_lang_*.json` 在仓库内**命中 0**。
- 根层现在只剩 4 个 md：`核心数据库.md`、`organize-docs-blockdustry_docs整理前现状_全量逐条清单.md`、以及本次普查新生的研究档。
- `核心数据库.md` **尚未搬家**（仍是 `仓库\docs\核心数据库.md`），**真值 816 行、无 front-matter、自引用仅 L554**（`[Agent进度]\organize-docs-blockdustry.md` L10 写 609 行，同样过期）。
- **结论：本任务实际是「断引用普查」而非「搬迁前普查」**；下面按「要改的活引用」与「不该动的历史行」分开给。

### 1. 三类引用面的权威清单（详见依据节，逐条含路径+行号+原文）
按「现在改了会不会让文档指向不存在的路径」分两组：

**A 组｜`核心数据库.md` 归位时才需要改的活引用（22 文件 / 30 处）**
- 正文路径字符串 20 文件 / 27 处；front-matter `relatedFiles` 4 篇 / 4 处，其中 2 处（`EXP-RELOCATE_..._坑.md` L6、`T52_待办路线图研究.md` L6）与正文同文件，**去重后 22 文件 / 30 处**。
- 已知锚点**全部复核成立**：正文自身 L554、`核心数据库\派生态索引.md` L5、`仓库\README.md` L62、`待办.md` L7/L67、`仓库\待办.md` L7/L66、三篇整合清单、`任务\核心数据库维护.md` L3/L9/L15、`任务\T38_核心数据库.md` L12/L15、`任务\T52_待办路线图研究.md` L8、`子agent\T52_待办路线图研究.md` L17。
- **补全的 4 处（摘要未列，均属「正文里的路径字符串」）**：
  1. `仓库\docs\核心数据库.md` **L9**「> - 计划依据：`docs/子agent/^P1_大规模迁移计划.md`」—— 引用**已改名**的 P1 档（批次 1 命中断引用）；同文件 L65、L234 同型（见 B 组）。
  2. `待办.md` **L63** 与 `仓库\待办.md` **L62**「核心数据库完善（目录锚点、已迁 78/未迁 127、README 迁移进度节）」—— **裸文件名·非路径**写法，摘要说「各 2 行」实际计数口径不同（见下）。
  3. **本次任务自身的盘点/预演档 5 篇全部是引用方**：根层 `organize-docs-blockdustry_docs整理前现状_全量逐条清单.md`（L45/L48/L49/L216/L222/L229）、`子agent\L1\organize-docs-blockdustry_docs整理前现状盘点-详细归纳_L1.md`（L6 `relatedFiles`）、`子agent\L1\organize-docs-blockdustry_docs整理前现状盘点-逐条清单_L1.md`（L6/L91/L206/L396/L417）、`子agent\L2\organize-docs-blockdustry_docs整理前现状盘点-扩充细节_L2.md`（L6/L27/L32/L37）、`子agent\L3\organize-docs-blockdustry_docs整理前现状盘点-高度概括_L3.md`（L17）。
  4. `任务\organize-docs-blockdustry.md`（L13/L32 只读登记、L43/L44 摘要风险）、`任务\organize-docs-blockdustry-交接卡.md`（L26/L50）、`任务\organize-docs-blockdustry-落地.md`（L59-L60）、`任务\T53_docs规范化整理.md`（L6/L10/L16/L17/L22）、`[Agent进度]\organize-docs-blockdustry.md`（L6/L7）—— 这些是「任务卡与进度档里的资源声明」，摘要未列。

**B 组｜批次 1 改名已经造成的断引用（改名即断，与 `核心数据库.md` 是否搬无关）**
- 活引用（共 10 文件 / 16 处）：`仓库\docs\子agent\P1_批1C_phaseWeaver整合清单.md` L116、`P1_批1C_pulverizerIncinerator整合清单.md` L144、`P1_批1C_pyratiteMixer整合清单.md` L97、`P1_批1E_solarPanel整合清单.md` L24、`P1_批1F_copperScrapWall整合清单.md` L18、`P1_批1E_diodeSurgeTower整合清单.md` L105、`P1_批1E_powerNodeLargeBatteryLarge整合清单.md` L116、`P1_批1F_titaniumWallDoor整合清单.md` L132/L137、`P1_批2A_高级墙体整合清单.md` L131、`P1_批2B_menderForceProjector整合清单.md` L118、`P1_批1C_kiln整合清单.md` L15、`P1_批1C_siliconSmelter整合清单.md` L100、`P1_批1A_A3整合清单.md` L30/L78、`P1_批1B_itemBridge整合清单.md` L33/L80、`P1_批1D_pneumaticDrill整合清单.md` L34/L102、`子agent\T18_提升机顶部直连传送带.md` L16、`子agent\T20_材料迁移与物品源.md` L39/L83、`子agent\T22_材料调整.md` L15、`子agent\T23_科技树UI精确复刻.md` L56、`子agent\T22_科技树解锁指令.md` L35、`子agent\T19_删除爬坡回滚.md` L69、`坑\物流交接.md` L16/L37、`坑\API签名与编译.md` L14/L32、`坑\炮塔黑色阴影.md` L59/L214、`坑\炮塔黑色阴影2.md` L85/L92/L136/L161-L163、`研究\` 各篇自引 —— 摘要里的「6 个旧名断引用（约 8 文件 19 处）」只覆盖其中一部分。
- **旧名 → 新名映射的权威来源**：`仓库\docs\核心数据库.md` changelog **L596-L602、L610-L726、L749-L782**（129 行）逐条记 `旧绝对路径(=相对仓库根) → 新路径`，是本轮唯一可机读的全量映射；其余登记档（`L1` 预演、`派生态索引`）写的是**改名前的旧名**。
- `仓库\docs\核心数据库\派生态索引.md` 是**派生物**（L1 注释 + L4 自述），L5-L124 登记的 106 条路径**全部指向旧名/旧目录**（P1 带 `^`、T* 无 `L{1,2,3}` 前缀、T99 四篇缺 `L*/`），**不可手改**，应靠 `librarian_sweep` / `ledger_rebuild` 重建。

**C 组｜不该动的历史行（改了会抹掉真实轨迹）**
- `仓库\docs\核心数据库.md` L554-L816 区间的 `[命名规范化]` / `[馆员搬移]` / `[归档]` / `[引用更新]` 记录：**L564-L599（10-02 与 10-03 13:05 事故痕迹）+ L610-L782（批次 1 搬迁痕迹）**，以及 `核心数据库.md` 正文 L9/L65/L234 这类**导引类引用**（导引类**要改**，历史行**不改**，是本项目已定的政策，见 `L1\T53_..._逐条搬迁表_L1.md` L332）。
- 口径确认：`关键数据库.md` L556 自述「历史行不改写」（L1 档 L332 引述）。

### 2. 三种写法各命中多少（仅统计「指向当前已不存在的路径」的活引用）
| 写法 | A 组（核心数据库归位） | B 组（批次 1 改名已断） | 合计 |
|---|---|---|---|
| 绝对路径 `D:\Blockdustry\...` | 正文 6 处 + front-matter 4 处 = **10** | 正文 3 处 + front-matter 4 处 = **7** | **17** |
| 相对路径 `docs/...`、`仓库/docs/...`、`./docs/...` | 正文 **17** | 正文 **≥30** | **≥47** |
| 裸文件名（无目录，如 `` `核心数据库.md` ``、`` `^T19_删除爬坡回滚.md` ``） | 正文 **3**（`待办.md` L63、`仓库\待办.md` L62、预演档计数行） | 正文 **≥26** | **≥29** |
> 计数口径：front-matter 一行内含多条路径的按「1 篇 1 处」记（`relatedFiles` 是数组，同一 element 只算 1 处）；历史行（C 组）不计入；`仓库\docs\核心数据库.md` changelog 内 129 行**全部不计入**。**未验证**：B 组「≥」号的精确值未逐行穷举，本轮抽查覆盖 12 个代表性文件。

### 3. 本任务自身的产出档也是引用方
根层 `organize-docs-blockdustry_docs整理前现状_全量逐条清单.md` 与 `子agent\L1|L2|L3\` 的 5 篇盘点/预演档，正文大段登记旧路径（含 `^研究-*` 18 条、`^P1_*` 26 条、`^T*` 71 条、34 个 json 的全绝对路径表 L66-L99），必须同批改或被 sweep 重建覆盖。另：`子agent\L1\organize-docs-blockdustry_docs整理前现状盘点-逐条清单_L1.md` 是研究员误生成的**重复副本**（交接卡 L59 已登记「只上报不删」）。

## 依据

### A 组逐条（绝对文件路径 + 行号 + 该行原文）
**A1 正文里的路径字符串（20 文件 / 27 处）**

1. `D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md` L554：
   `- 主文档命名固定 `D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md`，长期维护喵。` —— **绝对路径**
2. `D:\Blockdustry\仓库\docs\核心数据库\派生态索引.md` L5：
   `> 真身（唯一权威，勿删）：D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md` —— **绝对路径**（派生物，靠 sweep 重建）
3. `D:\Blockdustry\仓库\README.md` L62：
   `详见 [核心数据库](./docs/核心数据库.md)` —— **相对路径（Markdown 链接语法）**
4. `D:\Blockdustry\待办.md` L7：
   `> 当前 72/205：… 总地图 `docs/核心数据库.md`（T38 研究产出…）…` —— **相对路径**；同文件 L63 `- [x] 核心数据库完善（…）` —— **裸文件名**；L67 `` - [x] 核心数据库（T38，docs/核心数据库.md：已迁 40/迁移中 6/未迁 159 …） `` —— **相对路径**
5. `D:\Blockdustry\仓库\待办.md` L7 同款 **相对路径**；L62 裸文件名；L66 **相对路径**（该文件是 `D:\Blockdustry\待办.md` 的旧副本，数字为 78/205）
6. `D:\Blockdustry\任务\核心数据库维护.md` L3：`> 专门维护 `D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md`（迁移总地图…）` —— **绝对路径**；L9：`写：`D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md`、`D:\Blockdustry\仓库\README.md`` —— **绝对路径**（2 条）；L15：`读 `D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md` 全文 …` —— **绝对路径**
7. `D:\Blockdustry\任务\T38_核心数据库.md` L12：`- 写: `D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md`（主文档）` —— **绝对路径**；L15：`阶段产出: … + 主文档 `docs/核心数据库.md`` —— **相对路径**（该文件 L13/L15 还引用 `docs/子agent/^T38_核心数据库.md`、`^T38_A/B/C` —— **批次 1 已断，见 B 组**）
8. `D:\Blockdustry\任务\T52_待办路线图研究.md` L8：`只读: `D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md`（迁移进度基线：72/205）` —— **绝对路径**
9. `D:\Blockdustry\仓库\docs\产出\T52_待办路线图研究.md` L17：`真值基线：`D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md`（555 行）` —— **绝对路径**；另 L68/L75/L76 有 `docs/核心数据库.md` **相对路径** 3 处
10. `D:\Blockdustry\仓库\docs\产出\P1_批1C_pyratiteMixer整合清单.md` L97：`## 6. 核心数据库登记（供主会话更新 docs/核心数据库.md）喵` —— **相对路径**
11. `D:\Blockdustry\仓库\docs\产出\P1_批1E_solarPanel整合清单.md` L24：`> 主会话整合时把下列两行在 `docs/核心数据库.md` 从「未迁移」移到「已迁移」并更新计数：` —— **相对路径**
12. `D:\Blockdustry\仓库\docs\产出\P1_批1F_copperScrapWall整合清单.md` L18：`> 主会话整合时请把以下 4 项在 `docs/核心数据库.md` 4.6 墙体节由「未迁移」移到「已迁移」…` —— **相对路径**
13. `D:\Blockdustry\仓库\docs\坑\EXP-RELOCATE_搬迁引用同步实验全过程_坑.md` L89：`- 核心数据库 `D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md` 第 580~590 行新增 11 条记录…` —— **绝对路径**；L104：`` - `核心数据库.md` 第 580~590 行的 11 条实验记录未回滚；本次未执行回滚。 `` —— **裸文件名**
14. `D:\Blockdustry\仓库\docs\产出\L1\T53_docs规范化整理_docs-规范化整理-dryRun预演-逐条搬迁表_L1.md` L311：`- **实测引用面**：`RD\核心数据库\派生态索引.md` L5、`任务\核心数据库维护.md` L3/L9/L15、`任务\T38_核心数据库.md` L12、`任务\T52_待办路线图研究.md` L8、`AG\^T52_待办路线图研究.md` L17、`RD\坑\EXP-RELOCATE_..._坑.md` L89、正文自身 L554 = **8 文件 / 9 处**` —— **短别名（`RD\`/`AG\`）**，本档即「8 文件 9 处」的来源
15. 本次任务自身盘点/预演档 5 篇（逐条见结论 A 组第 3 点）
16. `D:\Blockdustry\任务\organize-docs-blockdustry.md` L13/L32/L43/L44、`organize-docs-blockdustry-交接卡.md` L26/L50、`organize-docs-blockdustry-落地.md` L59/L60、`T53_docs规范化整理.md` L6/L10/L16/L17/L22、`D:\Blockdustry\[Agent进度]\organize-docs-blockdustry.md` L6/L7 —— 均为**英文短别名或相对/绝对路径**（原文见对应行）
17. `D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md` L9：`> - 计划依据：`docs/子agent/^P1_大规模迁移计划.md``；L65：`…（见 `docs/子agent/^P1_中文名修正.md`）…`；L234：`> 批次速查（来自 `^P1_大规模迁移计划.md`）喵：…` —— **相对路径 + 裸文件名**（指向**已改名**的 P1 档 → 断）
18. `D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md` L292：`> 来源：子 agent B（`docs/子agent/^T38_B_电力防御炮塔.md`）喵。…`；L550-L552：三个裸名 `^T38_A/_B/_C_*.md` —— **相对路径 + 裸文件名**（批次 1 已去 `^`）
19. `D:\Blockdustry\仓库\docs\产出\T28_雷光光效研究.md`、`仓库\docs\子agent\README.md` L3（`命名 {编号}_{任务名}.md`，`^` 语义已废）—— **未验证是否含旧路径**，抽查未见
20. 计数说明：以上 4.2 的「正文路径字符串 20 文件 / 27 处」= 编号 1-13 + 15(5 篇) + 17/18 归并后去重。**未验证**项：`仓库\docs\子agent\T28_雷光光效研究.md` 未逐行读。

**A2 front-matter `relatedFiles` / 指针字段（4 篇 / 4 处）**
1. `D:\Blockdustry\仓库\docs\坑\EXP-RELOCATE_搬迁引用同步实验全过程_坑.md` L6：`relatedFiles: [...,"D:\\Blockdustry\\仓库\\docs\\核心数据库\\核心数据库.md",...]` —— **绝对路径**（另含 2 条已失效的路径碎片：`"D:\\\\...\`"`、`"D:\\...\`"`，及 `D:\Blockdustry\仓库\docs\子agent\L2\EXP-RELOCATE-指针测试_搬迁指针测试乙_L2.md` 指向 `搬入\` 的旧位置）
2. `D:\Blockdustry\仓库\docs\产出\T52_待办路线图研究.md` L6：`relatedFiles: [...,"D:\\Blockdustry\\仓库\\docs\\核心数据库\\核心数据库.md","D:\\Blockdustry\\仓库\\docs\\产出\\^P1_大规模迁移计划.md","...\\^P1_批1A_审查报告.md","...\\^T38_A|B|C_*.md","...\\^T26_dagger3D模型.md",...]` —— **绝对路径**，其中 **6 条因批次 1 去 `^` 已断**（只此 1 行）
3. `D:\Blockdustry\仓库\docs\产出\L1\organize-docs-blockdustry_docs整理dryRun预演_L1.md` L6：`relatedFiles: [...,"D:\\Blockdustry\\仓库\\docs\\核心数据库\\核心数据库.md",...]` —— **绝对路径**（另含 `核心数据库.md`、`T16_上下坡带.md`、`T52_待办路线图研究.md` 等**裸文件名**）
4. `D:\Blockdustry\仓库\docs\产出\L2\organize-docs-blockdustry_docs整理dryRun预演_L2.md` L6：同上 —— **绝对路径**（另含 `T16_上下坡带.md`、`T17_上下坡带模型.md`、`派生态索引.md` 等裸名）
> 全树 `relatedFiles:` 共 **137 命中**，其中 **131 命中为 `relatedFiles: null`**（head 由工具补齐的 legacy 档），仅上述 4 篇 + `L1\...盘点-详细归纳_L1.md` L6 / `L2\...盘点-扩充细节_L2.md` L6 / `L1\...盘点-逐条清单_L1.md` L6 / `L3\...盘点-高度概括_L3.md` L6 / `L1|L2|L3\T53_*` L6 / `L3\T99_*` L6 / `L2\T99_*` L6 / `L1\T99_*` L6 有非空数组（共 14 篇非空）。
> **计数修正：摘要说「relatedFiles 9 篇」，实测非空 14 篇**（其中含 `核心数据库.md` 的 8 篇：`EXP-RELOCATE_坑`、`T52`、`L1|L2\organize-docs-blockdustry_docs整理dryRun预演`、`L1|L2\organize-docs-blockdustry_docs整理前现状盘点`、`L1|L2|L3\T53_*`）。

### B 组逐条（批次 1 改名已断的活引用，抽查 12 文件）
（格式：`路径:行号 → 该行原文`）
1. `D:\Blockdustry\仓库\docs\产出\T18_提升机顶部直连传送带.md` L16 → `> 用户后续反馈两个新问题，已修复，见 ``docs/子agent/^T19_提升机顶格Y1输出与底面贴图.md``：`
2. `D:\Blockdustry\仓库\docs\产出\T20_材料迁移与物品源.md` L39 → `…埃里克尔（Erekir）6 材料（…）**不迁移**，见 ``^T22_材料调整.md`` 喵。`；L83 → `…（详见 ``^T22_材料调整.md``）喵。`
3. `D:\Blockdustry\仓库\docs\产出\T22_材料调整.md` L15 → `> 相关：``^T20_材料迁移与物品源.md``（基底）、``docs/坑/贴图.md``（命名）、``docs/坑/物流交接.md``（dumpItem）喵。`
4. `D:\Blockdustry\仓库\docs\产出\T23_科技树UI精确复刻.md` L56 → `- 占用文件（写）：…、docs/子agent/^T23_科技树UI精确复刻.md、任务登记 T23_科技树UI精确复刻.md 喵。`（**自引旧名**）
5. `D:\Blockdustry\仓库\docs\产出\T22_科技树解锁指令.md` L35 → `- 占用文件（写）：…、docs/子agent/^T22_科技树解锁指令.md、…`（**自引旧名**）
6. `D:\Blockdustry\仓库\docs\产出\T19_删除爬坡回滚.md` L69 → `| ``docs/子agent/T16_上下坡带.md`` / ``T17_上下坡带模型.md`` | 标注「已废弃」喵 |`（两篇**已归档到** `docs\archive\2026-10\^T16|^T17_*.md`）L70 自引 `docs/子agent/T19_删除爬坡回滚.md`（裸名，仍**有效**）
7. `D:\Blockdustry\仓库\docs\坑\物流交接.md` L16 → `- 注意：传送带**坡道（UP/DOWN）已被并行任务 ``^T19_删除爬坡回滚.md`` 移除**，本坑不再适用坡道分支喵。`；L37 → `- ``docs/子agent/T14_立体物流初步.md``（设计）、``T16_提升机底部交接.md``（兜底引入）、``T18_提升机顶部直连传送带.md``（conveyor 正下方源）、``T19_提升机顶格Y1输出与底面贴图.md``（dumpItemTop 层高修复）喵。`
8. `D:\Blockdustry\仓库\docs\坑\API签名与编译.md` L14/L32 → 引用 `docs/子agent/T16_科技树深入实现.md`、`docs/子agent/T18_科技树实现A.md`（裸名的 `^` 缺失，**但批次 1 去 `^` 后这两个路径反而变正确了** —— 属「负向断引用」）
9. `D:\Blockdustry\仓库\docs\坑\炮塔黑色阴影.md` L59 → `结论：…文档 ``研究-炮塔动画.md`` 注意事项 7 说的…`；L214 → `` - `docs/研究-炮塔动画.md`（注意事项 7：必须用 entityCutout）喵 ``；L162/L215/L216 → `研究-机器侧面贴图.md`、`docs/研究-PowerNode激光黑色.md`（**这两个目标档在 `研究\` 内不存在**）
10. `D:\Blockdustry\仓库\docs\坑\炮塔黑色阴影2.md` L85/L92/L136/L161/L162/L163 → `docs/研究-PowerNode激光黑色.md`、`docs/研究-炮塔黑色阴影.md`、`docs/研究-机器侧面贴图.md`（**目标缺失**）
11. `D:\Blockdustry\仓库\docs\修改\钻头侧面贴图应用.md` L50 → `- ``docs/研究-渲染与模型坑.md``（§1 BER 全亮、§2 坐标上限、§6 贴图比例）`；L51 → `- ``docs/研究-炮管黑.md``、``docs/研究-PowerNode激光黑色.md```
12. `D:\Blockdustry\仓库\docs\子agent\` 内 `T3_灵魂出窍.md` L238、`T14_火焰炮电弧.md` L105、`T8c_核心分裂黑箱修复.md` L27/L74、`T9a_贴图镜像与核心碰撞.md` L73/L85、`T12_核心背面消失.md` L61/L71、`T13_核心余光剔除.md` L58、`T6_矿机叶片.md` L60/L69/L100、`T4b_分裂炮贴图尺寸.md` L33 → 全部引用 `docs/研究-渲染与模型坑.md` / `研究-炮管黑.md` / `研究-炮塔动画.md` / `docs\^研究-Jade联动.md` 旧名。
> 目标映射（依据 `仓库\docs\核心数据库.md` changelog L707-L726）：`研究-渲染与模型坑.md` → `仓库\docs\坑\渲染与模型坑索引.md`；`^研究-炮塔动画.md` → `仓库\docs\研究\炮塔动画.md`；`^研究-Jade联动.md` → `仓库\docs\研究\Jade联动.md`；`^研究-炮管黑.md` → **归档为 `仓库\docs\坑\炮管黑.md`**（L1 预演 L319 记）；`^研究-机器侧面贴图.md`、`^研究-炮塔黑色阴影.md`、`^研究-PowerNode激光黑色.md` → **无对应现存文件，属真断引用**（与 L1 预演 L323 重名告警一致）。

### C 组（历史行，不参与本次修正）
- `D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md` L564-L599（10-02 的 `[命名规范化]` 4 条 + 10-03 13:05 的 `[馆员搬移]`/`[归档]`/`[引用更新]` 事故痕迹）、L610-L782（批次 1 全部 `[馆员搬移]/[引用更新]` 记录）、L783-L816（34 json 的引用更新）。
- 现执行 `sweep` 后 L727-L748、L783-L816 的 `[引用更新] → 指向 …` 目标虽正确，但**被指向的源文件（L1/L2 盘点档）实测仍是旧名**（抽查第一手 grep 确认），即「工具报告已更新、文件实际未改」的不一致，应记入验收项。

## 风险与待确认

1. **前提过期风险（高）**：交接卡与上级口述均以「预演未落地」为前提，实测批次 1 已落盘且**落盘时未同步改引用**；若后续按交接卡「批次 1 = 补引用」执行，会出现二次改动/重复计数。**建议上级先确认批次 1 是否已由 librarian 自动改过引用（抽查结论是「大部分没改」）**。
2. **计数不可叠加**：摘要的「8 文件 9 处」仅覆盖 `核心数据库.md` 的 external 引用，**不含**本次任务自身 5 篇盘点档、不含任务卡进度档、不含 changelog；「34 处引用」只覆盖 34 个 json 的**登记行**，不含实际 lang 片段被并入时的引用。**未验证**：`docs\_临时\` 下 34 个 json 是否仍被任何 `.json`/脚本内字面量引用（本轮只扫 `*.md`）。
3. **派生物不可手改（中）**：`仓库\docs\核心数据库\派生态索引.md` L5 + L19-L124 的 106 条旧路径，以及 `仓库\docs\坑\README.md`、`仓库\docs\子agent\README.md` 的索引区块，都应交给 `librarian_sweep`/`ledger_rebuild`；手改会被覆盖回。
4. **派生态索引自身位置冲突（中）**：`派生态索引.md` 现在位于 `docs\核心数据库\`（已是 `核心数据库.md` 的目标目录），`核心数据库.md` 搬进来后会与它同目录；`派生态索引.md` L5 的「真身」指针同时也要改。
5. **`待办.md` 双份漂移（中）**：`D:\Blockdustry\待办.md`（72/205）与 `D:\Blockdustry\仓库\待办.md`（78/205）是两份不同内容的副本，各自都有 2~3 处 `核心数据库.md` 引用；改哪一份、是否合并，**未验证**。
6. **架构/内容性风险（低）**：`核心数据库.md` L554 是**文档自述元数据**，改它就是改正文（用户已破例允许）；其余正文章节（如 L292、L550-L552 的行号锚点）**不能动行号**，只能改路径字面量，否则 L1 台账里大量「核心数据库 L217/L247/L299/L509/L510/L513/L520-L523」行号引用会集体失效（引用方见 `仓库\docs\子agent\T52_待办路线图研究.md` L68-L146）。
7. **未验证**：`仓库\docs\子agent\T28_雷光光效研究.md`、`仓库\docs\子agent\P2_dsh多智能体契约插件设计v1.md`、`仓库\docs\子agent\README.md` 未逐行读；`仓库\docs\研究\` 16 篇正文未逐行读（只抽样）；`仓库\src\` 按要求排除。
8. **口径差异**：`[Agent进度]\organize-docs-blockdustry.md` L7 自称真值「22 文件/31 行 + relatedFiles 9 篇 + 任务卡 7 篇」，与本轮实测「22 文件/30 处 + relatedFiles 8 篇（含核心数据库的） + 任务卡/进度档若干」**接近但不完全一致**，差异来自是否把 `待办.md` 裸名行与本次盘点档计入。

## 下一步

1. **请上级先裁定两个前提**：(a) 批次 1 是否允许重开「补引用」子批；(b) 计数以本档的「活引用」为准还是以 `[Agent进度]` L7 的 22/31 为准。
2. **批次 2（`核心数据库.md` 归位）执行清单**：搬 `仓库\docs\核心数据库.md` → `仓库\docs\核心数据库\核心数据库.md`，同批改 A 组 22 文件 / 30 处（含 L554 破例改一行）；**不要**改 C 组。
3. **批次 3（批次 1 改名补引用）执行清单**：按 changelog L610-L726 的映射，改 B 组活引用；对「目标档不存在」的 6 个旧名（`研究-机器侧面贴图.md`、`研究-炮塔黑色阴影.md`、`研究-PowerNode激光黑色.md`、`研究-炮管黑.md`、`研究-炮塔动画.md`、`研究-渲染与模型坑.md`）**只上报、不猜着改**。
4. **收尾**：`librarian_sweep` → `ledger_rebuild` 重建 `派生态索引.md`、`坑\README.md`、`子agent\README.md`，再复跑本档的分类 grep 做验收（期望：A/B 两组归零，C 组保留）。
5. **建议交付验收脚本口径**：`grep -n '\^\(P1\|T[0-9]\)'`（旧名残留）+ `grep -rn 'docs/核心数据库\.md'`（搬迁残留）+ `relatedFiles` 非空篇的路径存在性校验，三者分别给数。
