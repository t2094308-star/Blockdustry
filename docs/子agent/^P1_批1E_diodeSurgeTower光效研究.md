# ^P1_批1E diode/surge-tower 原版光效研究喵

> 任务要求：深入研究 surge-tower 的「闪电放电特效」（从塔顶放射电弧到远处节点）原版实现，逐条记录到文档再实现到渲染器喵。
> **结论先行：原版 surge-tower 没有任何闪电/电弧特效，它就是普通 PowerNode（标准激光连接线）。本任务不造任何闪电特效，渲染器忠实原版激光喵。**

---

## 一、原版 surge-tower 完整定义（Blocks.java L2494-2500）喵

```java
surgeTower = new PowerNode("surge-tower"){{
    requirements(Category.power, with(Items.titanium, 7, Items.lead, 10, Items.silicon, 15, Items.surgeAlloy, 15));
    size = 2;
    maxNodes = 2;
    laserRange = 40f;
    schematicPriority = -15;
}};
```

- 类是 **PowerNode**（`world/blocks/power/PowerNode.java`），不是带闪电的独立类喵。
- 参数：size=2（2×2）、maxNodes=2、laserRange=40f（远距）、schematicPriority=-15 喵。
- **没有任何 Lightning/Fx.lightning/电弧生成或播放代码**。全工程搜 `surgeTower|surge-tower|surge_tower` 只命中：Blocks.java 声明、SerpuloTechTree.java 技术树、Blocks.java L137 列表。无专属渲染器/特效/子弹喵。

## 二、PowerNode 视觉机制（surge-tower 的唯一视觉）喵

### 1. 连接线 = 标准激光（Drawf.laser），不是闪电喵
- `PowerNode.java` 字段：`@Load(value = "@-laser", fallback = "laser")` 与 `@Load(value = "@-laser-end", fallback = "laser-end")` 喵。
- surge-tower **没有** `surge-tower-laser.png`/`surge-tower-laser-end.png`，故回退到通用 `effects/laser.png`（4×48）与 `laser-end.png`（72×72）喵。
- `PowerNodeBuild.draw()`：对每个链接调 `drawLaser(x, y, link.x, link.y, size, link.block.size)` → `Drawf.laser(laser, laserEnd, laserEnd, x1, y1, x2, y2, laserScale, light, useLod)` 画**连续光柱**喵。
- 颜色：`setupColor(satisfaction)` = `lerp(laserColor1=white, laserColor2=Pal.powerLight #fbd367, (1-satisfaction)*0.86 + absin(3f, 0.1))`——满电近白、缺电琥珀，带轻微呼吸感喵。
- 与 T15 结论一致：`Drawf.laser(..., light=true)` 还会打一条线形光（LightRenderer.line），电弧类（Fx.lightning）则**不带光**喵。

### 2. 关键区别：激光（Laser）≠ 闪电（Lightning）喵
- **激光**（surge-tower 用）：`Drawf.laser` 直线光柱，白色→琥珀，宽 `laserScale`，`Drawf.light` 线光打亮喵。
- **闪电**（`entities/Lightning.java` + `Fx.lightning`，T15 §四）：锯齿折线 + 随机转向 + 折点圆点 + 10 tick 淡出，`Fx.lightning.at(x, y, rotation, color, lines)` 播放喵。
- surge-tower **完全不涉及闪电**。task 文案中「涌电塔的闪电放电特效」在原版并不存在喵。

## 三、diode 原版视觉（PowerDiode）喵

### 1. 完整定义（Blocks.java L2502-2504）喵
```java
diode = new PowerDiode("diode"){{
    requirements(Category.power, with(Items.silicon, 10, Items.plastanium, 5, Items.metaglass, 10));
}};
```

### 2. PowerDiode 视觉（PowerDiode.java）喵
- `@Load("@-arrow") TextureRegion arrow` → 加载 `diode-arrow.png`（32×32）喵。
- `PowerDiodeBuild.draw()`：
  ```java
  Draw.rect(region, x, y, 0);                    // 底座 diode.png
  Draw.rect(arrow, x, y, rotate ? rotdeg() : 0); // 箭头随旋转
  ```
- 底座 `diode.png`（32×32）+ 箭头 `diode-arrow.png`（32×32），箭头按放置朝向旋转喵。
- **无闪电、无粒子、无指示灯动画**，唯一的特殊视觉就是旋转的箭头喵。
- `drawPlanRegion` 规划预览同样画底座 + 箭头喵。
- Bars：`back`（input，后侧电池%）与 `front`（output，前侧电池%）两条电力条（HUD 层，非世界视觉）喵。

## 四、Blockdustry 实现对照喵

| # | 项 | 原版 | Blockdustry 实现 |
|---|---|---|---|
| 1 | surge-tower 视觉 | 标准 PowerNode 激光光柱（laser.png，白→琥珀） | SurgeTowerBlockEntityRenderer 画白→琥珀双面光柱（同 PowerNodeBlockEntityRenderer 激光法）喵 |
| 2 | surge-tower 参数 | size2/maxNodes2/laserRange40 | SurgeTowerBlockEntity 常量同原版喵 |
| 3 | surge-tower 闪电 | **不存在** | **不实现**（绝不瞎编）喵 |
| 4 | diode 底座 | diode.png 32×32 | 方块模型 cube_all 用 diode.png（拷原版）喵 |
| 5 | diode 箭头 | diode-arrow.png 随朝向旋转 | DiodeBlockEntityRenderer 在顶面画旋转箭头（facing.toYRot()）喵 |
| 6 | diode 机制 | 后侧电池%>前侧才转移（单向） | DiodeBlockEntity 同原版公式 + BFS 双向聚合喵 |
| 7 | diode bars | 后/前侧电池%电力条 | 未实现 HUD 条（见整合清单「可选增强」）喵 |

## 五、关于「闪电放电特效」的结论（需向协调者报告）喵

- **找不到 surge-tower 原版闪电特效，原版就是普通 PowerNode 标准激光**喵。
- 按任务协作规则「找不到原版动画/特效实现，绝不自己瞎编，报告协调者」：**本任务不实现闪电特效**，只忠实实现原版激光连接线喵。
- 若协调者确实想要「电弧放射」观感（属增强，非原版），可另开子任务基于 `ArcBeamEntity/ArcBeamRenderer`（勿改，只参考）+ T15 §六 的电弧分层方案（内层亮线 + 外层光晕 + 淡出）在渲染器里加光晕层；但这**不是** surge-tower 的原版视觉喵。

## 占用/交接喵
- 只读（Mindustry）：`world/blocks/power/PowerNode.java`、`world/blocks/power/PowerDiode.java`、`content/Blocks.java` L2494-2504、`content/SerpuloTechTree.java`、`assets-raw/sprites/blocks/power/{surge-tower,diode,diode-arrow}.png`、`assets-raw/sprites/effects/laser*.png` 喵。
- 只读（Blockdustry）：`power/PowerNodeBlockEntity.java`、`client/PowerNodeBlockEntityRenderer.java`、`entities/ArcBeamEntity.java`、`client/ArcBeamRenderer.java` 喵。
- 交接给：主会话（据此决定是否另开「电弧光晕增强」子任务）喵。

## 异常喵
- 无。全程只读原版/现有实现，未改任何共享文件；surge-tower 按原版 PowerNode 激光实现喵。
