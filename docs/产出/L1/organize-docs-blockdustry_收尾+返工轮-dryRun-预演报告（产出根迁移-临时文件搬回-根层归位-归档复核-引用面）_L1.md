---
taskId: organize-docs-blockdustry
role: librarian
tier: 1
keywords: ["收尾+返工轮-dryRun-预演报告（产出根迁移-临时文件搬回-根层归位-归档复核-引用面）","收尾返工轮","dryRun 预演","产出档根迁移","子agent 改名为 产出","产出 L1 L2 L3","临时文件搬回","仓库/docs/_临时","docs/_临时 错落点","FIX-88","FIX-98","tempDir 同源","核心数据库.md 归位","全量逐条清单 归位","归档复核","archive 2026-08","archive 2026-10","影响引用","断链接","工具回执偏窄","layoutVersion 3","判不准单列","organize-docs-blockdustry","blockdustry","子agent","核心","数据","据库","心数","临时","agent","l1","l2","l3","2026","归档","根层","引用","冲突"]
relatedFiles: ["D:\\Blockdustry\\仓库\\docs\\产出","D:\\Blockdustry\\仓库\\docs\\_临时","D:\\Blockdustry\\仓库\\docs\\核心数据库\\核心数据库.md","D:\\Blockdustry\\仓库\\docs\\核心数据库\\派生态索引.md","D:\\Blockdustry\\仓库\\docs\\产出\\L1\\organize-docs-blockdustry_docs整理前现状_全量逐条清单.md","D:\\Blockdustry\\仓库\\docs\\archive\\2026-08\\传送带上下坡.md","D:\\Blockdustry\\仓库\\docs\\archive\\2026-10\\T16_上下坡带.md","D:\\Blockdustry\\仓库\\docs\\archive\\2026-10\\T17_上下坡带模型.md","D:\\Blockdustry\\.agent-contract\\project.json","D:\\Blockdustry\\.agent-contract\\panel.json","D:\\DSH插件\\dsh-agent-contract\\src\\project\\adaptive.js","D:\\DSH插件\\dsh-agent-contract\\package.json","D:\\Blockdustry\\仓库\\README.md","D:\\Blockdustry\\待办.md","D:\\Blockdustry\\仓库\\待办.md","D:\\Blockdustry\\任务\\organize-docs-blockdustry-交接卡.md","D:\\Blockdustry\\任务\\organize-docs-blockdustry-落地.md","D:\\Blockdustry\\仓库\\docs\\研究\\organize-docs-blockdustry-refs_归位后引用面权威普查（核心数据库搬迁-+-批次1改名）_研究.md","D:\\DSH插件\\dsh-agent-contract","README.md","T28_雷光光效研究.md","P1_中文名修正.md","P1_大规模迁移计划.md","P1_批1A_A1整合清单.md","P1_批1A_A2整合清单.md","P1_批1A_A3整合清单.md","P1_批1A_审查报告.md","D:\\Blockdustry\\仓库\\docs\\产出\\P1_批1B_container整合清单.md","D:\\Blockdustry\\仓库\\docs\\产出\\P1_批1B_itemBridge整合清单.md"]
createdAt: 2026-10-03T16:24:57.637Z
librarianTouchedAt: 2026-10-04T06:16:24.832Z
librarianChanges: ["馆员搬移","引用更新（搬移）","复核 keywords（新增 0 个）","引用修复（指向现存档）","复核 keywords（新增 0 个）","引用修复（指向现存档）","复核 keywords（新增 0 个）","复核 keywords（新增 0 个）","复核 keywords（新增 0 个）","引用修复（指向现存档）","复核 keywords（新增 0 个）"]
detailLevel: full
---

> **速览** 本项目：Blockdustry
> **速览** 本档（L1）给：长期留档与全量核查
> **速览** 本档即最详细的一档（终点，没有下钻指针）
## 结论

**五份预演全部 `dryRun`，零落盘；本档是唯一新增文件（新生的档也需随批迁移，见 §1.4）。**

| # | 预演 | 回执 | 冲突 | 工具报影响引用 |
|---|---|---|---|---|
| ① | 产出档根迁移（根层 97 + `L1\|L2\|L3` 14） | **将移动 111 项** | 0 | 32 处 |
| ①b | 判不准单列 3 篇（`README.md` / `P2_…v1.md` / `T28_雷光光效研究.md`） | 将移动 3 项 | 0 | 2 处 |
| ② | 临时文件搬回（34 个 json） | **将移动 34 项** | 0 | 34 处 |
| ③ | 根层两篇归位（全量清单 + `核心数据库.md`） | 将移动 2 项 | 0 | 7 处 |
| ④ | 归档复核（3 篇已在位） | 将归档 **0** 篇 | 3（= 已在位，非真冲突） | — |
| | **合计** | **150 项待搬（其中 3 篇待裁）** | **0** | **75 处** |

- **①的实测与上级数一致**：`仓库\docs\子agent\` 根层 **100 个 .md**（28 × `P1_*` + 70 × `T*` + `README.md` + `P2_…`；`T28_…` 已计入 `T*`）、`L1/L2/L3` = **5/4/5**、非 .md **0 个**。
- **上一轮已到位的四类一律未重搬**：`研究\` 19、`坑\` 19、`修改\` 1、`核心数据库\` 1、`子agent\L1|L2|L3` 5/4/5、根层 100 篇已无 `^`；3 篇废弃档在 `archive\2026-08`(1) 与 `2026-10`(2)。
- **②的落点判据是源码级确定的**：0.24.0 下 `_临时` = **`D:\Blockdustry\仓库\docs\_临时`**；`D:\Blockdustry\docs\_临时` 是 **FIX-88 前的旧错值**（详见 §2）。
- **工具回执严重偏窄**：五份合计只报 75 处引用，人工扫到 **≥776 行**含 `子agent` 字面量（§5）。根改名造成的断链是**量级问题**，不是零星修补。

## 依据

### 1. 预演① 产出档根迁移 `仓库\docs\子agent\` → `仓库\docs\产出\`

**落点规则（0.24.0 源码）**：`D:\DSH插件\dsh-agent-contract\src\project\adaptive.js` L56 产出根探测候选 = `['docs/产出','仓库/docs/产出','docs/子agent','仓库/docs/子agent','子agent','deliverables']`；L73 `DEFAULT_STRUCTURE.deliverablesDir = 'docs/产出'`；L268-272 对 `DOC_STRUCTURE_KEYS`（含 `deliverablesDir`）按**文档类目录公共父目录**解析 ⇒ 本项目即 `仓库\docs\产出`，两级 `产出\L1|L2|L3`。

**1.1 根层 97 篇 → `产出\` 同名（原名不动）**
`P1_中文名修正.md`、`P1_大规模迁移计划.md`、`P1_批1A_A1整合清单.md`、`P1_批1A_A2整合清单.md`、`P1_批1A_A3整合清单.md`、`P1_批1A_审查报告.md`、`D:\Blockdustry\仓库\docs\产出\P1_批1B_container整合清单.md`、`D:\Blockdustry\仓库\docs\产出\P1_批1B_itemBridge整合清单.md`、`P1_批1CD_审查报告.md`、`P1_批1C_kiln整合清单.md`、`P1_批1C_phaseWeaver整合清单.md`、`P1_批1C_plastaniumCompressor整合清单.md`、`P1_批1C_pulverizerIncinerator整合清单.md`、`P1_批1C_pyratiteMixer整合清单.md`、`P1_批1C_siliconSmelter整合清单.md`、`P1_批1D_blastDrill整合清单.md`、`P1_批1D_laserDrill整合清单.md`、`P1_批1D_pneumaticDrill整合清单.md`、`P1_批1E_diodeSurgeTower光效研究.md`、`P1_批1E_diodeSurgeTower整合清单.md`、`P1_批1E_powerNodeLargeBatteryLarge整合清单.md`、`P1_批1E_solarPanel整合清单.md`、`P1_批1F_copperScrapWall整合清单.md`、`P1_批1F_door开关动画研究.md`、`P1_批1F_titaniumWallDoor整合清单.md`、`P1_批2A_高级墙体整合清单.md`、`P1_批2B_menderForceProjector整合清单.md`、`P1_雷光特效_整合清单.md`、`T10_blockhealth深度联系.md`、`T11_传送带贴图.md`、`T11_炮台贴图.md`、`T12_传送带转角颠倒.md`、`T12_核心背面消失.md`、`T12_炮台侧面贴图.md`、`T12_附身俯仰.md`、`T13_传送带侧面.md`、`T13_核心余光剔除.md`、`T13_灵魂出窍应用.md`、`T13_炮台侧面重绘与命名.md`、`T13_附身发射点红线.md`、`T14_火焰炮电弧.md`、`T14_电力整合.md`、`T14_科技树研究.md`、`T14_立体物流初步.md`、`T15_Mindustry光效研究.md`、`T15_提升机异常.md`、`T15_灵魂出窍误触发.md`、`T15_电弧基座.md`、`T16_提升机底部交接.md`、`T16_灵魂出窍双击深查.md`、`T16_电力节点倒角.md`、`T16_科技树深入实现.md`、`T17_提升机生效确认.md`、`T17_灵魂出窍C键.md`、`T17_电力节点倒角重做.md`、`T18_创造栏分类.md`、`T18_提升机顶部直连传送带.md`、`T18_科技树实现A.md`、`T18_科技树实现B.md`、`T19_删除爬坡回滚.md`、`T19_提升机顶格Y1输出与底面贴图.md`、`T1_传送带转角.md`、`T20_材料迁移与物品源.md`、`T20_科技树树形UI.md`、`T21_BlockHealth整组血量.md`、`T22_材料调整.md`、`T22_科技树解锁指令.md`、`T23_科技树UI精确复刻.md`、`T25_科技树彻底重做.md`、`T26_dagger3D模型.md`、`T2_上帝视角.md`、`T35_siliconSmelter冒烟特效研究.md`、`T36_kiln火焰特效研究.md`、`T38_A_物流生产液体.md`、`T38_B_电力防御炮塔.md`、`T38_C_单位逻辑物品.md`、`T3_灵魂出窍.md`、`T46_pulverizerIncinerator特效研究.md`、`T47_phaseWeaver织机特效研究.md`、`T49_维修光束力场特效研究.md`、`T4_分裂炮与标签.md`、`T4b_分裂炮贴图尺寸.md`、`T50_审查修复清单.md`、`T51_分裂炮台像素绘制研究.md`、`T52_待办路线图研究.md`、`T5_炮台附身.md`、`T6_矿机叶片.md`、`T6b_钻头侧面贴图.md`、`T6c_钻头侧面贴图应用.md`、`T6d_钻头真侧面贴图.md`、`T7_装甲机制.md`、`T8_核心正方体.md`、`T8b_核心贴图修复.md`、`T8c_核心分裂黑箱修复.md`、`T9a_贴图镜像与核心碰撞.md`、`T9b_附身黑屏退出修复.md`、`T9_三维物流.md`

**1.2 `L1|L2|L3` 14 篇 → `产出\L1|L2|L3\` 同名**
`L1\`：`D:\Blockdustry\仓库\docs\产出\L1\T53_docs规范化整理_docs-规范化整理-dryRun预演-逐条搬迁表_L1.md`、`organize-docs-blockdustry_docs整理前现状盘点-详细归纳_L1.md`、`organize-docs-blockdustry_docs整理dryRun预演_L1.md`、`T99_契约流程冒烟测试_T99-冒烟测试审查者通道回报-详细归纳_L1.md`、`organize-docs-blockdustry_docs整理前现状盘点-逐条清单_L1.md`（后者是误生成重复副本，**只上报不删**）
`L2\`：`T53_…-扩充细节_L2.md`、`organize-docs-blockdustry_docs整理dryRun预演_L2.md`、`T99_…-扩充细节_L2.md`、`organize-docs-blockdustry_docs整理前现状盘点-扩充细节_L2.md`
`L3\`：`T53_…-高度概括_L3.md`、`organize-docs-blockdustry_docs整理前现状盘点-高度概括_L3.md`、`organize-docs-blockdustry_docs整理dryRun预演_L3.md`、`T99_…研究员回报_L3.md`、`T99_…-高度概括_L3.md`

**1.3 单列待裁 3 篇（未硬塞 —— 分别给了判据）**

| 篇 | 我的判断 | 依据 | 建议动作 |
|---|---|---|---|
| `T28_雷光光效研究.md` | **属产出档，随迁** | `任务\T28_雷光光效研究.md` L29/L31 明写「写 `…\子agent\T28_雷光光效研究.md`」= 该任务的阶段产出 | 随 1.1 批迁 |
| `README.md` | **属目录说明档，随迁但正文会失指** | 它是 `子agent\` 的目录说明（L3「统一写入本目录（`D:\Blockdustry\仓库\docs\子agent`）」）；2024 命名规范把 `README.md` 记为违规（目录说明档例外） | 随目录迁；**正文 L3 的旧目录名只能上报，不动正文** |
| `P2_dsh多智能体契约插件设计v1.md` | **判不准** | `taskId: null`、`role: null`、`tier: 0`，不是任何任务的产出；内容是插件设计研究，按「类型 → 目录」更像 `研究\`（且需补 `_研究` 后缀）；但插件实现里它一直住在产出目录（`docs\FIX.md` 提到「产出目录里的设计稿」） | **单列，请上级裁定**：跟迁 `产出\` / 归 `研究\` |

**1.4 目标父目录不存在**：`仓库\docs\产出`（含 `L1|L2|L3`）与 `仓库\docs\_临时` 实测**都不存在**。预演未报冲突 ⇒ 推断工具会自建，但**未验证**；落盘时以第一项验尸。

### 2. 预演② 临时文件搬回：`_临时` 落点有定论

**结论：0.24.0 的实际解析 = `D:\Blockdustry\仓库\docs\_临时`。**

| 口径 | 路径 | 状态 |
|---|---|---|
| 当前插件（0.24.0）实际解析 | **`D:\Blockdustry\仓库\docs\_临时`** | 应为落点；**目录尚不存在** |
| 旧默认值（FIX-88 前，已修掉） | `D:\Blockdustry\docs\_临时` | **错值**；34 个 json 现在就在这儿 |

读到的配置（三处，互为印证）：
1. **插件源码**（`D:\DSH插件\dsh-agent-contract`，`package.json` `"version": "0.24.0"`）：`src\project\adaptive.js` L81 `DEFAULT_STRUCTURE.tempDir = 'docs/_临时'`，但 L268-272 + L322-353 的 `tempDirInfo()` 把它改成**文档类目录公共父目录 + `_临时`**；`scripts\verify.mjs` L6776/L7410 断言「类别在 `<root>/仓库/docs/*` ⇒ tempDir = `<root>/仓库/docs/_临时`」，L7438 只在「算不出公共父目录」时才退回 `<root>/docs/_临时`。`docs\FIX.md` L1783-1800（FIX-88）、L2081-2098（FIX-98）逐字记录了这次改动，并点名 `D:\Blockdustry\docs\_临时` 就是真机上被误建的错值。
2. **运行期派生物** `.agent-contract\project.json` L24 / L48：`"tempDir": "D:\\Blockdustry\\仓库\\docs\\_临时"`（`layoutVersion: 3`，`generatedAt 2026-10-03T16:16:11Z`）。
3. **版本快照** `.agent-contract\panel.json` L4-L5：`pluginVersion / runtimeVersion = 0.24.0`（`generatedAt 16:20:02Z`，晚于 project.json ⇒ 该 tempDir 值出自 0.24.0 的运行）。

已实测现场：`D:\Blockdustry\docs\_临时\` 恰为 **34 个 json**（17 组 × {en,zh}），文件名**已去 `^`**、内容仍为 lang 片段；`D:\Blockdustry\仓库\docs\_临时\` **不存在**。预演回执：**将移动 34 项 / 冲突 0 / 工具报影响引用 34 处**（人工实查 167 行含 `_临时` 字面量，其中 `核心数据库.md` changelog L749-L816 占 68 行）。

### 3. 预演③ 根层两篇归位（2 项 / 冲突 0 / 工具报 7 处）

| src | dst | 判定 |
|---|---|---|
| `仓库\docs\核心数据库.md` | `仓库\docs\核心数据库\核心数据库.md` | **用户已拍板**，照做。无 front-matter、真值 816 行；搬后正文 L554 自述路径成断链（§5） |
| `仓库\docs\organize-docs-blockdustry_docs整理前现状_全量逐条清单.md` | `仓库\docs\产出\L1\organize-docs-blockdustry_docs整理前现状_全量逐条清单.md`（**我的建议**） | **判不准 → 单列**。依据：它是本任务研究员的 L1 级附件明细（无 front-matter，供 `L1\…盘点-详细归纳_L1.md` 引用），与已在 `产出\L1\` 的同族档同属一层；**备选**是 `研究\`（本任务另一篇研究员档 `…refs_…_研究.md` 就落在 `研究\`）。审计只给到「由馆员手工移到对应类目录」（`doc_misfiled`），没指明哪个 |

### 4. 预演④ 归档复核（只报不动）

| 档（现位置） | 分片月 | 正文标废日 / 头部字段 | 一致？ |
|---|---|---|---|
| `archive\2026-08\传送带上下坡.md` | 2026-08 | 正文 L16「已废弃（**2026-08-13**）」；`archivedAt: 2026-08-13T00:00:00.000Z` | ✅ 一致 |
| `archive\2026-10\T16_上下坡带.md` | 2026-10 | 正文 L16「已废弃（T19 回滚）」；`archivedAt: 2026-10-03T13:05:54.446Z` | ✅ 一致（判废月 = 10 月） |
| `archive\2026-10\T17_上下坡带模型.md` | 2026-10 | 同上（`…54.567Z`） | ✅ 一致 |

三篇头部均已有 `archived: true` + `librarianTouchedAt: 2026-10-03T15:28:23.027Z` + `librarianChanges: ["归档到 <月> 分片"]`。用**现位置**再喂给 `librarian_archive --dryRun` 得到「将归档 0 篇 / 冲突 3 项（目标已存在，拒收）」——**这 3 项是「已在位」的证据，不是真冲突**；若要复核归档能力，应拿**原源路径**喂（源已不存在）。

### 5. 预演⑤ 影响引用面（工具回执 vs 人工实查）

**工具回执（合计 75 处）**：① 111 项→32（仅 29 篇被点到，最多的一篇 5 篇提到）｜①b 3 项→2（`T28_雷光光效研究.md` 被 2 篇提到）｜② 34 项→34（每项「被 1 篇提到」= `核心数据库.md` changelog 登记行）｜③ 2 项→7（`核心数据库.md` 5 篇 + 根层清单 2 篇）｜④ 0。
**人工扫（域：`仓库\`、`任务\`、`[Agent进度]\`、`待办.md`；排除 `.agent-contract\backups\` 与 `仓库\src\`）**：

| 断链源 | 命中量级（行） | 说明 |
|---|---|---|
| `子agent` 字面量（根改名 → 全断） | **≥776**：`仓库\docs` **619**、`任务` **149**、`[Agent进度]` 2、`待办.md` 3、`仓库\待办.md` 3 | 其中 `核心数据库.md` changelog L564-L816（**历史行，政策上不改**）约占 129 行；本任务的盘点/预演档大段登记旧路径约占 400 行；其余为任务卡资源声明与整合清单正文 |
| `_临时` 字面量（搬回 → 全断） | ≥167：`仓库\docs` 157、`任务` 10 | changelog L749-L816 占 68 行 |
| `核心数据库.md` 字面量 | ≥102 行（**活引用真值 = 22 文件 / 30 处**，据 `研究\…refs_…权威普查…_研究.md`） | 含正文自身 L554、`派生态索引.md` L5、`仓库\README.md` L62、`待办.md` L7/L63/L67 与 `仓库\待办.md` L7/L62/L66、三篇 P1 整合清单、`任务\核心数据库维护.md` L3/L9/L15、`任务\T38_核心数据库.md` L12/L15、`任务\T52_待办路线图研究.md` L8、`子agent\T52_待办路线图研究.md` L17 |
| 根层清单被引 | 11 处 | `交接卡` L36/L59、`研究\…refs` L25/L38、`L1\…详细归纳_L1.md` L6/L16、`L2\…扩充细节_L2.md` L6/L64、`L1\…dryRun预演_L1.md` L6（relatedFiles） |

**必覆盖锚点复核结果**（全部成立）：`核心数据库.md` 正文 **L554**（`- 主文档命名固定 \`D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md\`，长期维护喵。`）｜`核心数据库\派生态索引.md` **L5**（`> 真身（唯一权威，勿删）：D:\Blockdustry\仓库\docs\核心数据库\核心数据库.md`，派生物，靠 `sweep`/`ledger_rebuild` 重建）｜`仓库\README.md` **L62**（`详见 [核心数据库](./docs/核心数据库.md)`）｜`待办.md` L7/L63/L67 与 `仓库\待办.md` L7/L62/L66｜`^P1` 系整合清单（`pyratiteMixer` L97、`solarPanel` L24、`copperScrapWall` L18 等）｜`任务\核心数据库维护.md` L3/L9/L15/L20、`任务\T38_核心数据库.md` L12-L15、`任务\T52_待办路线图研究.md` L8｜**本任务自身**：根层清单（L45/L48/L49/L216/L222/L229）+ `子agent\L1|L2|L3\` 五篇盘点/预演档（含 `L1\…dryRun预演_L1.md` L6 `relatedFiles` 28 条、L20/L142）。
**代表样例（其余同型）**：`任务\README.md` L12/L23/L31（「产出须写入 `D:\Blockdustry\仓库\docs\子agent`」= 规范源文档，**必须改**）｜`任务\organize-docs-blockdustry-交接卡.md` L34/L35/L47/L48、`落地.md` L4/L23/L24/L30-L34/L54（上一轮范围，**含已作废的 `D:\Blockdustry\docs\_临时` 指标**）｜`子agent\T52_待办路线图研究.md` L6 一行 6 条断链｜`坑\API签名与编译.md` L14/L32、`坑\物流交接.md` L16/L37。

## 风险与待确认

1. **工具引用口径不可信（高）**：75 vs ≥776 —— `countReferences` 只认**逐字完整绝对路径字面量**且只遍历台账内档；`仓库\docs\研究|坑|修改|核心数据库` 四类**不在台账**（见 `坑\EXP-RELOCATE_…_坑.md` L41 源码判读）。**结论：引用修补不能只靠工具回执，必须按本档 §5 的域手工补扫。**
2. **`project.json` 与规范不符是「预期内的滞后」，不是配置错**：它现在仍写 `deliverablesDir = 仓库\docs\子agent`、`docKinds.产出档 = 同上`，是因为 `产出\` 尚不存在（磁盘事实）；`产出\` 一旦建好，0.24.0 会**自动重探并写回**（FIX-83/FIX-92/FIX-98，`verify.mjs` L6560 断言「迁移后会自动跟上」）。⇒ **落盘顺序应是「先建 `产出\` 与 `_临时\`、再搬、再重启 `contract_status` 复核 project.json 已改指 `产出`」**。
3. **归档冲突口径**：预演④的「3 项冲突」若被误读为真冲突会导致重复归档；建议上层按「已在位」记账（本档已给证据）。
4. **`^` 与 `archived` 语义冲突仍在**：`^T16/^T17` 归档后仍带 `^`，而规范已把归档语义搬到 `archived: true`；三篇归档档正文里还有指向 `docs/子agent/…` 的旧路径（`^T16` L159、`^T17` L53），**归档档正文是否也纳入本轮引用修补，需上级裁定**。
5. **判不准单列 3+1 条**：`P2_…v1.md`（跟迁 vs 归 `研究\`）、根层全量清单（`产出\L1\` vs `研究\`）、`README.md`（随迁但正文旧目录名改不改）、`核心数据库.md` L554 正文破例改一行（用户已拍板「同批修 9 处」，但「正文零改动」纪律需上级书面豁免）。
6. **重复副本仍在**：`产出\L1\organize-docs-blockdustry_docs整理前现状盘点-逐条清单_L1.md`（研究员误生成）**只上报不删**，迁完会与根层清单在语义上撞车。

## 下一步

1. 上级裁 §1.3 的 3 篇 + §3 的根层清单落点 + §5 风险 5 的引用修补豁免，再给「落盘」令。
2. 落盘建议顺序（每步先 `dryRun` 再 `dryRun:false`）：① 建 `仓库\docs\产出\{L1,L2,L3}` 与 `仓库\docs\_临时` → ② 跑 §1.1+§1.2 根迁移 → ③ 跑 §2 json 搬回 → ④ 跑 §3 两篇归位 → ⑤ `librarian_sweep` 落索引与台账 → ⑥ `ledger_rebuild` → ⑦ 重启 `contract_status` 复核 `project.json` 已改指 `产出` / `_临时`。
3. 引用修补单开一批：按 §5 表格，**只改导引类/资源声明与 front-matter `relatedFiles`，changelog 历史行（`核心数据库.md` L564-L816）一字不动**。
4. 复核口令：`grep '子agent'`、`grep '_临时'`、`grep 'docs/核心数据库\.md'` 三者应分别收敛到「历史行 + 归档档」残余量。
