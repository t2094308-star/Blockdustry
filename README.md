# Blockdustry

[![NeoForge](https://img.shields.io/badge/NeoForge-1.21.1-brightgreen)](https://neoforged.net/)
[![License](https://img.shields.io/badge/License-GPL--3.0-blue)](LICENSE)
[![Mindustry](https://img.shields.io/badge/Inspiration-Mindustry-ff69b4)](https://github.com/Anuken/Mindustry)
[![Status](https://img.shields.io/badge/Status-早期开发-red)]()

将 [Mindustry](https://github.com/Anuken/Mindustry) 的工业自动化与塔防玩法，带入 Minecraft 的方块世界。

---

## 简介

Blockdustry 是一个 Minecraft 模组，受经典工厂塔防游戏 Mindustry 启发，在三维方块世界中重建：

- 资源采集与加工产线
- 电力与物流系统（含垂直提升机）
- 炮塔防御与目标标签（对地/对空）
- 队伍归属与队伍共享资源
- 科技树（仿 Mindustry 研究）

---

## 前置依赖

- **Block Health**（方块血量前置库，多格整组血量）：https://github.com/t2094308-star/Block-Health
- **Jade**（可选，显示血量/建筑信息/各类条）：https://modrinth.com/mod/jade
- NeoForge 1.21.1

---

## 当前开发状态

早期开发中 — 核心系统正在建设，尚未发布可玩版本

- [x] 方块血量系统（前置库，多格整组共享血量 + 整组裂纹）
- [x] 队伍系统（Mindustry 原作队伍 + 队伍色 + 调试棒 UI）
- [x] 建筑系统（机械钻头 2×2 挖矿、多炮塔、多格高亮框、内置库存、3×3 跨格贴图）
- [x] 物流系统（传送带 + Router 物品传递 + 垂直提升机，物品平躺/缩放）
- [x] 加工工厂（石墨压缩机/硅冶炼厂/窑炉/塑钢压缩机/硫化物混合器/粉碎机/焚化炉/相织布编织器）
- [x] 电力系统（PowerNode 节点 + 电网 + 发电机 + 电池 + 电力源 + 激光光效；太阳能/大型节点/大型电池/二极管/涌电塔）
- [x] 炮塔：duo 双管、scatter 分裂（对空）、fuse 雷光（金属碎片霰弹，非火焰）、arc 电弧（闪电链，用电）
- [x] 目标标签（炮台对地/对空，单位陆/空/海）
- [x] 炮塔动画（转盘 + 炮管旋转/后坐力）+ 附身操控 + 穿透视野
- [x] 炮弹平滑 + 装甲机制（固定减伤）
- [x] 机器动画与粒子（发电机预热染色 + 火花、压机灰尘粒子、工厂预热染色）
- [x] 核心（3×3×3、队伍共享存储 + 队伍染色、死亡在核心重生、右上资源栏）
- [x] 单位系统（地面单位工厂 → dagger 索敌攻击 + 渲染）
- [x] 材料（赛普罗材料：铜/铅/玻璃/钛/硅/石墨/塑料钢等，覆盖原版煤/铜视觉）
- [x] 物品源（debug 方块菜单式产材料）+ 电力源（debug 无限产电）
- [x] 科技树（仿 Mindustry：树形 UI + 研究扣队伍资源 + 门控）
- [x] Jade 联动（血量/电量/进度条、建筑信息、队伍色）
- [x] 灵魂出窍（F4 freecam）
- [ ] 更多单位与单位 AI（dagger 仅 MVP）
- [ ] 敌人波次
- [ ] 科技树内容扩充（大量建筑已迁，研究节点随迁移持续补充）

---

## 大规模迁移进度

详见 [核心数据库](./docs/核心数据库.md)

按 P1 计划大规模并行迁移 Mindustry 赛普罗内容（机制/数据优先，贴图一律拷 Mindustry 原版 PNG 不重绘，动画特效同步迁移）。（含目录锚点跳转、已迁移/未迁移逐项备注、物品→产出建筑映射、横切阻塞清单）。

截至 2026-08-14 已迁移 **78 / 205** 项（建筑 61 + 物品 16 + 单位 1），未迁移 127：

- 批1A 物流：junction / distributor / sorter / inverted-sorter / overflow-gate / underflow-gate
- 批1B 存储+桥梁：container（2×2/容量300）、bridge-conveyor（传送带桥）
- 批1C 生产：silicon-smelter、kiln（窑炉）、plastanium-compressor、pyratite-mixer、pulverizer、incinerator、phase-weaver（相织布编织器）
- 批1D 钻机：pneumatic-drill（气动钻头）、laser-drill（激光钻头 3×3）、blast-drill（爆破钻头 4×4）
- 批1E 电力：solar-panel(+large)、power-node-large、battery-large、diode、surge-tower（巨浪电力塔）
- 批1F 墙体：copper/scrap/titanium-wall(+large)、door(+large)
- 批2A 高级墙：plastanium/thorium/surge-wall(+large)（surge 合金墙受击放电）
- 批2B 防御场：mender（修理器）、force-projector（力墙投影）
- 雷光（fuse）特效重做：金属碎片霰弹（纠正"火焰炮"误判）、炮口闪光/命中白闪/线光点光/热区/正确后坐

横切阻塞（后续批次需先做前置）：液体系统（P2，波及 30+ 块）、size 4-5 扩 Corner（Frame-0）、payload（永延）、逻辑 VM（P3）。

---

## 指令一览

调试棒（`/give @s blockdustry:debug_stick`）右键目标方块/实体打开队伍 UI（看/设队伍、电量）。

- `/blockdustry team get <x> <y> <z>` — 查询方块队伍
- `/blockdustry team set <x> <y> <z> <队伍>` — 设置方块队伍（DERELICT/SHARDED/CRUX/MALIS/GREEN/BLUE/NEOPLASTIC）
- `/blockdustry team player <队伍>` — 设置玩家自己的队伍
- `/blockdustry building get <pos>` — 查询建筑信息
- `/blockdustry tick` — 模组 tick 状态
- `/blockdustry research unlockall` — **一键解锁全部科技**（debug）
- 快捷键：`F4` 灵魂出窍，`J` 打开科技树

---

## 探索与坑文档

- `docs/` 存放 Mindustry 机制探索笔记（已完成项加 `^` 前缀）
- `docs/坑/` 按主题分类的坑记录（处理任务前按主题查阅，出错后更新）
- `docs/子agent/` 各子 agent 阶段产出

---

## 下载

（短时间内不会有打包完成的版本，但已提供最短流程，已着手大规模迁移）

---

## 许可证

本项目使用 GPL-3.0 许可证开源，灵感来源于 Mindustry。

---

当前版本: 0.0.1-alpha
