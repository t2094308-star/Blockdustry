# ^P1 批1C kiln 整合清单 —— 窖炉迁移喵

> 任务登记: `D:\Blockdustry\任务\T36_窖炉.md`。机制/数据忠于 Mindustry 原版，贴图拷原版（md5 校验一致）不重绘喵。
> 火焰特效研究: `docs/子agent/^T36_kiln火焰特效研究.md` 喵。

## 零、数据核对结论（先纠偏任务描述）喵
- Mindustry `Blocks.java L1103-1116`：`kiln = GenericCrafter, size 2, craftTime 30, 吃 1 铅+1 沙产 1 钢化玻璃, 耗电 0.60/s, cost 铜×60 石墨×30 铅×30, drawer=DrawDefault+DrawFlame(#ffc099), craftEffect=Fx.smeltsmoke, ambientSound=loopSmelter(0.07), itemCapacity=10` 喵。
- **任务标题「窖炉」是笔误，官方 bundle_zh_CN 名 = 「窑炉」**（`block.kiln.name = 窑炉`），en = `Kiln`。按原版官方名「窑炉」喵。
- 血量：GenericCrafter 未显式设 health → Mindustry 默认 `health = size² × 40 = 160`（requirements 铜/石墨/铅 healthScaling 均 0）。Blockdustry strength 3 → 单格 40、组血 160，与 Mindustry 一致喵。
- 火苗无独立粒子贴图（原版 DrawFlame 用 `Fill.circle` 程序化圆），故不拷 fireN.png；只拷 `kiln-top.png`（炉口烧红叠层）与 `kiln.png`（本体）喵。

## 一、已交付（独立新文件，已含渲染，主会话勿重复注册）喵

| 文件 | 说明 |
|---|---|
| `src/main/java/com/blockdustry/production/KilnRegistrar.java` | 自包含注册类（模板 ContainerRegistrar）：KILN 方块+物品+实体；size 2、strength 3（→单格血 40、组血 160）喵 |
| `src/main/java/com/blockdustry/production/KilnBlockEntity.java` | 忠实 GenericCrafter：craftTime 30、吃 1 铅+1 沙产 1 钢化玻璃、各类型独立容量 10、warmup 渐热、dumpItem 卸货、耗电 0.6/s（BlockdustryPowerNode）、craft 白色烟尘粒子（Fx.smeltsmoke 等效）、NBT 持久化喵 |
| `src/main/java/com/blockdustry/client/KilnBlockEntityRenderer.java` | 火焰渲染（DrawFlame 全参数忠实迁移）：顶面 kiln_top 叠层 alpha=warmup + 外圈 #ffc099/内圈白双圈火苗（absin 5s 脉动+随机抖动）+ 环境光晕（Drawf.light 等效）；getRenderBoundingBox 扩到 2×2+3 防剔除喵 |
| `assets/blockdustry/blockstates/kiln.json` | 全 9 corner 变体 → 4 象限模型（2×2，模板 distributor）喵 |
| `assets/blockdustry/models/block/kiln.json` | base 模型（全顶面，供物品栏展示）喵 |
| `assets/blockdustry/models/block/kiln_{nw,ne,sw,se}.json` | 2×2 四象限顶面裁剪模型（UV 每格 8×8/16）喵 |
| `assets/blockdustry/models/item/kiln.json` | 父=block/kiln 喵 |
| `assets/blockdustry/textures/block/kiln.png` | 拷原版 `kiln.png`（64×64，md5 3e5c6580b8a4d921ccfcd10824d4a024 与原版一致）喵 |
| `assets/blockdustry/textures/block/kiln_top.png` | 拷原版 `kiln-top.png`（64×64，md5 a3f6bdee48c8a686e4b26a6a20228d5a 与原版一致；火焰炉口叠层）喵 |
| `assets/blockdustry/textures/research/blocks/kiln.png` | 科技树图标（拷原版同图）喵 |

## 二、主会话挂载点（按序合并，全部必须）喵

### 1. register — `Blockdustry.java` 构造器
在 `ContainerRegistrar.register(modEventBus);` 之后加一行：
```java
com.blockdustry.production.KilnRegistrar.register(modEventBus);
```

### 2. tab — `BlockdustryBlocks.java` CRAFTING_TAB（Category.crafting 归锻造 tab）
在 `output.accept(GRAPHITE_PRESS_ITEM);` 之后加一行：
```java
output.accept(com.blockdustry.production.KilnRegistrar.KILN_ITEM);
```

### 3. ResearchNodes — `ResearchNodes.java` all()，加 1 节点（parent=graphite_press，Mindustry SerpuloTechTree L147：kiln 挂在 graphitePress 下）
```java
ResearchNode.builder("kiln")
        .parent("graphite_press")
        .unlockBlock(com.blockdustry.production.KilnRegistrar.KILN.get())
        .buildRequirement(copper, 60)     // Mindustry kiln = 铜×60 石墨×30 铅×30 喵
        .buildRequirement(graphite, 30)
        .buildRequirement(lead, 30)
        .build(),
```

### 4. 渲染 — `BlockdustryClient.java` registerRenderers
在 container 渲染器行之后加一行：
```java
event.registerBlockEntityRenderer(com.blockdustry.production.KilnRegistrar.KILN_ENTITY.get(), com.blockdustry.client.KilnBlockEntityRenderer::new);
```

### 5. ResearchIcons — `ResearchIcons.java` nodeTexture
在 `case "container" -> ...` 之后加一行：
```java
case "kiln" -> ResourceLocation.tryParse(base + "kiln.png");
```

### 6. 多格组血量 — `BlockdustryBlocks.java` registerBlockHealthDefaults
在 container 行之后加一行：
```java
registerGroupMaxHp(com.blockdustry.production.KilnRegistrar.KILN.get(), 2); // kiln 2×2 组血 160（strength 3 → 单格 40）喵
```

### 7. lang — `lang/en_us.json` + `lang/zh_cn.json` 追加
en_us:
```json
"block.blockdustry.kiln": "Kiln"
```
zh_cn:
```json
"block.blockdustry.kiln": "窑炉"
```
（官方 bundle_zh_CN `block.kiln.name = 窑炉`，en `block.kiln.name = Kiln` 喵）

## 三、行为要点（验收对照）喵
- 放置：2×2 占地（四象限跨格模型 + 组碰撞箱 + 组血 160），普通方块物品放置预检喵
- 生产：接电（0.6/s）+ 至少 1 铅 + 1 沙 → 30 tick 产 1 钢化玻璃；各物品独立容量 10；缺料/满/断电停摆（warmup 衰减）喵
- 火焰：通电且有料时 warmup 渐热爬升，顶面炉口叠层渐亮、双圈火苗 #ffc099/白呼吸脉动；无料/断电火灭喵
- 输出：每 tick 从轮询指针向相邻可接收传送带/建筑卸 1 件钢化玻璃（dumpItem），下游满则积存喵
- craft 白烟：每次产出时中心发 6 个白色尘点向外散（Fx.smeltsmoke 等效）喵

## 四、风险 / 待人工排查喵
- **编译阻塞（外部）**：`./gradlew compileJava` 失败，但 2 个错误全在**另一子任务 T34（爆破钻头）** 的 `building/BlastDrillBlock.java`（缺 `import net.minecraft.world.level.block.entity.BlockEntityType;`，L16/L45 找不到符号）。kiln 三个新文件无任何报错（同一次 javac 已类型检查）。**未擅改 T34 占用文件**，请协调者通知 T34 补 import 或由主会话修喵。
- 环境音 `ambientSound = Sounds.loopSmelter(0.07)` 未实现：Blockdustry 目前无方块环境循环音基础设施，属已知简化（与 graphite_press 一致）喵
- 队伍染色（kiln-team.png 叠层）未做：多数建筑未做队伍染色，视觉以原版 base 贴图为准喵
- 主会话合并后请跑 `./gradlew compileJava` + 游戏冒烟：创造栏锻造 tab 出现「窑炉」、放置 2×2、接电接传送带、喂 1 铅 1 沙产钢化玻璃、火焰呼吸动画、科技树 graphite_press 下新节点喵
