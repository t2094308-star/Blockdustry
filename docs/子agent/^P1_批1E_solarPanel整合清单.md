# ^P1 批1E solarPanel 太阳能电池板整合清单喵

> 任务登记: `D:\Blockdustry\任务\T40_1E_A_太阳能电池板.md`。机制/数据忠于 Mindustry v8 原版，贴图拷原版 PNG 不重绘喵。

## 零、数据核对结论（先纠偏，数据不串最高要求）喵
- Mindustry 实测 `Blocks.java L2607-2616` + `SolarGenerator.java`（全文）：
  - `solarPanel = new SolarGenerator("solar-panel")`：requirements(Category.power, 铅×10 硅×8)、powerProduction=0.12f、**size=1**（未覆盖）、health 默认 40（未覆盖）喵
  - `largeSolarPanel = new SolarGenerator("solar-panel-large")`：requirements(铅×60 硅×70 相织布×15)、**size=3**、powerProduction=1.6f、health 默认 40（未覆盖）喵
- **动画/特效结论**：SolarGenerator **无 Drawf.light、无自定义 drawer、无 generateEffect**（继承 PowerGenerator 默认 DrawDefault 静态渲染），两板均**无发光/无环境光效/无动画**。**未自创任何特效**，仅静态模型渲染（符合用户「找不到原版动画/特效绝不瞎编」要求）。原版唯一动态是产电效率随 `Attribute.light` 环境光 + `solarMultiplier`（昼夜）波动——Blockdustry 无昼夜光照系统，简化为恒定产电喵
- 中文名官方 bundle（bundle_zh_CN.properties L2011-2012）：`solar-panel = 太阳能板`、`solar-panel-large = 大型太阳能板`；en = Solar Panel / Large Solar Panel。描述：利用太阳能产生少量电力。喵

## 核心数据库登记（协调者要求字段）喵
> 主会话整合时把下列两行在 `docs/核心数据库.md` 从「未迁移」移到「已迁移」并更新计数：

| Mindustry 注册名 | 中文官方名 | 类别 | 依赖 |
|---|---|---|---|
| solar-panel | 太阳能板 | 电力 | SolarGenerator；size 1；无液体；无耗材 |
| solar-panel-large | 大型太阳能板 | 电力 | SolarGenerator；size 3；无液体；无耗材 |

## 一、已交付（独立新文件，主会话勿重复注册）喵

| 文件 | 说明 |
|---|---|
| `src/main/java/com/blockdustry/power/SolarPanelRegistrar.java` | 自包含注册类（模板 LaserDrillRegistrar）：SOLAR_PANEL（1×1）+ SOLAR_PANEL_LARGE（3×3）方块+物品+方块实体；均 strength 3f（原版 health 40）喵 |
| `src/main/java/com/blockdustry/power/SolarPanelBlockEntity.java` | 1×1 太阳能板 BE：类 SolarGenerator，被动产电恒 0.12f/tick（原版受光照影响已简化为恒定），不吃物品、无耗电/电池；产电接入 BlockdustryPowerNode 电网喵 |
| `src/main/java/com/blockdustry/power/SolarPanelLargeBlockEntity.java` | 3×3 大型板 BE：被动产电 1.6f/tick，**仅锚点格计入产电**（isAnchor 判定，避免 9 格重复计），非锚点格 getPowerLinks 并入锚点网（同 UnitFactoryBlockEntity 模式）喵 |
| `assets/blockdustry/blockstates/solar_panel.json` | 1×1 单 variant → solar_panel 模型喵 |
| `assets/blockdustry/blockstates/solar_panel_large.json` | 3×3 全 9 corner 变体 → solar_panel_large_{nw,n,ne,w,c,e,sw,s,se}（模型置空则黑紫，必须全覆盖）喵 |
| `assets/blockdustry/models/block/solar_panel.json` | cube_all（1×1，贴图 32×32）喵 |
| `assets/blockdustry/models/block/solar_panel_large.json` | 基准 cube 模型（item 图标用，全 UV 16×16）喵 |
| `assets/blockdustry/models/block/solar_panel_large_{nw,n,ne,w,c,e,sw,s,se}.json` | 9 个 3×3 corner 模型：顶面 UV 按 1/3 格裁剪（32px/96px，同 laser_drill 模板），侧面/底面用同贴图喵 |
| `assets/blockdustry/models/item/solar_panel.json` | parent block/solar_panel 喵 |
| `assets/blockdustry/models/item/solar_panel_large.json` | parent block/solar_panel_large 喵 |
| `assets/blockdustry/textures/block/solar_panel.png` | 拷原版 `power/solar-panel.png`（32×32）喵 |
| `assets/blockdustry/textures/block/solar_panel_large.png` | 拷原版 `power/solar-panel-large.png`（96×96）喵 |
| `assets/blockdustry/textures/research/blocks/solar_panel.png` | 科技树图标（拷原版）喵 |
| `assets/blockdustry/textures/research/blocks/solar_panel_large.png` | 科技树图标（拷原版）喵 |

> **无渲染器**：原版 SolarGenerator 无动画/光效/粒子，两板均为静态块渲染，corner 模型即可表达 3×3，故不写 BER（若协调者强制要渲染器骨架可后续加空壳，本任务按「不自创特效」原则省略）喵。

## 二、主会话挂载点（按序合并，全部必须）喵

### 1. register — `Blockdustry.java` 构造器
在 `com.blockdustry.building.PlastaniumCompressorRegistrar.register(modEventBus);` 之后加一行：
```java
com.blockdustry.power.SolarPanelRegistrar.register(modEventBus);
```

### 2. tab — `BlockdustryBlocks.java` POWER_TAB（Category.power 归电力 tab）
在 `output.accept(COMBUSTION_GENERATOR_ITEM);` 之后加两行：
```java
output.accept(com.blockdustry.power.SolarPanelRegistrar.SOLAR_PANEL_ITEM);
output.accept(com.blockdustry.power.SolarPanelRegistrar.SOLAR_PANEL_LARGE_ITEM);
```

### 3. ResearchNodes — `ResearchNodes.java` all()，加 2 节点
parent=solar_panel（原版 SerpuloTechTree L254-258：combustionGenerator→solarPanel→largeSolarPanel；solar_panel 的 parent 就是 combustion_generator）喵：
```java
ResearchNode.builder("solar_panel")
        .parent("combustion_generator")     // Mindustry SerpuloTechTree: combustionGenerator → solarPanel 喵
        .unlockBlock(com.blockdustry.power.SolarPanelRegistrar.SOLAR_PANEL.get())
        .buildRequirement(lead, 10)          // Mindustry solar-panel = 铅×10 硅×8 喵
        .buildRequirement(silicon, 8)
        .build(),
ResearchNode.builder("solar_panel_large")
        .parent("solar_panel")               // Mindustry SerpuloTechTree: solarPanel → largeSolarPanel 喵
        .unlockBlock(com.blockdustry.power.SolarPanelRegistrar.SOLAR_PANEL_LARGE.get())
        .buildRequirement(lead, 60)          // Mindustry solar-panel-large = 铅×60 硅×70 相织布×15 喵
        .buildRequirement(silicon, 70)
        .buildRequirement(com.blockdustry.item.BlockdustryItems.PHASE_FABRIC.get(), 15)
        .build(),
```

### 4. 渲染 — `BlockdustryClient.java` registerRenderers
**无需新增**（见「无渲染器」说明；两板静态模型渲染，注册了 BE 类型但无 BER 注册即可，MC 允许无渲染器方块实体）喵

### 5. ResearchIcons — `ResearchIcons.java` nodeTexture
在 `case "plastanium_compressor" -> ...` 之后加两行：
```java
case "solar_panel" -> ResourceLocation.tryParse(base + "solar_panel.png");
case "solar_panel_large" -> ResourceLocation.tryParse(base + "solar_panel_large.png");
```

### 6. 多格组血量 — `BlockdustryBlocks.java` registerBlockHealthDefaults
在 blast 行之后加一行：
```java
registerGroupMaxHp(com.blockdustry.power.SolarPanelRegistrar.SOLAR_PANEL_LARGE.get(), 3); // solar-panel-large 3×3 组血（strength 3 → 单格 40，9 格 = 360）喵
```

### 7. lang — `lang/en_us.json` + `lang/zh_cn.json` 追加
en_us:
```json
"block.blockdustry.solar_panel": "Solar Panel",
"block.blockdustry.solar_panel_large": "Large Solar Panel"
```
zh_cn:
```json
"block.blockdustry.solar_panel": "太阳能板",
"block.blockdustry.solar_panel_large": "大型太阳能板"
```
（官方 bundle_zh_CN L2011-2012 原文，禁止自创喵）

## 三、行为要点（验收对照）喵
- 类别：SolarGenerator（被动产电，非火力发电机/电力节点），无燃料无人工，恒产电喵
- 产电接入现有 BlockdustryPowerNode 电网：powerProduction 直接返回原版数值（0.12f / 1.6f，每 tick；与 CombustionGeneratorBlockEntity 返回 1f/tick 同单位），powerNeeded=0/capacity=0/stored=0喵
- 放置：solar-panel 1×1、solar-panel-large 3×3（BlockdustryBuildingItem size 3 自动填充 9 格 + 组血），无放置限制喵
- 不吃物品（acceptsItem 恒 false），无库存喵
- 科技树：combustion_generator 下「太阳能板」→ 下挂「大型太阳能板」；电力创造栏出现两板喵
- 渲染：1×1 cube_all 整块贴图；3×3 九格 corner 模型拼整块 96×96 贴图，顶面每格裁 1/3（32px）喵

## 四、风险 / 待人工排查喵
- **compileJava 当前失败与本次交付无关**：`DiodeBlockEntity.java:27`/`SurgeTowerBlockEntity.java:41` 引用未创建的 `DiodeSurgeTowerRegistrar` 包、`MenderBlockEntity.java:218` `NbtUtils.readBlockPos` 参数不匹配——均属并发任务 T42_1E_C_二极管涌电塔 的半成品文件。本任务 3 个 Java 文件（SolarPanelRegistrar/SolarPanelBlockEntity/SolarPanelLargeBlockEntity）无任何编译错误，待并发任务补齐后 compileJava 应通过喵
- 原版产电受环境光（Attribute.light + solarMultiplier，夜间衰减）影响，Blockdustry 无昼夜光照系统，本轮简化为恒定产电；若后续要复刻昼夜变化需接入时间/光照系统，非本任务范围喵
- 侧面/底面用顶视贴图（与 combustion_generator/laser_drill 等现有建筑一致），视觉上侧面显示俯视纹理属既有惯例喵

## 五、异常喵
无（自身无异常；编译失败归因并发任务，见上）。
