# ^P1 批1C pulverizer + incinerator 整合清单喵

> 任务登记: `D:\Blockdustry\任务\T46_粉碎机焚烧炉.md`。机制/数据忠于 Mindustry 原版，贴图拷原版（md5 校验一致）不重绘喵。
> 动画/特效逐条研究记录: `docs/子agent/^T46_pulverizerIncinerator特效研究.md` 喵。

## 零、数据核对结论（最高要求：两建筑机制完全不同，严禁混淆）喵

### pulverizer（GenericCrafter 类，size 1）—— 粉碎废料产沙喵
- Mindustry `Blocks.java L1293-1309`：`pulverizer = GenericCrafter("pulverizer")`，**GenericCrafter 生产类**：吃 1 废料(scrap) 产 1 沙(sand)，`craftTime=40`，耗电 `0.5/s`，`itemCapacity=10`（GenericCrafter 默认）喵。
- cost：`Category.crafting` 铜×30 铅×25；未显式设 health → Mindustry 默认 `health = size²×40 = 40`（requirements 铜/铅 healthScaling=0）喵。
- drawer：`DrawMulti(DrawDefault, DrawRegion("-rotator"){spinSprite=true; rotateSpeed=2}, DrawRegion("-top"))` → 本体 + 旋转转盘 + 顶盖喵。
- craftEffect=`Fx.pulverize`（40tick、5 个 stoneGray #8f8f8f 方块、半径 3+fin×8 单位、尺寸 fout×2+0.5、旋转 45°）喵。
- updateEffect=`Fx.pulverizeSmall`（30tick、3 个 stoneGray 方块、半径 fin×5 单位、尺寸 fout+0.5、旋转 45°；`updateEffectChance=0.04/tick`）喵。
- Blockdustry 实现：strength 3 → 单格血 10+10×3=40（与原版 health=40 一致）；`craftTime=40`、吃 1 废料产 1 沙、容量 10、耗电 0.5/s、warmup 预热（0.019/tick）喵。

### incinerator（Incinerator 类，size 1）—— 吞噬物品销毁，不产任何物品喵
- Mindustry `Blocks.java L1324-1329`：`incinerator = Incinerator("incinerator")`，**Incinerator 类**（非 GenericCrafter！无输出、无库存，吞噬销毁）喵。
- cost：`Category.crafting` 石墨×5 铅×15；`health = 90`（显式设置）；`envEnabled |= Env.space`；耗电 `0.5/s` 喵。
- IncineratorBuild：`heat = approachDelta(heat, efficiency, 0.04)`；`acceptItem` 需 `heat>0.5 && enabled`；`handleItem` 30% 概率播 `Fx.fuelburn`（物品被销毁、不存储）喵。
- effect=`Fx.fuelburn`（23tick、5 个灰圆点、半径 fin×9 单位、颜色 lightGray→gray）喵。
- draw：`heat>0` 时火焰——alpha=`((1-0.3)+absin(Time.time,8,0.3)+rand(0.06)-0.06)×heat`，外圈 `Fill.circle` 半径 2 单位 tint `flameColor #ffad9d`、内圈半径 1 单位纯白 alpha=`heat` 喵。
- Blockdustry 实现：strength 8 → 单格血 10+10×8=90（与原版 health=90 一致）；heat 预热 0.04/tick、heat>0.5 且通电才收物品、吞噬销毁、30% 概率灰烟粒子、耗电 0.5/s 喵。
- **液体吞噬未迁移**：Blockdustry 暂无液体系统（原版 hasLiquids=true 可焚液体），仅迁移物品吞噬；属已知范围裁剪喵。

## 一、已交付（独立新文件，已含渲染，主会话勿重复注册）喵

| 文件 | 说明 |
|---|---|
| `src/main/java/com/blockdustry/production/PulverizerRegistrar.java` | 自包含注册类（模板 KilnRegistrar）：PULVERIZER 方块+物品+实体；size 1、strength 3（→单格血 40）喵 |
| `src/main/java/com/blockdustry/production/PulverizerBlockEntity.java` | 忠实 GenericCrafter：craftTime 40、吃 1 废料产 1 沙、容量 10、warmup 预热、dumpItem 卸沙、耗电 0.5/s、craft 灰粒（Fx.pulverize 等效）+ 持续小灰粒（Fx.pulverizeSmall 等效）、NBT 持久化喵 |
| `src/main/java/com/blockdustry/client/PulverizerBlockEntityRenderer.java` | 转盘旋转（DrawRegion spinSprite 忠实：r_deg=totalProgress×2 → 120°/s，MC 0.1047 rad/tick）+ 顶盖静态叠层；全亮 + NO_OVERLAY 喵 |
| `src/main/java/com/blockdustry/production/IncineratorRegistrar.java` | 自包含注册类：INCINERATOR 方块+物品+实体；size 1、strength 8（→单格血 90）喵 |
| `src/main/java/com/blockdustry/production/IncineratorBlockEntity.java` | 忠实 Incinerator：heat 预热 0.04/tick、heat>0.5 且通电才收物品、吞噬销毁、30% 概率灰烟（Fx.fuelburn 等效）、耗电 0.5/s、NBT 持久化喵 |
| `src/main/java/com/blockdustry/client/IncineratorBlockEntityRenderer.java` | 火焰双圈 billboard（外圈 #ffad9d 半径 0.25 格 alpha=呼吸×heat、内圈白半径 0.125 格 alpha=heat），全亮 + NO_OVERLAY 喵 |
| `assets/blockdustry/blockstates/pulverizer.json` | size 1 全 9 corner 变体 → 单模型（模板 turret/sorter）喵 |
| `assets/blockdustry/models/block/pulverizer.json` | base 模型（顶面 pulverizer.png、侧面石料）喵 |
| `assets/blockdustry/models/item/pulverizer.json` | 父=block/pulverizer 喵 |
| `assets/blockdustry/textures/block/pulverizer.png` | 拷原版 `pulverizer.png`（32×32，md5 3dc00562b84f5f23dfbe7e373c033cc6 与原版一致）喵 |
| `assets/blockdustry/textures/block/pulverizer_rotator.png` | 拷原版 `pulverizer-rotator.png`（32×32，md5 311a5136abbd4e8b62dcc41754374858；转盘层）喵 |
| `assets/blockdustry/textures/block/pulverizer_top.png` | 拷原版 `pulverizer-top.png`（32×32，md5 fbc017c5c8cbdaf92f9a4a0c36edd431；顶盖层）喵 |
| `assets/blockdustry/textures/research/blocks/pulverizer.png` | 科技树图标（拷原版同图）喵 |
| `assets/blockdustry/blockstates/incinerator.json` | size 1 全 9 corner 变体 → 单模型喵 |
| `assets/blockdustry/models/block/incinerator.json` | base 模型（顶面 incinerator.png、侧面石料）喵 |
| `assets/blockdustry/models/item/incinerator.json` | 父=block/incinerator 喵 |
| `assets/blockdustry/textures/block/incinerator.png` | 拷原版 `incinerator.png`（32×32，md5 c5e851c49a017811c608f96db2137152）喵 |
| `assets/blockdustry/textures/research/blocks/incinerator.png` | 科技树图标（拷原版同图）喵 |

## 二、主会话挂载点（按序合并，全部必须）喵

### 1. register — `Blockdustry.java` 构造器
在 `com.blockdustry.production.KilnRegistrar.register(modEventBus);` 之后加两行：
```java
com.blockdustry.production.PulverizerRegistrar.register(modEventBus);
com.blockdustry.production.IncineratorRegistrar.register(modEventBus);
```

### 2. tab — `BlockdustryBlocks.java` CRAFTING_TAB（Category.crafting 归锻造 tab）
在 `output.accept(com.blockdustry.building.PlastaniumCompressorRegistrar.PLASTANIUM_COMPRESSOR_ITEM);` 之后加两行：
```java
output.accept(com.blockdustry.production.PulverizerRegistrar.PULVERIZER_ITEM);
output.accept(com.blockdustry.production.IncineratorRegistrar.INCINERATOR_ITEM);
```

### 3. ResearchNodes — `ResearchNodes.java` all()，加 2 节点（parent 照抄 Mindustry SerpuloTechTree L147-149：kiln → pulverizer → incinerator）
在 kiln 节点之后加：
```java
ResearchNode.builder("pulverizer")
        .parent("kiln")                   // Mindustry SerpuloTechTree L147: kiln → pulverizer 喵
        .unlockBlock(com.blockdustry.production.PulverizerRegistrar.PULVERIZER.get())
        .buildRequirement(copper, 30)     // Mindustry pulverizer = 铜×30 铅×25 喵
        .buildRequirement(lead, 25)
        .build(),
ResearchNode.builder("incinerator")
        .parent("pulverizer")             // Mindustry SerpuloTechTree L148: pulverizer → incinerator 喵
        .unlockBlock(com.blockdustry.production.IncineratorRegistrar.INCINERATOR.get())
        .buildRequirement(graphite, 5)    // Mindustry incinerator = 石墨×5 铅×15 喵
        .buildRequirement(lead, 15)
        .build(),
```

### 4. 渲染 — `BlockdustryClient.java` registerRenderers
在 plastanium_compressor 渲染器行之后加两行：
```java
event.registerBlockEntityRenderer(com.blockdustry.production.PulverizerRegistrar.PULVERIZER_ENTITY.get(), com.blockdustry.client.PulverizerBlockEntityRenderer::new);
event.registerBlockEntityRenderer(com.blockdustry.production.IncineratorRegistrar.INCINERATOR_ENTITY.get(), com.blockdustry.client.IncineratorBlockEntityRenderer::new);
```

### 5. ResearchIcons — `ResearchIcons.java` nodeTexture
在 `case "plastanium_compressor" -> ...` 之后加两行：
```java
case "pulverizer" -> ResourceLocation.tryParse(base + "pulverizer.png");
case "incinerator" -> ResourceLocation.tryParse(base + "incinerator.png");
```

### 6. 组血量 — `BlockdustryBlocks.java` registerBlockHealthDefaults
**无需新增**：pulverizer/incinerator 均为 size 1 单格，血量走单格公式（10 + 10×strength）；pulverizer strength 3=40、incinerator strength 8=90，与原版 health 一致，多格组血量仅 size≥2 需要喵。

### 7. lang — `lang/en_us.json` + `lang/zh_cn.json` 追加（中文名用原版官方 bundle，严禁自创）
en_us:
```json
"block.blockdustry.pulverizer": "Pulverizer",
"block.blockdustry.incinerator": "Incinerator"
```
zh_cn:
```json
"block.blockdustry.pulverizer": "粉碎机",
"block.blockdustry.incinerator": "焚化炉"
```
（官方 bundle_zh_CN：`block.pulverizer.name = 粉碎机`、`block.incinerator.name = 焚化炉`；en：`Pulverizer`/`Incinerator` 喵）
（可选官方描述：`block.pulverizer.description = 将废料粉碎成细沙。` / `Crushes scrap into fine sand.`；`block.incinerator.description = 熔融并蒸发它接收到的任何物品或液体。` / `Vaporizes any item or liquid it receives.` —— 如需 tooltip 可加 `block.blockdustry.pulverizer.desc`/`block.blockdustry.incinerator.desc`，本 mod 现无此 lang 惯例，默认不加喵）

### 8. Jade 进度条（可选，非必须）
pulverizer 已提供 `getCraftProgress()` getter；如需在 `jade/ProgressServerProvider.java` 给粉碎机加制作进度条，仿 graphite_press 分支加：
```java
if (info instanceof com.blockdustry.production.PulverizerBlockEntity p) {
    groups.add(new ViewGroup<>(List.of(ProgressView.create(p.getCraftProgress())), Optional.of(ID_CRAFT), Optional.empty()));
}
```
（incinerator 无进度条；heat 条未做，属已知简化喵）

## 三、行为要点（验收对照）喵
- **粉碎机**：1×1 占地；接电（0.5/s）+ 至少 1 废料 → 40 tick 产 1 沙；废料/沙各自独立容量 10；缺料/满/断电停摆（warmup 衰减）；转盘随 warmup 旋转、顶盖静止；craft 时中心喷 5 个灰尘点、运行中 4%/tick 概率喷 3 个小灰点；每 tick 向相邻传送带卸沙喵。
- **焚化炉**：1×1 占地；通电后 heat 以 0.04/tick 爬升，heat>0.5 且通电时才接收任意物品并销毁（不存储、无输出）；每接收 30% 概率喷 5 个灰烟点；火焰双圈随 heat 呼吸（外圈 #ffad9d、内圈白），断电/未热时无火焰且拒收喵。
- 科技树：kiln 下新节点「粉碎机」→ 其下「焚化炉」，研究成本=配方（粉碎机铜×30铅×25、焚化炉石墨×5铅×15）喵。

## 四、风险 / 待人工排查喵
- **编译**：未跑全量 gradle（并行任务锁冲突约定），6 个新文件已用 JDK21 javac 单独编译通过（0 错误，含依赖闭包 105 个 class）喵。
- 焚化炉液体吞噬未迁移（Blockdustry 无液体系统）；`envEnabled |= Env.space` 无对应（无太空维度）喵。
- 环境音 `ambientSound = Sounds.loopGrind(0.025)` 未实现：Blockdustry 无方块环境循环音基础设施（与 kiln/graphite_press 一致）喵。
- 转盘 spinSprite 的「90° 双幅淡出」原版特效未复刻（MC 方块模型/quad 无 alpha 双重绘制），转盘以整幅旋转近似，视觉等效喵。
- 主会话合并后请跑 `./gradlew compileJava` + 游戏冒烟：锻造 tab 出现「粉碎机」「焚化炉」；粉碎机接电接废料产沙转盘转、焚化炉通电冒火吞噬物品；科技树 kiln 下两节点喵。

## 五、核心数据库登记喵
- `pulverizer`：Mindustry 注册名 `pulverizer`｜中文官方名「粉碎机」｜类别 生产/制造（Category.crafting）｜依赖 废料(scrap)+电｜产 沙(sand)
- `incinerator`：Mindustry 注册名 `incinerator`｜中文官方名「焚化炉」｜类别 生产/制造（Category.crafting）｜依赖 电｜吞噬销毁物品（无产出）
