# ^P1 批1C siliconSmelter 整合清单喵

> 硅冶炼厂（Mindustry silicon-smelter，GenericCrafter size 2）迁移完成。本清单供主会话合并，列出所有新文件 + 共享文件挂载点（精确到方法/行）喵。
> 数据核对：craftTime=40、吃 coal×1+sand×2 产 silicon×1、耗电 0.5、配方 copper×30+lead×25、warmupSpeed 0.019、Fx.smeltsmoke 15tick、DrawFlame #ffef99、科技树 parent=graphitePress喵。

## 1. 新文件（自包含，无需主会话改动内容）喵

| 文件 | 说明 |
|---|---|
| `src/main/java/com/blockdustry/building/SiliconSmelterRegistrar.java` | 自包含注册类（方块+物品+BE），模板 FuseArcRegistrar |
| `src/main/java/com/blockdustry/building/SiliconSmelterBlockEntity.java` | BE：吃煤/沙产硅、warmup、冒烟起点同步、BlockdustryPowerNode(0.5) |
| `src/main/java/com/blockdustry/client/SiliconSmelterBlockEntityRenderer.java` | 尾焰（DrawFlame）+ 冒烟（Fx.smeltsmoke）渲染器 |
| `src/main/resources/assets/blockdustry/blockstates/silicon_smelter.json` | blockstate 全 corner 变体 |
| `src/main/resources/assets/blockdustry/models/block/silicon_smelter_{nw,ne,sw,se}.json` | 4 象限模型（顶面贴图象限，侧面 stone） |
| `src/main/resources/assets/blockdustry/models/item/silicon_smelter.json` | 物品模型（parent nw） |
| `src/main/resources/assets/blockdustry/textures/block/silicon_smelter.png` | 拷原版 silicon-smelter.png（64×64 不重绘） |
| `src/main/resources/assets/blockdustry/textures/entity/silicon_smelter_top.png` | 拷原版 silicon-smelter-top.png（火焰光晕） |
| `src/main/resources/assets/blockdustry/textures/research/blocks/silicon_smelter.png` | 科技树图标（= 方块贴图） |

## 2. 共享文件挂载点（主会话按此合并，勿动新文件）喵

### 2.1 `Blockdustry.java` 构造器（挂注册类）喵
在 `BridgeRegistrar.register(modEventBus);`（批1B 之后）附近加一行：
```java
com.blockdustry.building.SiliconSmelterRegistrar.register(modEventBus);
```

### 2.2 `BlockdustryClient.java` `registerRenderers`（注册渲染器）喵
在 `SorterBlockEntityRenderer` / `ContainerBlockEntityRenderer` 行附近加：
```java
event.registerBlockEntityRenderer(
    com.blockdustry.building.SiliconSmelterRegistrar.SILICON_SMELTER_ENTITY.get(),
    com.blockdustry.client.SiliconSmelterBlockEntityRenderer::new);
```

### 2.3 `BlockdustryBlocks.java` CRAFTING_TAB（创造栏）喵
`CRAFTING_TAB` 的 `displayItems` 里 `output.accept(GRAPHITE_PRESS_ITEM);` 之后加：
```java
output.accept(SiliconSmelterRegistrar.SILICON_SMELTER_ITEM);
```

### 2.4 `BlockdustryBlocks.java` `registerBlockHealthDefaults`（整组血量）喵
在 `registerGroupMaxHp(GRAPHITE_PRESS.get(), 2);` 后加：
```java
registerGroupMaxHp(SiliconSmelterRegistrar.SILICON_SMELTER.get(), 2); // 批1C silicon-smelter 2×2 组血喵
```

### 2.5 `ResearchNodes.java` `all()`（科技树）喵
在 `graphite_press` 节点之后加（copper/lead/silicon/graphite 局部变量已存在）：
```java
ResearchNode.builder("silicon_smelter")
        .parent("graphite_press")                 // Mindustry SerpuloTechTree: graphitePress → siliconSmelter 喵
        .unlockBlock(SiliconSmelterRegistrar.SILICON_SMELTER.get())
        .buildRequirement(copper, 30)             // Mindustry silicon-smelter = 铜×30 铅×25 喵
        .buildRequirement(lead, 25)
        .build(),
```

### 2.6 `ResearchIcons.java` `nodeTexture`（科技树图标）喵
在 `case "graphite_press"` 后加：
```java
case "silicon_smelter" -> ResourceLocation.tryParse(base + "silicon_smelter.png");
```

### 2.7 `lang/zh_cn.json` 喵
```json
"block.blockdustry.silicon_smelter": "硅冶炼厂"
```
（官方 bundle_zh_CN：block.silicon-smelter.name = 硅冶炼厂）

### 2.8 `lang/en_us.json` 喵
```json
"block.blockdustry.silicon_smelter": "Silicon Smelter"
```
（官方 bundle.properties：block.silicon-smelter.name = Silicon Smelter）

## 3. 可选（建议但非必须）Jade 显示喵

硅冶炼厂含 3 种库存（煤/沙/硅）+ 制作进度，若要让 Jade 显示需改共享文件：
- `jade/BuildingInfoServerDataProvider.java`：加 `else if (building instanceof SiliconSmelterBlockEntity s)` 分支，写入
  `KEY_COAL=s.getCoalCount()`、新增 `KEY_SAND`（=s.getSandCount()）、`KEY_SILICON=s.getSiliconCount()`（常量按现有模式加）
- `jade/BuildingInfoComponentProvider.java`：在 `KEY_SILICON` 分支处加硅冶炼厂显示（"煤: X | 沙: Y | 硅: Z"）
- `jade/ProgressServerProvider.java`：加 `if (info instanceof SiliconSmelterBlockEntity s)` → `ProgressView.create(s.getCraftProgress())`
- BE 已提供 getters：`getCoalCount()/getSandCount()/getSiliconCount()/getCraftProgress()` 喵

## 4. 已知省略喵
- ambientSound（loopSmelter 0.07f）：本 mod 无环境音系统
- emitLight 动态光（DrawFlame lightRadius=60）：MC 原版无动态光
- 冒烟 alpha 由原版「骤灭」改为 fout 淡出、烟团由水平面改为相机 billboard + 轻微上升（3D 适配，详见 docs/子agent/^T35_siliconSmelter冒烟特效研究.md）喵

## 5. 编译自查喵
- `SiliconSmelterBlockEntity/Registrar/Renderer` 三文件已用 JDK21 javac + 项目 classpath 单独编译通过（EXIT 0）
- 全量 `./gradlew compileJava` 因**并行任务 T34 爆破钻头**遗留错误（BlastDrillBlockEntity.java 缺 List import / ParticleTypes.ITEM_CRACK / oreColor 未定义）而失败，与本任务无关，待其任务收敛后自然解决喵
