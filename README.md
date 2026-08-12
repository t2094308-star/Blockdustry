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
- 电力与物流系统
- 炮塔防御与敌对波次
- 队伍归属与友军识别

---

## 前置依赖

- **Block Health**（方块血量前置库）：https://github.com/t2094308-star/Block-Health
- **Jade**（可选，开启后显示方块血量/建筑信息/进度条）：https://modrinth.com/mod/jade
- NeoForge 1.21.1

---

## 当前开发状态

早期开发中 — 核心系统正在建设，尚未发布可玩版本

- [x] 方块血量系统（前置库）
- [x] 队伍系统（Mindustry 原作队伍 + 队伍色 + 调试棒 UI）
- [x] 建筑系统（机械钻头 2×2 挖矿、duo 双管炮、多格高亮框、内置库存、3×3 跨格贴图）
- [x] 物流系统（传送带 + Router 物品传递、物品平躺/缩放）
- [x] 加工工厂（石墨压缩机：2 煤 → 1 石墨，带制作进度）
- [x] 电力系统（PowerNode 节点 + 电网 + 燃烧发电机 + 电池 + 激光光效）
- [x] 炮塔动画（duo 转盘 + 双炮管旋转/后坐力）
- [x] 炮弹平滑（客户端自模拟，弹道顺畅）
- [x] 机器动画与粒子（发电机预热染色 + 火花、压机灰尘粒子、工厂预热染色）
- [x] 核心（3×3、队伍共享存储 + 队伍染色、死亡在核心重生、右上资源栏）
- [x] 单位系统（地面单位工厂：硅+铅 → dagger，dagger 索敌攻击 + 渲染）
- [x] 物品源（debug 方块无限产煤/石墨/硅/铅）
- [x] Jade 联动（血量/电量/进度条、建筑信息、队伍色）
- [ ] 三维物流（传送带上下坡，方案见 `docs/`）
- [ ] 科技树
- [ ] 基础资源与加工
- [ ] 更多单位与单位 AI（dagger 仅 MVP，见 `docs/`）
- [ ] 敌人波次

---

## 探索笔记

`docs/` 目录存放对 Mindustry 原版机制的探索笔记（单位工厂、电力系统、三维物流、炮弹卡顿、炮塔动画、机器粒子、Jade 联动、核心与队伍共享资源等），供后续移植参考。

---

## 调试

- `/blockdustry team get/set/player <...>` 队伍命令
- `/blockdustry building get <pos>` 建筑信息
- `/blockdustry tick` 模组 tick 状态
- 队伍调试棒（创造栏「方块工业」）：右键目标查看/设置队伍、查看电量
- 安装 Jade 后准星指向建筑可查看血量 / 内容物 / 进度 / 电量

---

## 下载

（短时间内不会有打包完成的版本）

---

## 许可证

本项目使用 GPL-3.0 许可证开源，灵感来源于 Mindustry。

---

当前版本: 0.0.1-alpha
