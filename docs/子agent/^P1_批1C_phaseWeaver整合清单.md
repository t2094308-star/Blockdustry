# ^P1 批1C phaseWeaver 整合清单喵

> 相织布编织器（Mindustry phase-weaver，GenericCrafter size 2）迁移完成。本清单供主会话合并，列出所有新文件 + 共享文件挂载点（精确到方法/行）喵。
> ⚠️ 官方中文名是「**相织布编织器**」（bundle_zh_CN: `block.phase-weaver.name`），任务标题写的「相织布织机」是口语，lang 一律用官方名喵。
> 数据核对（Blocks.java L1136-1152）：craftTime=120、吃钍×4+沙×10 产相织布×1、耗电 5/s、cost 硅×130 铅×120 钍×75、
> itemCapacity=30、health=160、drawer DrawMulti(DrawRegion(-bottom), DrawWeave, DrawDefault)、craftEffect Fx.smeltsmoke、科技树 parent=plastaniumCompressor 喵。

## 1. 新文件（自包含，无需主会话改动内容）喵

| 文件 | 说明 |
|---|---|
| `src/main/java/com/blockdustry/building/PhaseWeaverRegistrar.java` | 自包含注册类（方块+物品+BE），模板 KilnRegistrar，strength 3 → 组血 160 |
| `src/main/java/com/blockdustry/building/PhaseWeaverBlockEntity.java` | BE：吃钍/沙产相织布、warmup、totalProgress 织机旋转累积、冒烟起点同步、BlockdustryPowerNode(5) |
| `src/main/java/com/blockdustry/client/PhaseWeaverBlockEntityRenderer.java` | 织机动画（DrawWeave：织纹旋转+accent 梭线扫描）+ Fx.smeltsmoke 渲染器 |
| `src/main/resources/assets/blockdustry/blockstates/phase_weaver.json` | blockstate 全 9 corner 变体 → 4 象限模型 |
| `src/main/resources/assets/blockdustry/models/block/phase_weaver_{nw,ne,sw,se}.json` | 4 象限模型（顶面裁 quadrant UV，侧面用 phase_weaver_side） |
| `src/main/resources/assets/blockdustry/models/item/phase_weaver.json` | 物品模型（parent nw） |
| `src/main/resources/assets/blockdustry/textures/block/phase_weaver.png` | 拷原版 phase-weaver.png（64×64，带窗口，不重绘） |
| `src/main/resources/assets/blockdustry/textures/block/phase_weaver_bottom.png` | 拷原版 phase-weaver-bottom.png（64×64 窗口地板） |
| `src/main/resources/assets/blockdustry/textures/block/phase_weaver_weave.png` | 拷原版 phase-weaver-weave.png（64×64 织纹） |
| `src/main/resources/assets/blockdustry/textures/block/phase_weaver_side.png` | 程序化生成侧面（采样基贴图边缘色 RGB166,154,149 + 面板线，非重绘，见坑/机器侧面贴图.md） |
| `src/main/resources/assets/blockdustry/textures/research/blocks/phase_weaver.png` | 科技树方块图标（= 拷原版 base） |
| `src/main/resources/assets/blockdustry/textures/research/items/thorium.png` | 科技树钍图标（拷原版 item-thorium.png） |

## 2. 共享文件挂载点（主会话按此合并，勿动新文件）喵

### 2.1 `Blockdustry.java` 构造器（挂注册类）喵
在 `com.blockdustry.building.PlastaniumCompressorRegistrar.register(modEventBus);` 行附近加一行：
```java
com.blockdustry.building.PhaseWeaverRegistrar.register(modEventBus);
```

### 2.2 `BlockdustryClient.java` `registerRenderers` 喵
在 `PlastaniumCompressorBlockEntityRenderer` 注册行（L82 附近）之后加：
```java
event.registerBlockEntityRenderer(com.blockdustry.building.PhaseWeaverRegistrar.PHASE_WEAVER_ENTITY.get(), com.blockdustry.client.PhaseWeaverBlockEntityRenderer::new);
```

### 2.3 `BlockdustryBlocks.java` CRAFTING_TAB（创造栏）喵
`displayItems` 里 `output.accept(com.blockdustry.building.PlastaniumCompressorRegistrar.PLASTANIUM_COMPRESSOR_ITEM);`（L165）之后加：
```java
output.accept(com.blockdustry.building.PhaseWeaverRegistrar.PHASE_WEAVER_ITEM);
```

### 2.4 `BlockdustryBlocks.java` `registerBlockHealthDefaults`（整组血量）喵
在 `registerGroupMaxHp(...PLASTANIUM_COMPRESSOR.get(), 2);`（L389）后加：
```java
registerGroupMaxHp(com.blockdustry.building.PhaseWeaverRegistrar.PHASE_WEAVER.get(), 2); // 批1C 相织布编织器 2×2 组血 160喵
```

### 2.5 `ResearchNodes.java` `all()`（科技树）喵
在 `plastanium_compressor` 节点（L150-157，以 `.build(),` 结尾）之后加（silicon/lead 局部变量已存在；钍用 BlockdustryItems.THORIUM 全限定，与 blast_drill 节点同款）：
```java
ResearchNode.builder("phase_weaver")
        .parent("plastanium_compressor")      // Mindustry SerpuloTechTree: plastaniumCompressor → phaseWeaver 喵
        .unlockBlock(com.blockdustry.building.PhaseWeaverRegistrar.PHASE_WEAVER.get())
        .buildRequirement(silicon, 130)       // Mindustry phase-weaver = 硅×130 铅×120 钍×75 喵
        .buildRequirement(lead, 120)
        .buildRequirement(com.blockdustry.item.BlockdustryItems.THORIUM.get(), 75)
        .build(),
```

### 2.6 `ResearchIcons.java` `nodeTexture` 喵
在 `case "plastanium_compressor"` 后加：
```java
case "phase_weaver" -> ResourceLocation.tryParse(base + "phase_weaver.png");
```

### 2.7 `ResearchIcons.java` `itemTexture` 喵
在 `case "plastanium"` 后加（blast_drill 节点已用钍做成本但图标缺失，本次补上）：
```java
case "thorium" -> ResourceLocation.tryParse("blockdustry:textures/research/items/thorium.png");
```

### 2.8 `lang/zh_cn.json` 喵
```json
"block.blockdustry.phase_weaver": "相织布编织器"
```
（官方 bundle_zh_CN：block.phase-weaver.name = 相织布编织器）

### 2.9 `lang/en_us.json` 喵
```json
"block.blockdustry.phase_weaver": "Phase Weaver"
```
（官方 bundle.properties：block.phase-weaver.name = Phase Weaver）

## 3. 可选（建议但非必须）Jade 显示喵

相织布编织器含 3 种库存（钍/沙/相织布）+ 制作进度，若要让 Jade 显示需改共享文件（本次未做，BE 已提供 getters）：
- `jade/BuildingInfoServerDataProvider.java`：加 `else if (building instanceof PhaseWeaverBlockEntity s)` 分支，写入
  `KEY_THORIUM=s.getThoriumCount()`、`KEY_SAND=s.getSandCount()`、`KEY_FABRIC=s.getPhaseFabricCount()`（常量按现有模式加）
- `jade/BuildingInfoComponentProvider.java`：在对应 KEY 分支加显示（"钍: X | 沙: Y | 相织布: Z"）
- `jade/ProgressServerProvider.java`：加 `if (info instanceof PhaseWeaverBlockEntity s)` → `ProgressView.create(s.getCraftProgress())`

## 4. 已知省略（本 mod 无对应系统）喵
- ambientSound（loopTech 0.02f）：本 mod 无环境音系统
- `envEnabled |= Env.space`：本 mod 无空间维度概念
- 织纹旋转 60rad/s 视觉极快 = 原版真实表现（totalProgress 每秒 +60），非 bug
- 冒烟 alpha 由原版「骤灭」改为 fout 淡出、烟团 3D 相机 billboard（与 SiliconSmelter 同款适配）

## 5. 编译自查喵
- `PhaseWeaverRegistrar/PhaseWeaverBlockEntity/PhaseWeaverBlockEntityRenderer` 三文件已用 JDK21 javac + 项目 classpath（ng_execute MC jar + neoforge universal + bus + datafixerupper + brigadier + joml + build/classes）单独编译通过（EXIT 0）喵
- 全量 `./gradlew compileJava` 由主会话统一做（并行任务 gradle 锁冲突规避）喵

## 6. 核心数据库登记喵
- Mindustry 注册名：`phase-weaver`
- 中文官方名：**相织布编织器**
- 类别：生产（GenericCrafter，Category.crafting）
- 依赖：钍（item）、沙（vanilla Items.SAND）、相织布（item）、电力（5/s）
