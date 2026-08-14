# ^P1_批1E diode + surge-tower 整合清单喵

> 本任务产出：二极管（diode，1×1 单向导电）+ 涌电塔（surge-tower，原版 **size=2** 远距电力节点）喵。
> 数据不串：全部参数取自 Mindustry Blocks.java L2494-2504 + PowerDiode/PowerNode 源码，未套用其他电力建筑喵。
> surge-tower 无闪电特效（原版就是标准 PowerNode 激光），本任务只忠实实现激光连接线，不造闪电（见光效研究文档）喵。

## 核心数据库登记（主会话整合时统一处理）喵

- **diode**：Mindustry 注册名 `diode`；中文官方名「二极管」；类别 电力；依赖 电力（size 1，rotate，无液体）→ **从未迁移移到已迁移**喵
- **surge-tower**：Mindustry 注册名 `surge-tower`；中文官方名「巨浪电力塔」（task 文案「涌电塔」为俗称，bundle 官方名是巨浪电力塔）；类别 电力；依赖 电力（size 2，无液体）→ **从未迁移移到已迁移**喵

## 一、主会话挂载点（精确到文件/行）喵

### 1. `Blockdustry.java` 构造器（register 挂载）喵
在 `FuseArcRegistrar.register(modEventBus);` 附近（约 L53 后）加一行喵：
```java
com.blockdustry.building.DiodeSurgeTowerRegistrar.register(modEventBus);
```
- `DiodeSurgeTowerRegistrar` 已自包含：BLOCKS/ITEMS/BLOCK_ENTITY_TYPES 三个 DeferredRegister 一并注册喵。

### 2. `BlockdustryClient.java` registerRenderers（约 L84 后）喵
```java
event.registerBlockEntityRenderer(com.blockdustry.building.DiodeSurgeTowerRegistrar.DIODE_ENTITY.get(), com.blockdustry.client.DiodeBlockEntityRenderer::new);
event.registerBlockEntityRenderer(com.blockdustry.building.DiodeSurgeTowerRegistrar.SURGE_TOWER_ENTITY.get(), com.blockdustry.client.SurgeTowerBlockEntityRenderer::new);
```

### 3. `ResearchNodes.java` all()（在 battery 节点后，约 L177 后）喵
```java
ResearchNode.builder("diode")
        .parent("power_node")          // 原版 parent=powerNodeLarge（未迁移），暂挂 power_node，powerNodeLarge 落地后改挂它喵
        .unlockBlock(com.blockdustry.building.DiodeSurgeTowerRegistrar.DIODE.get())
        .buildRequirement(BlockdustryBlocks.SILICON.get(), 10)   // Mindustry diode = 硅10 塑钢5 钢化玻璃10 喵
        .buildRequirement(com.blockdustry.item.BlockdustryItems.PLASTANIUM.get(), 5)
        .buildRequirement(com.blockdustry.item.BlockdustryItems.METAGLASS.get(), 10)
        .build(),
ResearchNode.builder("surge_tower")
        .parent("diode")               // 原版 parent=diode（SerpuloTechTree L207-208）喵
        .unlockBlock(com.blockdustry.building.DiodeSurgeTowerRegistrar.SURGE_TOWER.get())
        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 7)   // surge-tower = 钛7 铅10 硅15 涌电合金15 喵
        .buildRequirement(BlockdustryBlocks.LEAD.get(), 10)
        .buildRequirement(BlockdustryBlocks.SILICON.get(), 15)
        .buildRequirement(com.blockdustry.item.BlockdustryItems.SURGE_ALLOY.get(), 15)
        .build(),
```
- 研究图标：item model 已写（`models/item/{diode,surge_tower}.json`），`unlockBlock` 自动取方块物品图标，无需额外 ResearchIcons 注册喵。

### 4. `BlockdustryBlocks.java` registerBlockHealthDefaults()（组血量，约 L396 后）喵
```java
registerGroupMaxHp(com.blockdustry.building.DiodeSurgeTowerRegistrar.SURGE_TOWER.get(), 2); // 批1E 涌电塔 2×2 组血喵
```
- diode 为 1×1，无需组血喵。
- surge-tower 整组共享血量由 `BlockdustryBuildingEntity.registerHealthGroup()` 自动注册（放置时 registerHealthGroupExplicit），此处只设默认组总血喵。

### 5. lang（zh_cn.json / en_us.json）喵
追加（中文名取原版官方 bundle，en 取 bundle 官方英文名）喵：
```json
"block.blockdustry.diode": "二极管",
"block.blockdustry.surge_tower": "巨浪电力塔"
```
```json
"block.blockdustry.diode": "Battery Diode",
"block.blockdustry.surge_tower": "Surge Tower"
```

## 二、本任务新增文件清单（独立新文件，主会话无需改动内容）喵

### Java（`src/main/java/com/blockdustry/`）喵
| 文件 | 说明 |
|---|---|
| `building/DiodeBlock.java` | 1×1 旋转方块，FACING 属性（GateBlock 模式）喵 |
| `building/DiodeBlockEntity.java` | 单向电池转移（原版 PowerDiode.updateTile 公式 + BFS 双向网格聚合）喵 |
| `building/SurgeTowerBlock.java` | 2×2 电力节点方块喵 |
| `building/SurgeTowerBlockEntity.java` | laserRange=40 / maxNodes=2 / autolink / 手动连线喵 |
| `building/DiodeSurgeTowerRegistrar.java` | 自包含注册类（模板 FuseArcRegistrar）喵 |
| `client/DiodeBlockEntityRenderer.java` | 顶面画旋转箭头（diode-arrow.png）喵 |
| `client/SurgeTowerBlockEntityRenderer.java` | 塔顶放射激光光柱（白→琥珀，同 PowerNode 激光法）喵 |
| `power/SurgeTowerInteractHandler.java` | 空手右键涌电塔选中/连接（@EventBusSubscriber 自动挂载，无需主会话注册）喵 |

### 资源（`src/main/resources/assets/blockdustry/`）喵
| 文件 | 说明 |
|---|---|
| `blockstates/diode.json` | 36 变体（9 corner × 4 facing），全指向 diode 模型（箭头 BER 叠加）喵 |
| `blockstates/surge_tower.json` | 9 corner 变体 → 四角模型喵 |
| `models/block/diode.json` | cube_all 用 diode.png 喵 |
| `models/block/surge_tower.json` | 物品预览基础模型（top=surge_tower，side=turret_side）喵 |
| `models/block/surge_tower_{nw,ne,sw,se}.json` | 2×2 象限顶面 UV 模型喵 |
| `models/item/{diode,surge_tower}.json` | 物品模型喵 |
| `textures/block/{diode,diode_arrow,surge_tower}.png` | 拷原版 PNG 不重绘（哈希 7f7f58ab / cae719a0 / c80e39d9）喵 |

### 文档喵
| 文件 | 说明 |
|---|---|
| `docs/子agent/^P1_批1E_diodeSurgeTower光效研究.md` | surge-tower 无闪电特效的原版研究结论喵 |
| `docs/子agent/^P1_批1E_diodeSurgeTower_lang_{zh,en}.json` | lang 片段喵 |
| `D:\Blockdustry\任务\T42_1E_C_二极管涌电塔.md` | 任务登记喵 |

## 三、数据核对（原版 vs 实现）喵

| 参数 | 原版 | Blockdustry |
|---|---|---|
| diode 需求 | 硅10+塑钢5+钢化玻璃10 | 同（ResearchNodes）喵 |
| diode 机制 | 后侧电池%>前侧才转移差的一半（单向） | DiodeBlockEntity 同公式 + BFS 双向聚合喵 |
| diode 视觉 | diode.png + diode-arrow.png 随朝向旋转 | 模型 + BER 箭头喵 |
| surge-tower 需求 | 钛7+铅10+硅15+涌电合金15 | 同（ResearchNodes）喵 |
| surge-tower size | **2（2×2）** | SurgeTowerBlock 用 size=2（task 文案 3×3 为误）喵 |
| surge-tower maxNodes | 2 | SurgeTowerBlockEntity.MAX_NODES=2 喵 |
| surge-tower laserRange | 40f | LASER_RANGE=40f 喵 |
| surge-tower 视觉 | 标准 PowerNode 激光（laser.png 白→琥珀） | SurgeTowerBlockEntityRenderer 激光光柱喵 |
| 科技树 | powerNode→powerNodeLarge→diode→surgeTower | power_node→diode→surge_tower（powerNodeLarge 未迁移暂挂 power_node）喵 |

## 四、已知差距 / 风险（如实上报）喵

1. **surge-tower 无闪电特效**：原版就是普通 PowerNode 激光。task 文案「闪电放电特效」在原版不存在，本任务未造闪电。若需「电弧放射」观感属增强，另开子任务（参考 T15 §六 电弧分层方案）喵。
2. **外部普通节点连入 surge-tower 可能略超 maxNodes**：`PowerNodeBlockEntity.linkValid` 硬编码 MAX_NODES=10，不会检查涌电塔自身 maxNodes=2；涌电塔 tick 内自清理自己的 links 维持 2，但外部节点连向非锚点格时不会写回涌电塔 links（反向写回只在涌电塔主动 connect 时发生）。可选修复：主会话改 `PowerNodeBlockEntity.linkValid` 让 `be instanceof BlockdustryPowerNode` 通用检查目标容量喵。
3. **电池覆盖率简化模型**：Blockdustry 电池 status 每 tick 被电网覆盖写回（无持久电量），二极管按该模型允许的电池 status 调整执行原版公式；效果为「后侧覆盖率高时向低侧转移」，符合单向语义，但非 Mindustry 的持久储能语义。如需真持久电池，需重构 PowerGrid 电池模型（另开任务）喵。
4. **二极管 bars**（back/front 电池%）未实现（Blockdustry 无现成 bar 系统）；可选增强，非核心视觉喵。
5. **编译**：`./gradlew compileJava` 本任务文件**零错误**；全工程现存 1 个既有错误（`building/MenderBlockEntity.java:218` NbtUtils.readBlockPos API 不匹配，属其他在途任务，与本任务无关）喵。

## 五、协作交接喵
- 共享注册文件未改动：`Blockdustry.java` / `BlockdustryClient.java` / `ResearchNodes.java` / `BlockdustryBlocks.java` / `lang` 只出上述整合清单，由主会话合并喵。
- 独立新文件均已就位（见上表），主会话合并后重启游戏验证：diode 放置朝向后单向输电、surge-tower 2×2 放置后远距激光连接喵。
