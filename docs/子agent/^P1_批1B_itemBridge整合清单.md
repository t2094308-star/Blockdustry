# ^P1 批1B itemBridge（bridge-conveyor 传送带桥）整合清单喵

> 主会话按此清单挂载。数据全部核对 Mindustry 原版：`Blocks.java` L2105-2113（BufferedItemBridge）+ `ItemBridge.java` + `BufferedItemBridge.java` + `ItemBuffer.java` + `SerpuloTechTree.java` L42 + `bundle_zh_CN.properties` L2007 官方名喵。

## 一、新文件（子 agent 已写，主会话勿改）喵

**Java（包 `com.blockdustry.distribution`）喵**
- `src/main/java/com/blockdustry/distribution/BridgeRegistrar.java` — 自包含注册类（模板 SorterRegistrar）喵
- `src/main/java/com/blockdustry/distribution/ItemBridgeBlock.java` — 方块，size 1，无 FACING（配对方向动态），onPlace 触发自动配对喵
- `src/main/java/com/blockdustry/distribution/ItemBridgeBlockEntity.java` — 配对 + 缓冲传输 + 交付节流 BE 喵

**Java（包 `com.blockdustry.client`）喵**
- `src/main/java/com/blockdustry/client/ItemBridgeBlockEntityRenderer.java` — 桥面/端部/滚动箭头渲染喵

**资源（assets/blockdustry）喵**
- `blockstates/bridge_conveyor.json` — 9 corner 变体（cube_all，桥无固定朝向）喵
- `models/block/bridge_conveyor.json`（cube_all）、`models/item/bridge_conveyor.json` 喵
- `textures/block/bridge_conveyor.png`（本体）、`bridge_conveyor_end.png`、`bridge_conveyor_bridge.png`、`bridge_conveyor_arrow.png` — 拷 Mindustry `core/assets-raw/sprites/blocks/distribution/bridge-conveyor{,-end,-bridge,-arrow}.png`（32×32，像素级一致未重绘）喵
- `textures/research/blocks/bridge_conveyor.png` — 科技树图标（同源拷贝 bridge-conveyor.png）喵

**文档/片段**
- `docs/子agent/^P1_批1B_itemBridge_lang_zh.json`、`..._lang_en.json` — lang 片段喵
- `D:\Blockdustry\任务\T30_1B_itemBridge.md` — 任务登记喵

## 二、主会话挂载点（精确）喵

### 1. `Blockdustry.java` 构造器（register）喵
在 `GateRegistrar.register(modEventBus);` 后加：
```java
import com.blockdustry.distribution.BridgeRegistrar;
...
BridgeRegistrar.register(modEventBus);
```

### 2. `BlockdustryBlocks.java` — 物流 tab 喵
`DISTRIBUTION_TAB` 的 `displayItems` 内（`output.accept(GateRegistrar.UNDERFLOW_GATE_ITEM);` 后）加：
```java
output.accept(BridgeRegistrar.BRIDGE_ITEM);
```
（需 import `com.blockdustry.distribution.BridgeRegistrar`）喵

### 3. `ResearchNodes.java` — 1 节点 parent=router 喵
在 `all()` 的 router 节点下、sorter 节点附近追加（配方照抄 Blocks.java L2106：lead×6 + copper×6；原版无显式 researchCost → 用默认公式）喵：
```java
ResearchNode.builder("bridge_conveyor")
        .parent("router")            // Mindustry SerpuloTechTree: router → itemBridge 喵
        .unlockBlock(BridgeRegistrar.BRIDGE.get())
        .buildRequirement(lead, 6)    // Mindustry itemBridge = 铅×6 铜×6 喵
        .buildRequirement(copper, 6)
        .build(),
```
（`copper = Items.COPPER_INGOT`、`lead = BlockdustryBlocks.LEAD.get()` 已在 all() 顶部定义）喵

### 4. `BlockdustryClient.java` — registerRenderers 加 1 行喵
在 `registerRenderers(EntityRenderersEvent.RegisterRenderers event)` 里加：
```java
event.registerBlockEntityRenderer(com.blockdustry.distribution.BridgeRegistrar.BRIDGE_ENTITY.get(), com.blockdustry.client.ItemBridgeBlockEntityRenderer::new);
```
（或 import 后简写）喵

### 5. `ResearchIcons.java` — nodeTexture 加 1 case 喵
在 `nodeTexture` 的 switch 里加：
```java
case "bridge_conveyor" -> ResourceLocation.tryParse(base + "bridge_conveyor.png");
```
（贴图已拷到 `textures/research/blocks/bridge_conveyor.png`）喵

### 6. lang 合并喵
把 `^P1_批1B_itemBridge_lang_zh.json` / `_lang_en.json` 内容并入 `lang/zh_cn.json` / `lang/en_us.json`：
```json
"block.blockdustry.bridge_conveyor": "传送带桥"
```
```json
"block.blockdustry.bridge_conveyor": "Bridge Conveyor"
```
（中英均取 Mindustry 官方 bundle：bridge-conveyor=传送带桥/Bridge Conveyor；描述「跨越任意地形或建筑运输物品 / Transports items over terrain or buildings」可选加）喵

### 7. 无需做的事喵
- 无组血量（size 1 非多格）喵
- 无 Jade 注册（走 BuildingInfo 通用 provider）喵
- 无右键菜单（桥配置交互暂未做，见风险）喵

## 三、机制说明（忠于 Mindustry ItemBridge / BufferedItemBridge）喵

- **配对**：放置第二个桥时，自动连接「最近放置的未配对桥」（原版 `playerPlaced → findLink` 只认静态 `lastBuild`）；要求同 block、同队、直线同轴、距离 2..range（相邻格不自动配对，原版 `!proximity.contains`）。配对双向写 link 坐标喵。
- **缓冲传输**（BufferedItemBridge.updateTransport）：自身库存 → 缓冲队列（容量 14）→ 物品到期（原版 `buffer.poll(speed)`，speed=74 Mindustry 帧 → MC 20Hz ≈ 24.667 tick 飞行延迟）→ 交付节流（原版 `timer(timerAccept, 4)` → MC ≈ 1.333 tick）→ 对端 `acceptItem + handleItem` 喵。
- **未配对/失效**：`linkValid=false` → 把库存 dump 给邻居（原版 doDump）喵。
- **接收判定**（原版 acceptItem + checkAccept）：同队 + 库存未满；来源为配对端可收；配对有效时来源不得与配对端同向（防回灌）喵。
- **视觉**：端部面板（endRegion 两端）+ 桥面条带（bridgeRegion，宽 6.5units≈0.8125 格）+ 沿桥滚动箭头（arrowRegion，间距 6units≈0.75 格，alpha=`|sin((a-time/6.2)/0.4·2π)|`）。原版 bridge-conveyor `fadeIn=moveArrows=false` → alpha 恒 1；箭头滚动动画保留（原版 draw 中箭头一直画，moveArrows 字段未在 draw 中使用）喵。
- **物品不在桥上画实体**：原版 BufferedItemBridge 不渲染 buffer 内物品（传输为缓冲 + 瞬移交接），本 port 同样不画（忠于原版）喵。

## 四、占用与交接喵
- 占用文件：见 `D:\Blockdustry\任务\T30_1B_itemBridge.md`（仅新文件，无共享文件写入）喵
- 交接给：主会话（按本清单 6 处挂载 + 编译 + 冒烟）喵
- 风险/待人工排查：
  - **速度单位换算**：原版 `speed=74`（Mindustry 逻辑帧，60Hz）与 `Time.time` 的关系按「逻辑帧=74 帧停留」移植为 MC 20Hz 的 24.667 tick；若游戏内感觉传输太快/太慢，可在 `ItemBridgeBlockEntity.FLIGHT_TICK` 处微调（74*20/60）喵
  - **跨格渲染**：桥面条带/端部 quad 顶点超出本格 [0,1]，MC BE 渲染可正常画到邻格；若对端格恰在区块边界，需两端区块同加载（原版同受 clip 半径约束）喵
  - **手动配对交互**：原版桥可用配置工具连 range 内任意有效桥；本 port 仅做「自动配对最近未配对桥」，手动点选配对未实现（MC 无对应配置工具先例，待后续）喵
  - **lastPlaced 静态字段**：跨维度/多服务器共享（原版 `ItemBridge.lastBuild` 同是块级静态字段），单世界单维度场景无影响喵

## 五、异常喵
无。
