# ^P1 批1C pyratiteMixer 整合清单喵

> 硫化物混合器（Mindustry pyratite-mixer，GenericCrafter size 2）迁移完成。本清单供主会话合并，列出所有新文件 + 共享文件挂载点（精确到行）喵。
> 数据核对：**size=2**（任务描述误写 size 1，已按原版 Blocks.java L1189-1202 修正）、craftTime=80（默认）、吃 coal×1+lead×2+sand×2 产 pyratite×1、耗电 0.20、配方 copper×50+lead×25、warmupSpeed 0.019、health=size²×40=160（copper/lead healthScaling=0）、envEnabled|=space、ambientSound=loopMachineSpin（本 mod 无环境音系统，省略）喵。
> 动画核查结论：**原版无任何动画/粒子/光效**——drawer=DrawDefault（静态贴图）、craftEffect/updateEffect=Fx.none、无 emitLight。故本迁移**不新建渲染器**（忠实原版，杜绝自编特效）。详见下方「动画特效核查」喵。

## 1. 新文件（自包含，无需主会话改动内容）喵

| 文件 | 说明 |
|---|---|
| `src/main/java/com/blockdustry/production/PyratiteMixerRegistrar.java` | 自包含注册类（方块+物品+BE），模板 KilnRegistrar |
| `src/main/java/com/blockdustry/production/PyratiteMixerBlockEntity.java` | BE：吃煤/铅/沙产硫化物、warmup、BlockdustryPowerNode(0.20)，无粒子 |
| `src/main/resources/assets/blockdustry/blockstates/pyratite_mixer.json` | blockstate 全 corner 变体 |
| `src/main/resources/assets/blockdustry/models/block/pyratite_mixer_{nw,ne,sw,se}.json` | 4 象限模型（顶面贴图象限，侧面 stone） |
| `src/main/resources/assets/blockdustry/models/item/pyratite_mixer.json` | 物品模型（parent nw） |
| `src/main/resources/assets/blockdustry/textures/block/pyratite_mixer.png` | 拷原版 pyratite-mixer.png（64×64，不重绘） |
| `src/main/resources/assets/blockdustry/textures/research/blocks/pyratite_mixer.png` | 科技树图标（= 方块贴图） |

## 2. 共享文件挂载点（主会话按此合并，勿动新文件）喵

### 2.1 `Blockdustry.java` 构造器（挂注册类）喵
在 `KilnRegistrar.register(modEventBus);`（批1C）附近加一行：
```java
com.blockdustry.production.PyratiteMixerRegistrar.register(modEventBus);
```

### 2.2 `BlockdustryBlocks.java` `CRAFTING_TAB`（创造栏，L164 kiln 之后）喵
```java
output.accept(com.blockdustry.production.PyratiteMixerRegistrar.PYRATITE_MIXER_ITEM);
```

### 2.3 `BlockdustryBlocks.java` `registerBlockHealthDefaults`（整组血量，L389 kiln 后）喵
```java
registerGroupMaxHp(com.blockdustry.production.PyratiteMixerRegistrar.PYRATITE_MIXER.get(), 2); // 批1C 硫化物混合器 2×2 组血 160喵
```

### 2.4 `ResearchNodes.java` `all()`（科技树，L149 kiln 节点之后）喵
```java
ResearchNode.builder("pyratite_mixer")
        .parent("graphite_press")                 // Mindustry SerpuloTechTree: graphitePress → pyratiteMixer（需求 SectorComplete(crateredBattleground)）喵
        .unlockBlock(com.blockdustry.production.PyratiteMixerRegistrar.PYRATITE_MIXER.get())
        .buildRequirement(copper, 50)             // Mindustry pyratite-mixer = 铜×50 铅×25 喵
        .buildRequirement(lead, 25)
        .build(),
```

### 2.5 `ResearchIcons.java` `nodeTexture`（L55 `plastanium_compressor` 后加）喵
```java
case "pyratite_mixer" -> ResourceLocation.tryParse(base + "pyratite_mixer.png");
```

### 2.6 `lang/zh_cn.json` 喵
```json
"block.blockdustry.pyratite_mixer": "硫化物混合器"
```
（官方 bundle_zh_CN：block.pyratite-mixer.name = 硫化物混合器）

### 2.7 `lang/en_us.json` 喵
```json
"block.blockdustry.pyratite_mixer": "Pyratite Mixer"
```
（官方 bundle.properties：block.pyratite-mixer.name = Pyratite Mixer）

## 3. 动画特效核查（逐条记录，用户最高要求）喵

| 原版机制 | 原版实现 | 迁移处置 |
|---|---|---|
| drawer | `DrawDefault`（静态 `Draw.rect(block.region, ...)`，无 spinSprite） | 静态方块模型（blockstate corner 变体），**不新建渲染器** |
| craftEffect | `Fx.none`（未覆写） | 无 craft 粒子 |
| updateEffect | `Fx.none`（未覆写） | 无 update 粒子 |
| emitLight | 未设置 | 无光效 |
| ambientSound | `Sounds.loopMachineSpin` 0.1f | 本 mod 无环境音系统，省略（同 silicon-smelter/kiln 先例） |
| warmup | GenericCrafter approachDelta 0.019，仅驱动动画/音效 | BE 跟踪 warmup + NBT 存档，无视觉消费者 |

结论：原版 pyratite-mixer **没有任何制作动画/粒子/光效**（纯静态贴图 + 旋转环境音），已忠实复刻为静态方块模型。若协调者预期看到搅拌动画，请知悉那并非原版行为——本 mod 不做「原版没有」的增强喵。

## 4. 可选（建议但非必须）Jade 显示喵

BE 已提供 getters：`getCoalCount()/getLeadCount()/getSandCount()/getPyratiteCount()/getCraftProgress()`，若需 Jade 显示按 silicon-smelter 同款模式加分支喵。

## 5. 编译自查喵

- `PyratiteMixerRegistrar.java` + `PyratiteMixerBlockEntity.java` 已用 JDK21 javac（Eclipse Adoptium 21.0.11）+ 项目 classpath（neoForm recompile/classes 含 AccessTransformer 后的 MC + blockhealth jar + 全部模块依赖）单独编译通过（EXIT 0）喵。
- 注意：javac 编译必须用 `build/neoForm/neoFormJoined1.21.1-20240808.144430/steps/recompile/classes` 作为 MC 类路径，而不是 `steps/rename/outputs.jar`（后者 `BlockEntitySupplier` 是包私有，报「private 访问控制」假错误）喵。

## 6. 核心数据库登记（供主会话更新 docs/核心数据库.md）喵

- Mindustry 注册名：`pyratite-mixer`
- 中文官方名：硫化物混合器（英文 Pyratite Mixer）
- 类别：生产/加工（Mindustry Category.crafting，GenericCrafter）
- 依赖：建造 铜×50 铅×25；运行 煤×1+铅×2+沙×2 → 硫化物×1（craftTime 80，耗电 0.20/s）；科技树 parent=graphite_press
