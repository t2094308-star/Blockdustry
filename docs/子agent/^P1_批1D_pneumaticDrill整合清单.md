# ^P1 批1D pneumaticDrill（气动钻头）整合清单喵

> 主会话按此清单挂载。数据全部核对 Mindustry 原版：`Blocks.java` L2878-2885（pneumatic-drill）+ `Drill.java`（钻机基类）+ `SerpuloTechTree.java` L104-105（父节点 graphitePress）+ `bundle_zh_CN.properties` L1987（官方名「气动钻头」）喵。
> 定位：mechanical-drill 的**上位进阶钻机**（Drill 类），不是 laser/blast-drill。参数独有：tier=3、drillTime=400、size=2、耗水 3.5/60·s、造价 铜×18+石墨×10 喵。

## 一、新文件（子 agent 已写，主会话勿改）喵

**Java（包 `com.blockdustry.building`）喵**
- `src/main/java/com/blockdustry/building/PneumaticDrillRegistrar.java` — 自包含注册类（模板 FuseArcRegistrar），方块+物品+BE 喵
- `src/main/java/com/blockdustry/building/PneumaticDrillBlockEntity.java` — 钻机 BE（drillTime 27 tick、warmup、dominant 矿石、pulverizeSmall/Fx.mine 粒子）喵

**Java（包 `com.blockdustry.client`）喵**
- `src/main/java/com/blockdustry/client/PneumaticDrillBlockEntityRenderer.java` — rotator（旋转）+ top（顶盖）+ item（矿色 tint）三层渲染喵

**资源（assets/blockdustry）喵**
- `blockstates/pneumatic_drill.json` — 9 corner 变体（2×2 四模型复用，nw 占 n/w/c）喵
- `models/block/pneumatic_drill_{nw,ne,sw,se}.json` — 顶面四分之一裁剪（uv [0,0,8,8]/[8,0,16,8]/[0,8,8,16]/[8,8,16,16]），side 用 pneumatic_drill_side 喵
- `models/item/pneumatic_drill.json` — cube_all 喵
- `textures/block/pneumatic_drill.png`、`pneumatic_drill_rotator.png`、`pneumatic_drill_top.png`、`pneumatic_drill_item.png`（drill-item-2）、`pneumatic_drill_side.png` — 全部拷 Mindustry 原版 PNG（64×64，像素级一致未重绘）喵
- `textures/research/blocks/pneumatic_drill.png`、`pneumatic_drill_top.png` — 科技树图标（原版拷贝）喵

**文档/片段**
- `docs/子agent/^P1_批1D_pneumaticDrill_lang_zh.json`、`..._lang_en.json` — lang 片段（官方名）喵
- `D:\Blockdustry\任务\T32_气动钻头.md` — 任务登记喵

## 二、主会话挂载点（精确）喵

### 1. `Blockdustry.java` 构造器（register）喵
在 `com.blockdustry.distribution.BridgeRegistrar.register(modEventBus);` 后加：
```java
import com.blockdustry.building.PneumaticDrillRegistrar;
...
PneumaticDrillRegistrar.register(modEventBus);
```

### 2. `BlockdustryBlocks.java` — 锻造/生产 tab（CRAFTING_TAB）喵
`CRAFTING_TAB` 的 `displayItems` 内（`output.accept(GRAPHITE_PRESS_ITEM);` 后）加：
```java
output.accept(com.blockdustry.building.PneumaticDrillRegistrar.PNEUMATIC_DRILL_ITEM);
```

### 3. `BlockdustryBlocks.java` — registerBlockHealthDefaults() 组血量喵
在 `registerGroupMaxHp(GRAPHITE_PRESS.get(), 2);` 后加（pneumatic 与 mechanical 同 size 2、同强度 3f，组血公式一致）：
```java
registerGroupMaxHp(com.blockdustry.building.PneumaticDrillRegistrar.PNEUMATIC_DRILL.get(), 2);
```

### 4. `BlockdustryClient.java` — registerRenderers 加 1 行喵
在 `event.registerBlockEntityRenderer(...BridgeRegistrar.BRIDGE_ENTITY.get()...);` 附近加：
```java
event.registerBlockEntityRenderer(com.blockdustry.building.PneumaticDrillRegistrar.PNEUMATIC_DRILL_ENTITY.get(), com.blockdustry.client.PneumaticDrillBlockEntityRenderer::new);
```

### 5. `ResearchNodes.java` — 1 节点 parent=graphite_press 喵
在 `all()` 的 `graphite_press` 节点下、`combustion_generator` 节点附近追加（配方照抄 Blocks.java L2879：copper×18 + graphite×10；原版无显式 researchCost → 默认=配方；SerpuloTechTree 里 pneumatic-drill 父链为 graphitePress，需先研究 frozenForest 的 objective 由主会话按既有模式处理）喵：
```java
ResearchNode.builder("pneumatic_drill")
        .parent("graphite_press")     // Mindustry SerpuloTechTree: graphitePress → pneumaticDrill 喵
        .unlockBlock(PneumaticDrillRegistrar.PNEUMATIC_DRILL.get())
        .buildRequirement(copper, 18) // Mindustry pneumatic-drill = 铜×18 石墨×10 喵
        .buildRequirement(graphite, 10)
        .build(),
```
（`copper = Items.COPPER_INGOT`、`graphite = BlockdustryBlocks.GRAPHITE.get()` 已在 all() 顶部定义；需 import `com.blockdustry.building.PneumaticDrillRegistrar`）喵

### 6. `ResearchIcons.java` — nodeTexture 加 1 case + 顶盖叠加喵
`nodeTexture` 的 switch 里加：
```java
case "pneumatic_drill" -> ResourceLocation.tryParse(base + "pneumatic_drill.png");
```
`drawNodeIcon` 的 drill 顶盖特判改为同时覆盖 pneumatic（drill 用既有 drillTopTexture()，pneumatic 用 pneumatic_drill_top.png）：
```java
String path = node.id.getPath();
if (path.equals("drill") || path.equals("pneumatic_drill")) {
    ResourceLocation top = path.equals("drill")
            ? drillTopTexture()
            : ResourceLocation.tryParse("blockdustry:textures/research/blocks/pneumatic_drill_top.png");
    if (top != null) drawScaled(g, top, x, y, box, box);
}
```

### 7. `ProgressServerProvider.java` — drillspeed/warmup 进度条喵
`getGroups` 里（`info instanceof UnitFactoryBlockEntity uf` 块后）加（Mindustry Drill.setBars 的 drillspeed 条，值取 warmup）：
```java
if (info instanceof com.blockdustry.building.PneumaticDrillBlockEntity pd) {
    groups.add(new ViewGroup<>(List.of(ProgressView.create(pd.getWarmup())), Optional.of(ID_CRAFT), Optional.empty()));
}
```

### 8. lang 合并喵
把 `^P1_批1D_pneumaticDrill_lang_zh.json` / `_lang_en.json` 并入 `lang/zh_cn.json` / `lang/en_us.json`：
```json
"block.blockdustry.pneumatic_drill": "气动钻头"
```
```json
"block.blockdustry.pneumatic_drill": "Pneumatic Drill"
```
（中英均取 Mindustry 官方 bundle：block.pneumatic-drill.name = 气动钻头 / Pneumatic Drill；可选加描述「一种改进的钻头，能开采钛。采矿速度比机械钻头快。/ An improved drill, capable of mining titanium. Mines at a faster pace than a mechanical drill.」）喵

## 三、机制说明（忠于 Mindustry Drill / pneumatic-drill）喵

- **钻速**：原版 mechanical drillTime=600 / pneumatic=400（气动 1.5 倍速）。现模 mechanical 门槛 40 tick，pneumatic 取 `400/600×40 ≈ 26.67 → 27 tick`，忠实相对钻速喵。
- **tier=3**：开采判定复用现有 `DrillBlockEntity.oreResult`（同包静态方法，未改机械钻），可采 MC 全部主世界矿石（含钻石/绿宝石/红石/青金石）喵。
- **warmup 预热**：Drill.warmupSpeed=0.015，随开采爬升、停采衰减；驱动旋转速度与粒子频率（渲染器 `angle = gameTime×0.3×warmup`，warmup 经 block update 同步，变化>0.02 才发包，GraphitePress 同款）喵。
- **动画/特效**（原版 Drill.draw() 全要素迁移）：
  - 底座 region = 方块模型顶面（pneumatic_drill.png 四象限拼 2×2）喵
  - rotator 旋转 = BER 旋转平面（pneumatic_drill_rotator.png）喵
  - top 顶盖 = BER 静态平面（pneumatic_drill_top.png）喵
  - item 矿团 = BER 矿色 tint（pneumatic_drill_item.png = 原版 drill-item-2）喵
  - updateEffect=pulverizeSmall → 灰 DustParticle（钻时 0.02×warmup 概率）喵
  - drillEffect=Fx.mine → 矿色 DustParticle（出矿时播，GraphitePress「每产出播一次」惯例，保证可见；原版 drillEffectChance=0.02 触发较隐蔽）喵
  - drawRim=false（heat rim 为 blast-drill 专属，pneumatic 无）喵

## 四、占用与交接喵
- 占用文件：见 `D:\Blockdustry\任务\T32_气动钻头.md`（仅新文件，无共享文件写入）喵
- 交接给：主会话（按本清单 8 处挂载 + `./gradlew compileJava` + runClient 冒烟）喵
- 风险/待人工排查：
  - **水 boost 未实现**：原版 `consumeLiquid(water 3.5/60).boost()`（液体增压 1.6x）——mod 无液体系统，机械钻同样未实现；待液体系统就绪后补喵
  - **放置矿石检查缺失**：`BlockdustryBuildingItem.place()` 的「须放在矿石上」硬编码只判 `getBlock() == BlockdustryBlocks.DRILL.get()`，pneumatic 放置不强制有矿；主会话可顺手在该处加 `|| getBlock() == PneumaticDrillRegistrar.PNEUMATIC_DRILL.get()`（共享文件，本 agent 未改）喵
  - **侧面贴图**：pneumatic_drill_side.png 直接用了原版顶视 pneumatic-drill.png（不透明，可正常当侧面）；任务约束「不重绘」故未自绘侧脸，若需机械钻 T6d 同款定制侧面，可后续单开任务喵
  - **dominant 计数加速未实现**：原版 progress 随 dominantItems（矿格数）加速，现按机械钻骨架 `progress += warmup`（不乘矿格数），保证 1.5 倍相对钻速稳定；多矿格加速可后续细化喵
  - **item 显示扫描**：渲染器 `getMinedOre()` 客户端实时扫下方方块算 dominant（与 BE 服务端逻辑一致）；新放置未同步锚点前可能短暂显示偏差，正常喵

## 五、异常喵
无。
