# ^P1 批1D blast-drill 爆破钻头整合清单喵

> 任务登记: `D:\Blockdustry\任务\T34_爆破钻头.md`。机制/数据忠于 Mindustry v8 原版，贴图拷原版 PNG 不重绘喵。

## 零、数据核对结论（先纠偏，数据不串最高要求）喵
- Mindustry 实测 `Blocks.java L2900-2919`：`blastDrill = new Drill("blast-drill")`，**size = 4**（4×4 占地，128×128 贴图；任务描述称 size 2 有误，laser 子任务同样纠正过——blast-drill 是 laser-drill 上位，原版就是 4×4）。drillTime=280、tier=5、drawRim=true、hasPower、updateEffect=Fx.pulverizeRed(0.03)、drillEffect=Fx.mineHuge、rotateSpeed=6、warmupSpeed=0.01、itemCapacity=20、liquidBoostIntensity=1.8、consumePower(3f)、requirements=铜65 硅60 钛50 钍75、researchCost 无显式（默认=配方）。
- **爆破特效结论（须向协调者确认）**：原版 Drill 类**没有「爆炸产生额外矿」机制**，全库 grep blastDrill 仅出现于 Blocks.java/SerpuloTechTree.java。「爆破」= 纯视觉：`Fx.pulverizeRed`（红色碎屑，钻时 0.03×warmup 概率）+ `drawRim`（红色发光 rim，additive）+ `Fx.mineHuge`（出矿大碎屑）。本实现已忠实迁移这三种特效，未自创额外爆炸。若用户坚持要「爆炸产出额外矿」，需协调者确认后另行设计（原版无此机制）。
- 中文名官方 bundle：`block.blast-drill.name = 爆破钻头`（bundle_zh_CN.properties L2027），en = Airblast Drill。描述：终极钻头，需要大量电力。

## 一、已交付（独立新文件，已含渲染，主会话勿重复注册）喵

| 文件 | 说明 |
|---|---|
| `src/main/java/com/blockdustry/building/BlastDrillRegistrar.java` | 自包含注册类（模板 FuseArcRegistrar/SorterRegistrar）：BLAST_DRILL 方块+物品+实体；size 4、strength 3.8（→单格血 48、组血 768 = 原版，钍需求 healthScaling 0.2 → 16×40×1.2=768；T50 审查修正，原写 strength 6/组血 1280 有误）喵 |
| `src/main/java/com/blockdustry/building/BlastDrillBlock.java` | **4×4 方块（自定义）**：基础类 Corner 枚举只支持 1/2/3 格，4×4 需 16 格无法用 corner 区分，故覆写 getShape/getCollisionShape 按「本格相对锚点的 dx/dz」直接算整组包围盒，绕开 corner 限制喵 |
| `src/main/java/com/blockdustry/building/BlastDrillBuildingItem.java` | 4×4 多格放置物品：覆写 place 预检 4×4 下方有矿（Mindustry Drill.canPlaceOn 语义）喵 |
| `src/main/java/com/blockdustry/building/BlastDrillBlockEntity.java` | 忠实移植 Drill.DrillBuild（与 laser-drill 对齐）：countOre 取 dominant 矿、progress/warmup/timeDrilled、getDrillTime=(280+50×硬度)、产矿 dumpItem→storeItem 兜底、BlockdustryPowerNode 耗电 3（仅锚点格计入，getPowerLinks 非锚点并入锚点网）、Fx.pulverizeRed→红色 Dust 粒子、Fx.mineHuge→ITEM 粒子携带矿石、warmup/spin/dominant 客户端同步喵 |
| `src/main/java/com/blockdustry/client/BlastDrillBlockEntityRenderer.java` | 4×4 单层建筑 BER 画整组：基座 box（顶面 blast_drill.png + 四面深灰）、rim 红色发光（additive 用 entityTranslucent 近似，alpha=warmup×0.6×(0.7+\|sin(time×2π/3)\|×0.3)）、rotator 旋转钻头（角度=timeDrilled×6）、top 顶板、mine item（矿色 tint）；getRenderBoundingBox 扩 4×4 防余光剔除；全亮+NO_OVERLAY（坑/BER渲染.md §1）；叠画 y 偏移防共面（坑 §3）喵 |
| `assets/blockdustry/blockstates/blast_drill.json` | 全 9 corner 变体 → 同一空模型（4×4 视觉全由 BER 画）喵 |
| `assets/blockdustry/models/block/blast_drill.json` | 空模型（particle=blast_drill）喵 |
| `assets/blockdustry/models/item/blast_drill.json` | cube_all（物品栏图标）喵 |
| `assets/blockdustry/textures/block/blast_drill.png` | 拷原版 `drills/blast-drill.png`（128×128）喵 |
| `assets/blockdustry/textures/block/blast_drill_rim.png` | 拷原版 `blast-drill-rim.png`（128×128）喵 |
| `assets/blockdustry/textures/block/blast_drill_rotator.png` | 拷原版 `blast-drill-rotator.png`（128×128）喵 |
| `assets/blockdustry/textures/block/blast_drill_top.png` | 拷原版 `blast-drill-top.png`（128×128）喵 |
| `assets/blockdustry/textures/block/blast_drill_item.png` | 拷原版 `drill-item-4.png`（128×128，原版 Drill.itemRegion fallback `drill-item-@size`）喵 |
| `assets/blockdustry/textures/research/blocks/blast_drill.png` | 科技树图标（拷原版 blast-drill.png）喵 |

> 注：并行 laser-drill 子任务曾对我 `building/BlastDrillBlock.java` 补 2 个缺失 import（Block/BlockEntityType），属编译必需，已确认无逻辑改动喵。

## 二、主会话挂载点（按序合并，全部必须）喵

### 1. register — `Blockdustry.java` 构造器
在 `com.blockdustry.distribution.BridgeRegistrar.register(modEventBus);` 之后加一行：
```java
com.blockdustry.building.BlastDrillRegistrar.register(modEventBus);
```

### 2. tab — `BlockdustryBlocks.java` CRAFTING_TAB（Category.production 归锻造 tab）
在 `output.accept(GRAPHITE_PRESS_ITEM);` 之后加一行：
```java
output.accept(com.blockdustry.building.BlastDrillRegistrar.BLAST_DRILL_ITEM);
```

### 3. ResearchNodes — `ResearchNodes.java` all()，加 1 节点
parent=laser_drill（原版 SerpuloTechTree L110-112：laserDrill→blastDrill；laser_drill 节点由批1D laser 子任务提供，已存在）喵：
```java
ResearchNode.builder("blast_drill")
        .parent("laser_drill")            // Mindustry SerpuloTechTree: laserDrill → blastDrill 喵
        .unlockBlock(com.blockdustry.building.BlastDrillRegistrar.BLAST_DRILL.get())
        .buildRequirement(copper, 65)     // Mindustry blast-drill = 铜×65 硅×60 钛×50 钍×75 喵
        .buildRequirement(silicon, 60)
        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 50)
        .buildRequirement(com.blockdustry.item.BlockdustryItems.THORIUM.get(), 75)
        .build(),
```

### 4. 渲染 — `BlockdustryClient.java` registerRenderers
在 laser 渲染器行之后加一行：
```java
event.registerBlockEntityRenderer(com.blockdustry.building.BlastDrillRegistrar.BLAST_DRILL_ENTITY.get(), com.blockdustry.client.BlastDrillBlockEntityRenderer::new);
```

### 5. ResearchIcons — `ResearchIcons.java` nodeTexture
在 `case "laser_drill" -> ...` 之后加一行：
```java
case "blast_drill" -> ResourceLocation.tryParse(base + "blast_drill.png");
```

### 6. 多格组血量 — `BlockdustryBlocks.java` registerBlockHealthDefaults
在 laser 行之后加一行：
```java
registerGroupMaxHp(com.blockdustry.building.BlastDrillRegistrar.BLAST_DRILL.get(), 4); // blast-drill 4×4 组血 768（strength 3.8 → 单格 48，16 格 = 原版 16×40×1.2）喵
```

### 7. lang — `lang/en_us.json` + `lang/zh_cn.json` 追加
en_us:
```json
"block.blockdustry.blast_drill": "Airblast Drill"
```
zh_cn:
```json
"block.blockdustry.blast_drill": "爆破钻头"
```
（官方 bundle_zh_CN `block.blast-drill.name = 爆破钻头`，en `Airblast Drill` 喵）

### 8.（可选）Jade 钻速/预热条 — `ProgressServerProvider.java` getGroups
在 `info instanceof UnitFactoryBlockEntity` 块后加（Mindustry Drill.setBars 的 drillspeed 条，值取 warmup）喵：
```java
if (info instanceof com.blockdustry.building.BlastDrillBlockEntity bd) {
    groups.add(new ViewGroup<>(List.of(ProgressView.create(bd.getWarmup())), Optional.of(ID_CRAFT), Optional.empty()));
}
```

## 三、行为要点（验收对照）喵
- 放置：4×4 占地（整组碰撞箱 + 组血 768），放置预检 4×4 下方有矿（复用 DrillBlockEntity.oreResult）喵
- 耗电：无电不挖（powerStatus≤0.01 停摆、warmup 衰减、钻头停转、rim 熄灭），接电力节点供电后 warmup 爬升（0.01/tick）喵
- 挖掘：统计 4×4 下方各矿数量取 dominant；progress 按 dominantCount 加速；产速按原版 getDrillTime=(280+50×矿石硬度)；产出优先 dumpItem 卸邻居、满则积存（itemCapacity=20，覆写 getCapacity/isFull）喵
- 特效（爆破核心，全部忠实原版）：
  - pulverizeRed 红色碎屑粒子（0.03×warmup 概率，建筑中心 ±1 格随机）
  - rim 红色发光（additive 用 entityTranslucent 近似，alpha 随 warmup 脉冲）
  - rotator 旋转钻头（角度=timeDrilled×6）
  - mineHuge ITEM 粒子携带矿石（出矿时 0.02×warmup）
  - mine item 层染矿色
- 科技树：laser_drill 下新节点「爆破钻头」；创造栏锻造 tab 出现喵

## 四、风险 / 待人工排查喵
- **size=4 与基础类 Corner 系统**：基础类只支持 1/2/3 格，4×4 用自定义 BlastDrillBlock 覆写 shape 解决（blockstate 9 corner 全指向空模型，视觉全由 BER 画）。若后续统一把基础类扩到 4×4（Corner 扩 16 值），可改回标准实现喵
- **液体 boost 未实现**：原版 consumeLiquid(water 0.1).boost（1.8×）——mod 无液体系统，与 laser/pneumatic 一致本轮不做喵
- **rim 发光用 entityTranslucent 近似 additive**：原版 Blending.additive 会加亮底色，entityTranslucent 正常混叠略暗；如需真 additive 需自建 RenderType（AdditiveStateShard），可后续单开喵
- **科技树链协调**：原版链 mechanical→graphitePress→pneumatic→laser→blast；laser 子任务把 laser_drill 挂在 graphite_press 下（写时 pneumatic 未完成），pneumatic 子任务把 pneumatic_drill 也挂 graphite_press 下 → 现 laser 与 pneumatic 成平行兄弟。blast_drill 正确挂在 laser_drill 下。**建议主会话把 laser_drill 父改到 pneumatic_drill**（更贴近原版链），blast_drill 保持挂 laser_drill 喵
- 矿石硬度映射（RAW_IRON=2/RAW_GOLD=1/DIAMOND=3/EMERALD=3/COAL=2/其余=1）为 Mindustry 硬度体系对 MC 矿石的近似，与 laser 一致喵

## 五、异常喵
无。
