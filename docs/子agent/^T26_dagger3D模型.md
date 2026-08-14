# T26 dagger 3D 模型 —— MCP 阻塞报告喵

> 状态：**BLOCKED（blockbench MCP 不可达）**，尚未产出模型喵。
> 日期：2026-08-14 喵。

## 结论（一句话）喵

本会话内 `mcp__blockbench__*` 工具全部不可用，Blockbench 的 MCP 插件未在 `localhost:3000` 监听，按任务强制要求**不得改用其他建模/自绘方案**，已停下等待 MCP 就绪后重试喵。

## MCP 状态诊断喵

| 检查项 | 结果 |
|---|---|
| 会话工具列表中的 `mcp__blockbench__*` | 无（一个都没有，MCP 未连接）喵 |
| 端口 3000 本地监听 | 未监听（`netstat` 无 `:3000` LISTENING）喵 |
| `curl http://localhost:3000/bb-mcp` | HTTP 000（连接失败/拒绝）喵 |
| MCP 配置位置 | `C:/Users/flafk/.claude.json` → `projects["C:/Users/flafk"].mcpServers.blockbench`：`npx mcp-remote http://localhost:3000/bb-mcp` 喵 |
| 项目级配置（D:/Blockdustry） | 无 `.claude.json` / `.claude/settings.json`，未注册 blockbench MCP 喵 |

## 判定喵

- Blockbench MCP 插件（监听 3000 的 bb-mcp 服务端）**未启动**，且当前 Claude Code 会话也未注入任何 `mcp__blockbench__*` 工具喵。
- 根因二选一：① Blockbench 未打开或未装/未启用 MCP 插件；② 即使 3000 起来了，本会话工具表已固定，仍需重开会话才能注入 MCP 工具喵。

## 就绪条件 / 重试步骤喵

1. 打开 Blockbench，确认 MCP 插件（bb-mcp，监听 `http://localhost:3000/bb-mcp`）已启用且日志无报错喵。
2. 确认 `netstat -ano | findstr :3000` 出现本地 LISTENING，或 `curl http://localhost:3000/bb-mcp` 返回非 000 喵。
3. 重新发起本任务（新会话），让 Claude Code 加载 blockbench MCP 工具喵。
4. 重试后执行计划（见下）喵。

## 就绪后的执行计划（供重试参考）喵

### 建模（blockbench-modeling，Modded Entity / Java 实体格式）喵
- 参考 `D:\Blockdustry\Mindustry\core\assets-raw\sprites\units\dagger.png`（及 dagger-base.png / dagger-leg.png）喵。
- 结构：root → body（躯干）+ head（头）+ leg_left/leg_right（双足）+ weapon（炮管）喵。
- cube 几何小尺寸（Mindustry dagger 约 0.7 格），比例贴近原 sprite 喵。

### 贴图（blockbench-texturing）喵
- 新建贴图，裁剪/复用现有 `src/main/resources/assets/blockdustry/textures/entity/dagger.png` 的像素，或 blockbench 内绘制喵。
- 导出 PNG 到 `src/main/resources/assets/blockdustry/textures/entity/dagger.png`（替换）喵。

### 导出（blockbench export）喵
- 导出 Minecraft Java 实体模型 JSON 到 `src/main/resources/assets/blockdustry/models/entity/dagger.json`（若用 Modded Entity codec 也导 LayerDefinition 数据）喵。

### 集成（Java）喵
- 新建 `client/model/DaggerModel.java`：`EntityModel<DaggerUnitEntity>` + `static createBodyLayer()`，几何完全取自 blockbench 导出喵。
- 改 `client/DaggerUnitRenderer.java`：注入 `ModelPart`（ctx.bakeLayer），`render()` 里 `model.setupAnim(...)` + `model.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, 1,1,1,1)`，替换平面 quad，沿用 0xF000F0 全亮喵。
- `BlockdustryClient`：`EntityRenderersEvent.RegisterLayerDefinitions` 注册 `ModelLayerLocation`，渲染器 `ctx.bakeLayer(layer)` 取模型喵。

### 行为不变 + 编译喵
- 不动 `DaggerUnitEntity` 行为逻辑喵。
- `./gradlew compileJava` 通过喵。

## 本次未改动清单喵
- 未新建/改写任何模型 JSON、贴图 PNG、`DaggerModel.java` 等产物喵（避免用手写/自绘替代方案）喵。

## 复核（2026-08-14，用户反馈「本地配置已有 blockbench MCP」后复查）喵
- 用户反馈正确：`~/.claude.json` 中 `mcpServers.blockbench` 配置确实存在（`npx mcp-remote http://localhost:3000/bb-mcp`，stdio）喵。
- Blockbench 应用内插件**已登记**：`AppData\Roaming\Blockbench\Local Storage\leveldb` 记录 `installed_plugins=[{"id":"blockbench-mcp-plugin","version":"0.0.1","path":"https://github.com/jasonjgardner/blockbench-mcp-plugin","source":"url"}]` 喵。
- 但 **bb-mcp 服务端未运行**：`Test-NetConnection 127.0.0.1:3000` 与 `localhost:3000` 均 `TcpTestSucceeded=False`；`netstat` 监听列表无 3000 端口喵。
- 疑点：`plugins\blockbench-mcp-plugin.js`（349KB，11:50 修改）内容为 GitHub 仓库页 HTML（`<title>jasonjgardner/blockbench-mcp-plugin ...</title>`），并非有效插件 JS，怀疑是「另存为」误存页面；真正插件代码应在 Blockbench 缓存中喵。
- `mcp-remote` npm 包未全局安装（`npm ls -g` 为空），但 `npx` 可联网自动下载，非阻塞点喵。
- 本会话工具列表仍无任何 `mcp__blockbench__*` 工具，MCP 未注入喵。
- 判定维持 **BLOCKED**：配置/插件登记在（用户正确），但服务端未监听 + 当前会话工具未注入，仍需协调者处理喵。
