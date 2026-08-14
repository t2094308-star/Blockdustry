# ^P1 批1E power-node-large + battery-large 整合清单喵

> 任务登记: `D:\Blockdustry\任务\T41_1E_B_大型节点电池.md`。机制/数据忠于 Mindustry v8 原版，贴图拷原版（md5 校验一致）不重绘喵。

## 核心数据库登记（协调者要求的一行）喵
- `power-node-large` | 大型电力节点 | 电力 | 依赖：电力网络体系（科技树父=power-node）；size 2；PowerNode 类
- `battery-large` | 大型电池 | 电力 | 依赖：电力网络体系（科技树父=battery）；size 3；Battery 类

## 零、数据核对结论（数据不串最高要求）喵
- `Blocks.java L2487-2492`：`powerNodeLarge = new PowerNode("power-node-large")`：requirements=钛×5 铅×10 硅×3；**size 2**；**maxNodes 15**；**laserRange 15f**。类 = PowerNode（非 diode/surgeTower，勿串用 surgeTower 的 maxNodes 2 / laserRange 40）喵。
- `Blocks.java L2512-2517`：`batteryLarge = new Battery("battery-large")`：requirements=钛×20 铅×50 硅×30；**size 3**；**consumePowerBuffered(50000f)**；baseExplosiveness 5f（被毁会炸，未迁移爆炸——与 1×1 battery 一致）。类 = Battery（非其他储能建筑）喵。
- **动画特效（用户最高要求）**：原版 `Battery.drawer = DrawMulti(DrawDefault, DrawPower, DrawRegion("-top"))`。DrawPower 无 `-power` 贴图（已 grep 全库确认无 `battery*-power.png`），走 `Fill.square` 以 `lerp(emptyLightColor=f8c266, fullLightColor=fb9567, power.status)` 实心色方块垫底；顶盖 `battery-large-top.png` 中央 35% 透明区露出变色发光核心——即原版「电量显示动画」。原版 Battery **无任何 Fx 粒子**，故不瞎编粒子，已如实实现发光方片+顶盖。power-node-large 无额外光效（仅贴图切角+激光），激光沿用现有 BlockdustryPowerNode 激光机制做 2×2 版喵。
- 中文名/描述用官方 bundle：`block.power-node-large.name=大型电力节点`（en Large Power Node）、`block.battery-large.name=大型电池`（en Large Battery）。描述：power-node-large「连接范围更大的高级电力节点。」；battery-large「储存电网多余电力，并在电网供电不足时放电。比普通电池容量更高。」

## 一、已交付（独立新文件，已含渲染，主会话勿重复注册）喵

### 大型电力节点 power-node-large（2×2）
| 文件 | 说明 |
|---|---|
| `src/main/java/com/blockdustry/power/PowerNodeLargeRegistrar.java` | 自包含注册类（模板 FuseArcRegistrar/SorterRegistrar）：POWER_NODE_LARGE 方块+物品+实体；size 2、strength 2（同 1×1 power_node）喵 |
| `src/main/java/com/blockdustry/power/PowerNodeLargeBlockEntity.java` | 忠实移植 PowerNode.PowerNodeBuild：LASER_RANGE=15、MAX_NODES=15、autolink 首 tick 三维球扫描、linkValid（同队/距离/对方节点容量）、双向 connect（兼容 1×1 节点）、NBT 持久化。**多格连通性**：锚点格 `getPowerLinks()` = 实际链接 + 本组 4 格，保证节点连到任意格整组入网（电网不分裂）喵 |
| `src/main/java/com/blockdustry/client/PowerNodeLargeBlockEntityRenderer.java` | 2×2 版激光渲染（沿用 1×1 激光机制：白→琥珀按满足率 lerp、billboard 双面光柱、端点十字光点、NO_OVERLAY 防染黑）。激光源点=2×2 整组中心，目标若是大型节点偏移半格；不画 1×1 专属倒角（大型节点贴图自带切角观感）喵 |
| `src/main/java/com/blockdustry/power/PowerNodeLargeInteractHandler.java` | 右键两阶段连接/断开（@EventBusSubscriber，独立选择 key，镜像 1×1 交互）喵 |
| `src/main/java/com/blockdustry/power/PowerNodeLargeLinkHandler.java` | 先放大节点后放用电器自动反向连接（@EventBusSubscriber，半径 15 事件驱动）喵 |

### 大型电池 battery-large（3×3）
| 文件 | 说明 |
|---|---|
| `src/main/java/com/blockdustry/power/BatteryLargeRegistrar.java` | 自包含注册类：BATTERY_LARGE 方块+物品+实体；size 3、strength 3（同 1×1 battery）喵 |
| `src/main/java/com/blockdustry/power/BatteryLargeBlockEntity.java` | 忠实移植 Battery.BatteryBuild：CAPACITY=50000、powerStatus 0..1、充放电由电网结算。**多格连通性**：仅锚点格报容量 50000（非锚点格 0 防重复计数），锚点格 `getPowerLinks()` = 本组 9 格保证节点连到任意格整组入网喵 |
| `src/main/java/com/blockdustry/client/BatteryLargeBlockEntityRenderer.java` | 原版 Battery 特效：发光方片（Mindustry Fill.square 半径 11 世界单位=1.375 格，颜色按 status lerp f8c266→fb9567）+ 顶盖 battery_large_top.png（中央透明露出发光核心）。只用 entityTranslucent + NO_OVERLAY（坑/BER渲染.md、PowerNode激光黑色.md）喵 |

### 资源（唯一文件名，贴图拷原版 PNG 不重绘）
| 文件 | 说明 |
|---|---|
| `assets/blockdustry/blockstates/power_node_large.json` | 9 corner 变体 → 4 象限模型（2×2）喵 |
| `assets/blockdustry/blockstates/battery_large.json` | 9 corner 变体 → 9 宫格模型（3×3）喵 |
| `assets/blockdustry/models/block/power_node_large.json` + `_{nw,ne,sw,se}.json` | 基底全顶面 + 2×2 四象限顶面裁剪模型（UV 每格 8/16）喵 |
| `assets/blockdustry/models/block/battery_large.json` + `_{nw,n,ne,w,c,e,sw,s,se}.json` | 基底全顶面 + 3×3 九宫格顶面裁剪模型（UV 每格 5.333/16）喵 |
| `assets/blockdustry/models/item/power_node_large.json` / `battery_large.json` | 父=block 基底模型喵 |
| `assets/blockdustry/textures/block/power_node_large.png` | 拷原版 `power-node-large.png`（64×64，md5 0154dd53…一致）喵 |
| `assets/blockdustry/textures/block/battery_large.png` | 拷原版 `battery-large.png`（96×96，md5 33c101fe…一致）喵 |
| `assets/blockdustry/textures/block/battery_large_top.png` | 拷原版 `battery-large-top.png`（96×96，md5 b6b7c965…一致）喵 |
| `assets/blockdustry/textures/research/blocks/power_node_large.png` | 科技树图标（拷原版 power-node-large.png）喵 |
| `assets/blockdustry/textures/research/blocks/battery_large.png` | 科技树图标（拷原版 battery-large.png）喵 |

## 二、主会话挂载点（按序合并，全部必须）喵

### 1. register — `Blockdustry.java` 构造器
在 `com.blockdustry.building.PlastaniumCompressorRegistrar.register(modEventBus);` 之后加两行：
```java
com.blockdustry.power.PowerNodeLargeRegistrar.register(modEventBus);
com.blockdustry.power.BatteryLargeRegistrar.register(modEventBus);
```

### 2. tab — `BlockdustryBlocks.java` POWER_TAB（Category.power 归电力 tab）
在 `output.accept(POWER_SOURCE_ITEM);` 之后加两行：
```java
output.accept(com.blockdustry.power.PowerNodeLargeRegistrar.POWER_NODE_LARGE_ITEM);
output.accept(com.blockdustry.power.BatteryLargeRegistrar.BATTERY_LARGE_ITEM);
```

### 3. ResearchNodes — `ResearchNodes.java` all()，battery 节点之后加 2 节点
原版 SerpuloTechTree L205-218：combustionGenerator→powerNode→(powerNodeLarge→diode→surgeTower / battery→batteryLarge)。powerNodeLarge 父=powerNode、batteryLarge 父=battery，均无额外 researchCost 覆盖（=requirements）喵：
```java
ResearchNode.builder("power_node_large")
        .parent("power_node")
        .unlockBlock(com.blockdustry.power.PowerNodeLargeRegistrar.POWER_NODE_LARGE.get())
        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 5) // Mindustry power-node-large = 钛×5 铅×10 硅×3 喵
        .buildRequirement(lead, 10)
        .buildRequirement(silicon, 3)
        .build(),
ResearchNode.builder("battery_large")
        .parent("battery")
        .unlockBlock(com.blockdustry.power.BatteryLargeRegistrar.BATTERY_LARGE.get())
        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 20) // Mindustry battery-large = 钛×20 铅×50 硅×30 喵
        .buildRequirement(lead, 50)
        .buildRequirement(silicon, 30)
        .build(),
```
（`lead`/`silicon` 为 all() 顶部已声明的局部变量；若顶部无 `silicon`，可写 `BlockdustryBlocks.SILICON.get()`）

### 4. 渲染 — `BlockdustryClient.java` registerRenderers
在 battery 相关渲染行之后加两行：
```java
event.registerBlockEntityRenderer(com.blockdustry.power.PowerNodeLargeRegistrar.POWER_NODE_LARGE_ENTITY.get(), com.blockdustry.client.PowerNodeLargeBlockEntityRenderer::new);
event.registerBlockEntityRenderer(com.blockdustry.power.BatteryLargeRegistrar.BATTERY_LARGE_ENTITY.get(), com.blockdustry.client.BatteryLargeBlockEntityRenderer::new);
```

### 5. ResearchIcons — `ResearchIcons.java` nodeTexture
在 `case "battery" -> ...` 之后加两行：
```java
case "power_node_large" -> ResourceLocation.tryParse(base + "power_node_large.png");
case "battery_large" -> ResourceLocation.tryParse(base + "battery_large.png");
```

### 6. 多格组血量 — `BlockdustryBlocks.java` registerBlockHealthDefaults
在 battery 相关行之后加两行：
```java
registerGroupMaxHp(com.blockdustry.power.PowerNodeLargeRegistrar.POWER_NODE_LARGE.get(), 2); // power-node-large 2×2 组血（strength 2 同 1×1）喵
registerGroupMaxHp(com.blockdustry.power.BatteryLargeRegistrar.BATTERY_LARGE.get(), 3); // battery-large 3×3 组血（strength 3 同 1×1）喵
```

### 7. lang — `lang/en_us.json` + `lang/zh_cn.json` 追加
见 `^P1_批1E_powerNodeLargeBatteryLarge_lang_en.json` / `_zh.json` 片段（en 官方 Large Power Node / Large Battery；zh 官方 大型电力节点 / 大型电池）喵。

## 三、行为要点（验收对照）喵
- 放置：power-node-large 2×2、battery-large 3×3（corner 跨格模型 + 组碰撞箱 + 组血）喵
- 大型节点：maxNodes 15、laserRange 15f；首 tick 自动连接半径 15 内同队有电建筑；后放用电器由 PowerNodeLargeLinkHandler 反向连；右键空手两阶段连接/断开；激光白→琥珀按电网满足率 lerp（满电近白、缺电琥珀）喵
- 大型电池：容量 50000，充放电由电网结算（盈余充电/缺口放电，PowerGrid 已支持多电池按容量均分）；充电中顶盖中央发光核心由琥珀渐橙（status 0→1），即原版电量显示动画喵
- 科技树：power_node 下「大型电力节点」、battery 下「大型电池」；创造栏电力 tab 出现喵

## 四、风险 / 待协调喵
- **并行任务编译阻塞（重要）**：T38_B 子任务的 `building/MenderBlockEntity.java` L218 有编译错误 `NbtUtils.readBlockPos(list.getCompound(i))`（1.21.1 签名需 2 参 `readBlockPos(CompoundTag, String)`），导致整仓 `compileJava` 失败。我已隔离验证本任务 8 个文件编译通过（仅既有弃用警告），并已恢复该文件。请协调者知会 T38_B agent 修复。
- battery-large `baseExplosiveness 5f`（被毁爆炸）未迁移：与现有 1×1 battery 一致（无爆炸体系），如后续需要可加 BlockHealth 破坏事件。
- 多格电力建筑入网靠「锚点格 getPowerLinks() 含本组全部格」实现；PowerGridManager 仍会遍历所有格（含非锚点），但非锚点格容量/链接为 0/空，仅作连通桥梁，电网结算等效整组一份电池。该方案与既有 laser-drill（3×3 耗电）同思路，未改共享文件。
- 双节点互连会双份激光（1×1 节点间本就如此，沿用现状）。
- 主会话合并后请跑 `./gradlew compileJava`（需先等 T38_B 修复 Mender）+ 游戏冒烟：电力 tab 出现两建筑、放置 2×2/3×3、节点接电池充电顶盖变色、科技树新节点喵。
