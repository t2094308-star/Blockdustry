# ^P1 批1B container 整合清单 —— 存储容器迁移喵

> 任务登记: `D:\Blockdustry\任务\T29_1B_container.md`。机制/数据忠于 Mindustry 原版，贴图拷原版（md5 校验一致）不重绘喵。

## 零、数据核对结论（先纠偏任务描述）喵
- Mindustry 实测 `Blocks.java L3225-3230`：`container = size 2、itemCapacity 300、scaledHealth 55、配方 Category.effect 钛×100`。
- **任务描述所称 size 3 / itemCapacity 1000 是 vault（size 3 / 1000，L3232-3237）的参数，非 container**。按「数据不串（最高要求）」以原版 container 为准实现：size 2、itemCapacity 300。vault 本轮不做喵。
- 原版 StorageBlock **无 buildConfiguration / 无点击存储 UI**（StorageBuild 未覆写 buildConfiguration），点开只显示通用信息面板列出库存。故本实现**不自创复杂 UI**：简单存储显示 = 渲染器顶面主物品图标 + Jade 通用 provider（主类型 xN / 300）喵。
- 原版 `outputsItems() = false`（物品靠 unloader 取出）。本 mod 尚无 unloader，故按任务要求「参考 ItemSource 存取方式（Sink + dumpItem）」实现向相邻传送带/建筑卸货，保证与物流交接；下游不收则积存库内喵。
- 中文名用官方 bundle：`block.container.name = 容器`（bundle_zh_CN.properties），en 官方 = Container 喵。

## 一、已交付（独立新文件，已含渲染，主会话勿重复注册）喵

| 文件 | 说明 |
|---|---|
| `src/main/java/com/blockdustry/storage/ContainerRegistrar.java` | 自包含注册类（模板 FuseArcRegistrar）：CONTAINER 方块+物品+实体；size 2、strength 4.5（→单格血 55、组血 220，Mindustry size²×scaledHealth）喵 |
| `src/main/java/com/blockdustry/storage/ContainerBlock.java` | 2×2 存储方块（继承 BlockdustryBuildingBlock），无方向/无配置，不覆写 useWithoutItem（忠于原版无 UI）喵 |
| `src/main/java/com/blockdustry/storage/ContainerBlockEntity.java` | 忠实移植 StorageBlock.StorageBuild：多物品类型存储（LinkedHashMap），每类型独立上限 300（separateItemCapacity 语义）；轮询卸货指针（ItemModule.take 的 takeRotation）；NBT 持久化 bd_container_items 列表喵 |
| `src/main/java/com/blockdustry/client/ContainerBlockEntityRenderer.java` | 顶面中央画主存储物品图标；getRenderBoundingBox 扩到 2×2 防余光剔除（坑/碰撞箱.md §3）喵 |
| `assets/blockdustry/blockstates/container.json` | 全 9 corner 变体 → 4 象限模型（2×2，模板 distributor）喵 |
| `assets/blockdustry/models/block/container.json` | base 模型（全顶面，供物品栏展示）喵 |
| `assets/blockdustry/models/block/container_{nw,ne,sw,se}.json` | 2×2 四象限顶面裁剪模型（UV 每格 8×8/16）喵 |
| `assets/blockdustry/models/item/container.json` | 父=block/container 喵 |
| `assets/blockdustry/textures/block/container.png` | 拷原版 `blocks/storage/container.png`（64×64，md5 88cd65cc16cd14188255638cfc966705 与原版一致）喵 |
| `assets/blockdustry/textures/research/blocks/container.png` | 科技树图标（拷原版同图）喵 |

## 二、主会话挂载点（按序合并，全部必须）喵

### 1. register — `Blockdustry.java` 构造器
在 `ElevatorBlocks.register(modEventBus);` 之后加一行：
```java
com.blockdustry.storage.ContainerRegistrar.register(modEventBus);
```

### 2. tab — `BlockdustryBlocks.java` STORAGE_TAB（Category.effect 归存储 tab，core 之后）
在 `output.accept(CORE_ITEM);` 之后加一行：
```java
output.accept(com.blockdustry.storage.ContainerRegistrar.CONTAINER_ITEM);
```

### 3. ResearchNodes — `ResearchNodes.java` all()，加 1 节点（parent=router，Mindustry SerpuloTechTree 中 container 挂在 router 下）
```java
ResearchNode.builder("container")
        .parent("router")
        .unlockBlock(com.blockdustry.storage.ContainerRegistrar.CONTAINER.get())
        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 100) // Mindustry container = 钛×100 喵
        .build(),
```
（若 all() 顶部局部变量区方便，也可加 `Item titanium = BlockdustryItems.TITANIUM.get();` 后写 `titanium`，与 copper/lead 一致喵）

### 4. 渲染 — `BlockdustryClient.java` registerRenderers
在 sorter 渲染器行之后加一行：
```java
event.registerBlockEntityRenderer(com.blockdustry.storage.ContainerRegistrar.CONTAINER_ENTITY.get(), com.blockdustry.client.ContainerBlockEntityRenderer::new);
```

### 5. ResearchIcons — `ResearchIcons.java` nodeTexture
在 `case "underflow_gate" -> ...` 之后加一行：
```java
case "container" -> ResourceLocation.tryParse(base + "container.png");
```

### 6. 多格组血量 — `BlockdustryBlocks.java` registerBlockHealthDefaults
在 distributor 行之后加一行：
```java
registerGroupMaxHp(com.blockdustry.storage.ContainerRegistrar.CONTAINER.get(), 2); // container 2×2 组血 220（strength 4.5 → 单格 55）喵
```

### 7. lang — `lang/en_us.json` + `lang/zh_cn.json` 追加
en_us:
```json
"block.blockdustry.container": "Container"
```
zh_cn:
```json
"block.blockdustry.container": "容器"
```
（官方 bundle_zh_CN `block.container.name = 容器`，en `block.container.name = Container` 喵）

## 三、行为要点（验收对照）喵
- 放置：2×2 占地（四象限跨格模型 + 组碰撞箱 + 组血量 220），普通方块物品放置预检喵
- 存取：传送带/物品源可推入任意材料，各类型独立上限 300（可同时存多种，如铜×300+铅×300）喵
- 输出：每 tick 从轮询指针起向相邻可接收传送带/建筑卸 1 件（dumpItem），下游满/不收则积存喵
- 简单存储显示：顶面画主存储物品图标；Jade 显示「内容: <主类型> xN / 300」（读 getStoredItem/getStoredCount/getCapacity 的通用 provider，自动生效，无需新代码）喵
- 无电力/无耗料/无配置菜单（忠于原版无 UI）喵

## 四、风险 / 待人工排查喵
- **核心链接（Mindustry 放核心旁 linkedCore 直通核心池、移除时分摊回容器）未实现**，属本轮已知简化（依赖核心共享池架构，后续批次处理）喵
- 队伍染色（container-team.png 叠加层）未做：方块模型静态、无 BlockColors 挂点，且多数建筑未做队伍染色；视觉以原版 base 贴图为准喵
- dumpItem 卸货是「下游不收则积存」的缓冲语义（drill/press/router 同款），与 Mindustry 纯存储+unloader 语义略有差异（无 unloader 前的适配）；后续迁移 unloader 时若需「不自动卸货」可去掉 tickAnchor 的 dump 逻辑喵
- 主会话合并后请跑 `./gradlew compileJava` + 游戏冒烟：创造栏存储 tab 出现「容器」、放置 2×2、传送带喂入/接出、科技树 router 下新节点、Jade 显示库存喵
