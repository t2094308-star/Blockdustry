---
tier: 0
keywords: ["角色","审查","审计","配置","契约","agent"]
relatedFiles: ["D:\\Blockdustry","cordis.patch.yml"]
createdAt: 2026-10-02T10:46:45.155Z
librarianTouchedAt: 2026-10-03T16:27:39.271Z
librarianChanges: ["补 tier","补 createdAt","补 keywords（角色、审查、审计、配置、契约、agent）","补 relatedFiles（2 条）","馆员搬移"]
---

# P2 dsh 多智能体契约插件 · 设计 v2

> 目标：把「提示词约束工作流」（源规范 = Blockdustry 的 `[1] 总纲`）迁移到 dsh 并**机制化**：
> 契约注入 + 三档文档 + 任务板/台账 + 审计（警告制）+ 自研面板。
> **定位：通用契约工作流引擎**——项目根、目录约定、角色、模型路由、预算、总纲模板全部可配；Blockdustry 只是第一个实例。
> 状态：设计稿（v2），未开始编码。日期：2026-10-02。
> 变更：v2 定稿面板方案（自注册独立 tab）、auto-review 保留、新增「通用化与项目配置」层。

## 0. 关键决策（已定）

1. **底座 = subagent 机制**（`dsh-subagent` 服务 + `tool-subagent` + `tool-subagent-fork` + `tool-subagent-control` + `list-agents`）
   - **移除** `@deepseek-ai/dsh-experimental-agent-team-profile`
   - 原因（实测）：Team 的 patch 会 `disabled: tool-subagent-control` → 续聊句柄断链；Team 版 `send_message(target)` 只认 roster 成员，对 subagent id 投递必失败；官方定位是"替换"而非"并存"
2. 协作层（名册 / 任务板 / 谱系树 / 审计 / 契约引擎）**全部自研**
3. 官方 team 相关内容（`spawn_teammate` / `team_task_*` / 官方成员面板）**一律不用**；审查/对抗审查成员**自研**
4. **面板 = 自注册独立 tab 覆盖**：用 `ctx.betterSidebar.registerTab` 服务，**不 fork、不改** better-sidebar 源码；与它的「任务管理」页并存，靠命名/图标区分，并关掉它的自动展开
5. 审计 = **警告制**；注入预算 = **软上限 + 硬上限**（超软限 30% 以内仅提醒——LLM 对字数不敏感）
6. `@deepseek-ai/dsh-experimental-auto-review` **保留**——它管**权限审批**（审查子智能体的命令风险），与我们的"审查成员"职责不重合
7. **通用化**：项目根 / 目录约定 / 角色卡 / 模型路由 / 预算 / 总纲模板 **全部可配**（见 §2.5）
8. 术语统一官方口径：**主代理 / 成员 / 子智能体**（弃用"子代理/子agent"）

## 1. 底座事实（2026-10-02 实测）

| 事实 | 内容 |
|---|---|
| 前台（one-shot） | 创建 → 跑一个 turn → 产出文本 → 结束 → **释放**；**完成后不可再派活** |
| 后台（continuable） | 返回 id，**可续派/唤醒**；结算通知 "…unless you send it more." |
| 模式开关 | `tool-subagent` 的 `backgroundMode: 'one-shot' \| 'continuable'`（插件级；**可挂多实例** = 每角色一条专属通道） |
| 相邻寻址 | `send_message` 只走**直接层级**；子→父要求自身处于常驻可续接态 |
| 谱系口径 | `parentSession` + subagent origin，任意深度；**fork 不入 lineage**（需单独标注） |
| 释放语义 | 宿主释放**连带后代**；Publication 后 caller must dispose |
| 深度预算 | profile 已配 `maxDepth: 3` / `maxActiveSubagents: 15`（实测 depth-3 真血缘 ✓） |
| 权限 | 子智能体与主智能体**平权**（工作区内读写、区外只读）→ "共享文件只归主代理写"只能=**软约束+审计** |
| 官方注入 | 子级 runtime-context 自带 `subagent:delegation` 权限声明（范围固定、不可自扩、被拒须回报） |

## 2. 角色与模式

| 角色 | 模式 | 模型路由 | 工具白名单 | 备注 |
|---|---|---|---|---|
| 主代理(Lead) | — | 主力 | 全部 | 唯一可写共享文件 |
| 研究者 | 前台(one-shot) | 主力/便宜档 | 只读 + 写 [位置] | 产出研究档 |
| 实现者 | 后台(continuable) | 主力 | 独立新文件 + 编译 | 便于审查退回 |
| 审查者（同级） | 后台 | 主力 | 只读 | 可被退回后复跑 |
| **对抗审查者** | 后台 | **异源**（GLM/Qwen，可选 GPT） | 只读 + 搜索 | 红队找茬；审计校验"异源" |
| 图书管理员 | 后台（闲置唤醒） | 便宜档 | 文档目录/索引/核心数据库 | 批次完成或审计报警时唤醒 |

## 2.5 通用化与项目配置（v2 新增）

插件 = **引擎**；项目 = **配置实例**。全部走插件设置 / `cordis.patch.yml` config，不硬编码任何项目路径：

```yaml
project:
  name: Blockdustry            # 实例名（可多个项目并列，按会话选定）
  root: D:\Blockdustry         # 工作区根（示例值，非默认值）
paths:
  tasksDir:       任务                      # 任务登记
  progressDir:    "[Agent进度]"             # 进度文件（≤400 字）
  deliverablesDir: 仓库/docs/产出        # [位置]（三档文档落盘）
  docsDirs:       [仓库/docs, 仓库/docs/坑] # 文档切片检索源
contract:
  mandateTemplate: <[1]总纲模板>            # 可替换为别的项目的总纲
roles:                                       # 可覆盖内置角色卡
  - id: researcher
    mode: one-shot                           # 前台/后台
    modelRoute: default
    tools: [read, search, write_deliverable]
    budgetChars: 800
  - id: adversary
    mode: continuable
    modelRoute: glm                          # 异源路由（GLM/Qwen/…）
budgets:
  softLimit: 800        # 每片段软上限（字符）
  hardLimit: 1200       # 硬上限
  warnOverSoftPct: 30   # 超软限 ≤30% 仅提醒
modelRoutes: {}          # 角色→模型路由映射；密钥走 dsh 凭据，不落配置
audit:
  checks: [missing_doc, over_budget, unfilled_slot, stale_progress, cross_vendor, ghost_run, orphan_task, unreleased_run]
```

**预留不写死**：项目根、对抗源、密钥、模型路由都留配置位；跑 Blockdustry 时填 `root: D:\Blockdustry` 即可。

## 3. 契约引擎（注入）

- **挂点**：委派工具（多实例，每角色一个）的 `description` + 子级首条消息
- **装配顺序**（接在官方 delegation-scope 声明之后）：
  1. `[1] 总纲 + 2』状态` **完整实例**（传递性要求）
  2. 角色卡（职责/边界/工具纪律；≤800 字）
  3. 任务切面（任务简报 + 项目简介 + debug 文件清单）
  4. 文档切片（从文档注册表检索：相关坑/研究/接口约束）
  5. 状态槽自动填充：`[Agent进度]` `[任务]` `[位置]` `[上级]` `[层数]` `[总Agent数]`
  6. 能力配置（工具白名单 / effort / maxTurns / 模式）
- **预算**：每片段软上限 + 硬上限；**超软限 ≤30% 仅提醒**，超硬限才拦/裁剪
- **纪律**：不破坏 prompt cache（子级都是 fresh 上下文，天然安全）

## 4. 产出契约（工具）

- `doc_emit(level=3|2|1)`：三级 250~300 字（直发上级）→ 二级（发上级的上级）→ 一级（存 [位置]，≤3000 字）
- `progress_upsert`：`[Agent进度]` 目录，**≤400 字硬校验**
- `bugfix_note`：严重 Bug 修复总结 → [位置]
- **铁律：前台(one-shot) run 必须先落盘后返回**（否则随 run 蒸发）→ 审计对"幽灵 run"（有节点无产出档）黄灯

## 5. 台账与任务板（自研）

三张表（SQLite 或 md 索引，待定）：
- **成员表**：名字(kebab, `<任务号>-<角色>`) · 角色 · 模式(前台/后台) · 层数 · 上级 · 状态 · 模型 · 费用 · 最新产出档 · 最后活动
- **任务表**：任务号 · 状态 · 占用文件 · 依赖 · 归属成员 · 产出
- **文档表**：档级 · 归属 · 绝对路径 · 字数 · 摘要 · 时间

约束映射：相邻寻址 → 上报/下发只走直接层级（跨层靠三档文档转贴）；释放 → 记"已释放/失联"并告警未完成任务

## 6. 面板（自注册独立 tab，不 fork better-sidebar）

- 实现：`ctx.betterSidebar.registerTab(descriptor)`（返回 disposer，HMR 安全）
- 与官方「任务管理」页**并存**：命名/图标区分（如「契约」/「协作」），并关闭它的 `autoOpenSubagent` 自动展开
- **新增功能（本次需求）**：
  1. **点击卡片跳转到该子智能体会话**（现状点不进去；抓手：客户端 `ui-session` 包的会话切换能力）
  2. **角色配色标签**：审查/对抗审查专属颜色卡片；角色 + 状态 + 前台/后台 三重标签
  3. **隐藏已完成/一次性节点**（过滤开关）
  4. **合规徽章**：契约✓ · 三档✗ · 进度✗ · 预算超限⚠ · 异源✓/✗

## 7. 审计（警告制）

`audit_scan` 对账项（可配开关）：缺档 / 超字数 / 漏槽位 / 进度未更新 / 异源校验（审查者模型家族≠实现者）/ 幽灵 run / 孤儿任务（成员已释放但任务未结）/ 未释放 run / 名册-任务表不一致。
输出：红黄绿清单 + 人话报告（图书管理员整理）。

## 8. 图书管理员

- 触发：**批次完成或审计报警时自动唤醒**（后台成员，常驻可续接）
- 职责：核心数据库（迁移进度计数/行号/阻塞清单）· 坑库索引 · 待办勾选 · 命名与档级校验 · 去重归档 · 审计报告整理

## 9. 已定 / 待办

**已定**：底座路线 · 面板方案（独立 tab）· auto-review 保留 · 审计警告制 · 预算软硬限与 30% 规则 · 通用化配置层
**待办（M0/M1 处理）**：
1. 对抗源具体接入（GLM/Qwen 路由 + 凭据）
2. 项目配置的落地形式（插件设置 UI 还是 cordis.patch.yml 手填）
3. 台账存储选型（SQLite vs md 索引）

## 10. 里程碑（草案）

- **M0 环境**：移除 team bundle → 恢复 continuable；建插件骨架 + 项目配置层（跑通"配置→生效"）
- **M1 契约引擎**：多实例委派通道 + 契约/角色卡注入（先跑通"研究→实现"两角色）
- **M2 产出契约 + 台账**：doc_emit / progress_upsert / 三张表
- **M3 面板**：独立 tab（谱系图 + 跳转 / 配色标签 / 隐藏已完成 / 合规徽章）
- **M4 审计 + 图书管理员**：audit_scan + 自动唤醒
- **M5 端到端**：拿一批真实迁移任务跑通全链（研究→实现→审查→对抗审查→修复→收档）

## 附：源规范（要迁移的本体，[1] 总纲摘要）

1. 核心原则与传递性（创建子 Agent 时必须完整传递 [1]~[2]）
2. 进度管理（[Agent进度] 下 .md，实时推进，≤400 字）
3. 前期调研与阅读（先读 [任务] 摘要+项目简介，再读 [位置] 下 debug 文件与相关文件）
4. 任务执行（读上级指定的任务文件）
5. 产出规范（三档文档：三级 250~300 字 → 二级加变量/物品/状态 → 一级 ≤3000 字存 [位置]）
6. 文档流转（三级→上级直发；二级→上级的上级）
7. 异常处理（严重 Bug 修复总结 .md → [位置]）
8. 子 Agent 管理（该层 ≤4 且总数 ≤20 才可分裂；为下一级配置所有 [] 信息）
2』状态信息：`[层数]` `[总Agent数]`
