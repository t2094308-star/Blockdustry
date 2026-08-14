# ^P1 批1D laser-drill 整合清单 —— 激光钻头迁移喵

> 任务登记: `D:\Blockdustry\任务\T33_激光钻头.md`。机制/数据忠于 Mindustry v8 原版，贴图拷原版（md5 校验一致）不重绘喵。

## 零、数据核对结论（先纠偏任务描述，数据不串最高要求）喵
- Mindustry 实测 `Blocks.java L2887-2898`：`laserDrill = new Drill("laser-drill")`，**size = 3**（任务描述称 size 2 有误；blast-drill 才 size 4、pneumatic-drill size 3——严禁套用他机参数，以原版 L2890 `size=3` 为准实现）。drillTime=280、tier=4、consumePower(1.10f)、requirements=铜35 石墨30 硅30 钛20、drillEffect=Fx.mineBig、updateEffect=Fx.pulverizeMedium、consumeLiquid(water,0.08f).boost()。
- **激光光束视觉（用户最高要求，须向协调者确认）**：Mindustry v8 全库 grep 确认**不存在 LaserDrill 类**（旧版已移除），laser-drill 即普通 `Drill` 类，`Drill.draw()` 只画 base→裂纹→rotator(旋转)→top→item，**无独立激光光束绘制**。原版「激光」视觉 = 底座贴图 `laser-drill.png` 上的**中央深色激光窗 + 紫色激光能量装饰**（像素统计：亮紫 191,146,249 / 暗紫 102,92,159）+ rotator 旋转钻头 + top 盖。按协作规则「找不到原版特效绝不瞎编」：**本实现不添加原版没有的激光光束**，忠实迁移原版全部实际视觉（贴图+rotator+top+Fx 粒子）。若需额外光束动画，请协调者决策后再加。
- 中文名用官方 bundle：`block.laser-drill.name = 激光钻头`（bundle_zh_CN.properties L1988），en = Laser Drill。描述：通过激光技术更快地开采，但需要电力，可开采钍。

## 一、已交付（独立新文件，已含渲染，主会话勿重复注册）喵

| 文件 | 说明 |
|---|---|
| `src/main/java/com/blockdustry/building/LaserDrillRegistrar.java` | 自包含注册类（模板 FuseArcRegistrar/SorterRegistrar）：LASER_DRILL 方块+物品+实体；size 3、strength 3（→单格血 40、组血 360，Mindustry health 比例与 mechanical 一致）喵 |
| `src/main/java/com/blockdustry/building/LaserDrillBlock.java` | 3×3 方块（继承 BlockdustryBuildingBlock，Corner 9 宫格）喵 |
| `src/main/java/com/blockdustry/building/LaserDrillBuildingItem.java` | 多格放置物品：覆写 place 预检 3×3 下方有矿（Mindustry Drill.canPlaceOn 语义）喵 |
| `src/main/java/com/blockdustry/building/LaserDrillBlockEntity.java` | 忠实移植 Drill.DrillBuild：countOre 取 dominant 矿、progress/warmup/timeDrilled、getDrillTime=(280+50×硬度)、产矿 dumpItem→storeItem 兜底、BlockdustryPowerNode 耗电 1.10、Fx.mineBig→ITEM 粒子、Fx.pulverizeMedium→CRIT 粒子、warmup/spin 客户端同步喵 |
| `src/main/java/com/blockdustry/client/LaserDrillBlockEntityRenderer.java` | 锚点格画 rotator 旋转钻头（laser_drill_rotator.png，角度=timeDrilled×2）+ top 盖（laser_drill_top.png）；getRenderBoundingBox 扩 3×3 防余光剔除（坑/碰撞箱.md §3）；全亮+NO_OVERLAY（坑/BER渲染.md §1）喵 |
| `assets/blockdustry/blockstates/laser_drill.json` | 全 9 corner 变体 → 9 个象限模型（3×3）喵 |
| `assets/blockdustry/models/block/laser_drill.json` | base 模型（全顶面，供物品栏展示）喵 |
| `assets/blockdustry/models/block/laser_drill_{nw,n,ne,w,c,e,sw,s,se}.json` | 3×3 九宫格顶面裁剪模型（UV 每格 5.333/16，模板 unit_factory）喵 |
| `assets/blockdustry/models/item/laser_drill.json` | 父=block/laser_drill 喵 |
| `assets/blockdustry/textures/block/laser_drill.png` | 拷原版 `drills/laser-drill.png`（96×96，md5 4c8356c1 与原版一致）喵 |
| `assets/blockdustry/textures/block/laser_drill_rotator.png` | 拷原版 `laser-drill-rotator.png`（96×96，md5 8c072044 一致）喵 |
| `assets/blockdustry/textures/block/laser_drill_top.png` | 拷原版 `laser-drill-top.png`（96×96，md5 4a3133ce 一致）喵 |
| `assets/blockdustry/textures/research/blocks/laser_drill.png` | 科技树图标（拷原版 laser-drill.png）喵 |

> 注：为让整体编译通过，对并行 blast-drill 子任务的文件 `building/BlastDrillBlock.java` 补了 2 个缺失 import（`net.minecraft.world.level.block.Block`、`BlockEntityType`），未改逻辑，属编译必需的最小修复，请协调者知会该 agent 喵。

## 二、主会话挂载点（按序合并，全部必须）喵

### 1. register — `Blockdustry.java` 构造器
在 `com.blockdustry.distribution.BridgeRegistrar.register(modEventBus);` 之后加一行：
```java
com.blockdustry.building.LaserDrillRegistrar.register(modEventBus);
```

### 2. tab — `BlockdustryBlocks.java` CRAFTING_TAB（Category.production 归锻造 tab）
在 `output.accept(GRAPHITE_PRESS_ITEM);` 之后加一行：
```java
output.accept(com.blockdustry.building.LaserDrillRegistrar.LASER_DRILL_ITEM);
```

### 3. ResearchNodes — `ResearchNodes.java` all()，加 1 节点
parent=graphite_press（原版 SerpuloTechTree L110：pneumaticDrill→laserDrill，pneumaticDrill 挂在 graphitePress 下；Blockdustry 无 pneumaticDrill，挂 graphite_press 最贴近原版链 mechanical→graphitePress→pneumatic→laser）喵：
```java
ResearchNode.builder("laser_drill")
        .parent("graphite_press")
        .unlockBlock(com.blockdustry.building.LaserDrillRegistrar.LASER_DRILL.get())
        .buildRequirement(copper, 35)       // Mindustry laser-drill = 铜×35 石墨×30 硅×30 钛×20 喵
        .buildRequirement(graphite, 30)
        .buildRequirement(silicon, 30)
        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 20)
        .build(),
```
（若 all() 顶部局部变量区需补 `Item titanium = com.blockdustry.item.BlockdustryItems.TITANIUM.get();` 也可写成 `titanium`）

### 4. 渲染 — `BlockdustryClient.java` registerRenderers
在 bridge 渲染器行之后加一行：
```java
event.registerBlockEntityRenderer(com.blockdustry.building.LaserDrillRegistrar.LASER_DRILL_ENTITY.get(), com.blockdustry.client.LaserDrillBlockEntityRenderer::new);
```

### 5. ResearchIcons — `ResearchIcons.java` nodeTexture
在 `case "bridge_conveyor" -> ...` 之后加一行：
```java
case "laser_drill" -> ResourceLocation.tryParse(base + "laser_drill.png");
```
（若希望科技树图标叠顶盖，可仿 drill 在 drawNodeIcon 的 `if (node.id.getPath().equals("drill"))` 处追加 `|| node.id.getPath().equals("laser_drill")` 并加 `drillTopTexture` 对应 laser_drill_top；不叠亦可，laser-drill 底座贴图本身已含激光窗/紫色装饰喵）

### 6. 多格组血量 — `BlockdustryBlocks.java` registerBlockHealthDefaults
在 container 行之后加一行：
```java
registerGroupMaxHp(com.blockdustry.building.LaserDrillRegistrar.LASER_DRILL.get(), 3); // laser-drill 3×3 组血 360（strength 3 → 单格 40，9 格）喵
```

### 7. lang — `lang/en_us.json` + `lang/zh_cn.json` 追加
en_us:
```json
"block.blockdustry.laser_drill": "Laser Drill"
```
zh_cn:
```json
"block.blockdustry.laser_drill": "激光钻头"
```
（官方 bundle_zh_CN `block.laser-drill.name = 激光钻头`，en `Laser Drill` 喵）

## 三、行为要点（验收对照）喵
- 放置：3×3 占地（九宫格跨格模型 + 组碰撞箱 + 组血 360），放置预检 3×3 下方有矿（复用 DrillBlockEntity.oreResult，同 mechanical 可挖矿表）喵
- 耗电：无电不挖（powerStatus≤0.01 停摆 warmup 衰减、钻头停转），接电力节点供电后 warmup 爬升（0.015/tick）喵
- 挖掘：统计 3×3 下方各矿数量，取 dominant；progress 按 dominantCount 加速；产速按原版 getDrillTime=(280+50×矿石硬度)；产出优先 dumpItem 卸给邻居、满则积存（itemCapacity=10）喵
- 特效：rotator 旋转钻头（角度=timeDrilled×2）+ top 盖（BER）；工作时 CRIT 灰粉尘（Fx.pulverizeMedium 0.02）；产矿时 ITEM 粒子携带矿石物品（Fx.mineBig 0.02）喵
- 科技树：graphite_press 下新节点「激光钻头」；创造栏锻造 tab 出现喵

## 四、风险 / 待人工排查喵
- **激光光束视觉**：原版当前版本确无光束动画，本实现未自创光束（见「零」节）。若用户坚持要"顶部射激光"视觉效果，需协调者确认后按旧版 LaserDrill/BeamDrill 视觉设计追加（BeamDrill 为侧面打墙矿，非顶部）喵
- 液体 boost（water 0.08 boost 2.56×）未做：Blockdustry 无液体注入系统，本轮按无 boost 实现（与现有 mechanical-drill 一致）喵
- 矿石硬度映射（RAW_IRON=2/RAW_GOLD=1/DIAMOND=3/EMERALD=3/COAL=2/其余=1）为 Mindustry 硬度体系对 MC 矿石的近似，无官方映射，可后续调喵
- 贴图 96×96 非 2 的幂，MC atlas 已支持；若实际渲染异常可缩 64×64（最近邻）喵
- 主会话合并后请跑 `./gradlew compileJava` + 游戏冒烟：创造栏锻造 tab 出现「激光钻头」、放置 3×3、接电力节点挖矿、科技树 graphite_press 下新节点、Jade 显示库存喵
