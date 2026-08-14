# ^P1 批2B mender(修理器) + force-projector(力墙投影) 整合清单喵

> 修理器（Mindustry mender，MendProjector size 1）+ 力墙投影（Mindustry force-projector，ForceProjector size 3）迁移完成。本清单供主会话合并喵。
> 数据核对：mender = range 40(=5格) / reload 200 / healPercent 4% / phaseBoost 4 / phaseRangeBoost 20 / useTime 400 / 耗电 0.3 / 吃硅 boost / baseColor #84f491；
> force-projector = radius 101.7(=12.7格) / sides 6 / shieldHealth 750 / phaseShieldBoost 400 / phaseRadiusBoost 80 / cooldownNormal 1.5 / cooldownBrokenBase 0.35 / phaseUseTime 350 / 耗电 4 / 吃相织布 boost。
> 科技树：mender parent=powerNode；force-projector 原版 parent=mend-projector（未迁移，临时挂 mender）喵。

## 1. 新文件（自包含，无需主会话改动内容）喵

| 文件 | 说明 |
|---|---|
| `src/main/java/com/blockdustry/building/MenderRegistrar.java` | 自包含注册类（方块+物品+BE），模板 FuseArcRegistrar；strength 7 → 血量 80 |
| `src/main/java/com/blockdustry/building/MenderBlockEntity.java` | BE：范围维修（BlockHealthApi.heal）、吃硅 boost、耗电 0.3、维修批次特效同步 |
| `src/main/java/com/blockdustry/client/MenderBlockEntityRenderer.java` | 顶部呼吸 + 旋转方框脉冲 + 目标块维修闪烁（healBlockFull 等效） |
| `src/main/java/com/blockdustry/building/ForceProjectorRegistrar.java` | 自包含注册类（方块+物品+BE） |
| `src/main/java/com/blockdustry/building/ForceProjectorBlockEntity.java` | BE：六边形护盾、子弹拦截（反射读伤害）、蓄力/破碎、耗电 4、吃相织布 boost |
| `src/main/java/com/blockdustry/client/ForceProjectorBlockEntityRenderer.java` | 护盾六边形（实心+线框、受击闪白）+ 蓄力 top + 破碎扩散 + 拦截点光点 |
| `src/main/resources/assets/blockdustry/blockstates/mender.json` | blockstate 全 corner 变体（size 1） |
| `src/main/resources/assets/blockdustry/blockstates/force_projector.json` | blockstate 全 9 corner 变体（size 3） |
| `src/main/resources/assets/blockdustry/models/block/mender.json` | 模型（顶面 mender.png 全贴图） |
| `src/main/resources/assets/blockdustry/models/block/force_projector_{nw,n,ne,w,c,e,sw,s,se}.json` | 9 象限模型（顶面按 1/3 裁剪） |
| `src/main/resources/assets/blockdustry/models/item/{mender,force_projector}.json` | 物品模型 |
| `src/main/resources/assets/blockdustry/textures/block/{mender,force_projector,force_projector_team}.png` | 拷原版 PNG 不重绘 |
| `src/main/resources/assets/blockdustry/textures/entity/{mender_top,force_projector_top}.png` | 拷原版顶部光晕贴图 |
| `src/main/resources/assets/blockdustry/textures/research/blocks/{mender,force_projector}.png` | 科技树图标（= 方块贴图） |

## 2. 共享文件挂载点（主会话按此合并，勿动新文件）喵

### 2.1 `Blockdustry.java` 构造器（挂注册类）喵
在 `SiliconSmelterRegistrar.register(modEventBus);` 附近加两行：
```java
com.blockdustry.building.MenderRegistrar.register(modEventBus);
com.blockdustry.building.ForceProjectorRegistrar.register(modEventBus);
```

### 2.2 `BlockdustryClient.java` `registerRenderers`（注册渲染器）喵
在 `PlastaniumCompressorBlockEntityRenderer` 行附近加：
```java
event.registerBlockEntityRenderer(com.blockdustry.building.MenderRegistrar.MENDER_ENTITY.get(), com.blockdustry.client.MenderBlockEntityRenderer::new);
event.registerBlockEntityRenderer(com.blockdustry.building.ForceProjectorRegistrar.FORCE_PROJECTOR_ENTITY.get(), com.blockdustry.client.ForceProjectorBlockEntityRenderer::new);
```

### 2.3 `BlockdustryBlocks.java` CRAFTING_TAB（创造栏）喵
`CRAFTING_TAB` 的 `displayItems` 里 `PLASTANIUM_COMPRESSOR_ITEM` 之后加（原版 Category.effect，本 mod 无 effect tab，放生产辅助 tab）：
```java
output.accept(com.blockdustry.building.MenderRegistrar.MENDER_ITEM);
output.accept(com.blockdustry.building.ForceProjectorRegistrar.FORCE_PROJECTOR_ITEM);
```

### 2.4 `BlockdustryBlocks.java` `registerBlockHealthDefaults`（整组血量）喵
在 `registerGroupMaxHp(BlastDrillRegistrar.BLAST_DRILL.get(), 4);` 后加：
```java
registerGroupMaxHp(com.blockdustry.building.ForceProjectorRegistrar.FORCE_PROJECTOR.get(), 3); // 批2B force-projector 3×3 组血喵
```
（mender 单格不注册组血，血量由 strength 7 → 10+10×7=80 自动）

### 2.5 `ResearchNodes.java` `all()`（科技树）喵
在 `battery` 节点之后加（Mindustry SerpuloTechTree：powerNode → mender → mendProjector → forceProjector）：
```java
ResearchNode.builder("mender")
        .parent("power_node")                 // Mindustry SerpuloTechTree: powerNode → mender 喵
        .unlockBlock(MenderRegistrar.MENDER.get())
        .buildRequirement(lead, 30)           // Mindustry mender = 铅×30 铜×25 喵
        .buildRequirement(copper, 25)
        .build(),
ResearchNode.builder("force_projector")
        .parent("mender")                     // 原版 parent=mend-projector（未迁移），临时挂 mender，待 mend-projector 迁移后改挂 喵
        .unlockBlock(ForceProjectorRegistrar.FORCE_PROJECTOR.get())
        .buildRequirement(lead, 100)          // Mindustry force-projector = 铅×100 钛×75 硅×125 喵
        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 75)
        .buildRequirement(silicon, 125)
        .build(),
```

### 2.6 `ResearchIcons.java` `nodeTexture`（科技树图标）喵
在 `case "plastanium_compressor"` 后加：
```java
case "mender" -> ResourceLocation.tryParse(base + "mender.png");
case "force_projector" -> ResourceLocation.tryParse(base + "force_projector.png");
```

### 2.7 `lang/zh_cn.json` 喵
```json
"block.blockdustry.mender": "修理器",
"block.blockdustry.force_projector": "力墙投影"
```
（官方 bundle_zh_CN：block.mender.name=修理器，block.force-projector.name=力墙投影，严禁自创）

### 2.8 `lang/en_us.json` 喵
```json
"block.blockdustry.mender": "Mender",
"block.blockdustry.force_projector": "Force Projector"
```
（官方 bundle.properties：block.mender.name=Mender，block.force-projector.name=Force Projector）

## 3. 已知省略喵
- force-projector 冷却液（ConsumeCoolant cooldownLiquid=1.2）：本 mod 无液体系统，省略，cooldownNormal 保留喵
- 环境音（loopShield ambientSound）：本 mod 无环境音系统喵
- Fx.forceShrink（移除时护盾收缩动画）：MC 中 BE 移除后不再渲染，无法呈现，省略喵
- mender 维修音效（Sounds.healWave）：本 mod 无环境音系统喵
- mender 原版**无**"维修光束激光"，维修视觉 = 目标块 healBlockFull 闪烁（详见 ^T49 特效研究），已忠实移植喵

## 4. 编译自查喵
- 6 个 Java 文件已用 JDK21 javac + gradle 输出的 compileClasspath + sourcepath 单独编译通过（EXIT 0）
- 未跑全量 gradle compileJava（并行任务约束）喵

## 5. 核心数据库登记（供主会话更新核心数据库）喵
- mender：Mindustry 注册名 `mender` / 中文官方名 修理器 / 类别 效果(effect) / 电力0.3 + 硅 / 依赖 power-node
- force-projector：Mindustry 注册名 `force-projector` / 中文官方名 力墙投影 / 类别 效果(effect) / 电力4 + 相织布 / 依赖 mend-projector（临时 mender）
