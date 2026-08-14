# ^P1 批1F titaniumWall/door 整合清单 —— 钛墙/大型钛墙 + 门/大门迁移喵

> 任务登记: `D:\Blockdustry\任务\T44_1F_B_钛墙门.md`。数据忠于 Mindustry 原版，贴图拷原版不重绘，门开关动画/特效研究见 `docs/子agent/^P1_批1F_door开关动画研究.md` 喵。

## 零、数据核对结论（Blocks.java 实测）喵
| 块 | 类 | size | 配方 | health | strength（10+10×s） | 开关特效 |
|---|---|---|---|---|---|---|
| titanium-wall L1718-1721 | Wall | 1 | 钛×6 | 110×4=440 | 43 | 无（静态） |
| titanium-wall-large L1723-1727 | Wall | 2 | 钛×24 | 110×4×4=1760 | 43 | 无（静态） |
| door L1785-1788 | Door | 1 | 钛×6+硅×4 | 100×4=400 | 39 | dooropen/doorclose + Sounds.door |
| door-large L1790-1796 | Door | 2 | 钛×24+硅×16 | 100×4×4=1600 | 39 | dooropenlarge/doorcloselarge + Sounds.door |

- 官方 bundle 名：钛墙 / 大型钛墙 / 门 / 大门（en：Titanium Wall / Large Titanium Wall / Door / Large Door）喵。

## 一、已交付（独立新文件，含渲染/音效，主会话勿重复注册）喵

| 文件 | 说明 |
|---|---|
| `src/main/java/com/blockdustry/defense/DefenseRegistrar.java` | 自包含注册类（模板 ContainerRegistrar/FuseArcRegistrar）：4 方块+物品+2 BE 类型+门 SoundEvent；wall strength43、door strength39 喵 |
| `src/main/java/com/blockdustry/defense/WallBlockEntity.java` | 墙体 BE（Mindustry Wall.WallBuild）：空 tickAnchor，承载队伍/装甲/组血喵 |
| `src/main/java/com/blockdustry/defense/DoorBlock.java` | 门方块（Mindustry Door）：右键开关（服务端 toggle）+ `getCollisionShape` 开门空形状（可通行）；`getShape` 保持满形状保证开门仍可点选喵 |
| `src/main/java/com/blockdustry/defense/DoorBlockEntity.java` | 门 BE（Mindustry DoorBuild）：open 状态 NBT 持久化、连锁门 BFS 同开同关、门内有实体不能关、60 tick 冷却、播音效+特效时刻同步喵 |
| `src/main/java/com/blockdustry/client/DoorBlockEntityRenderer.java` | 门渲染：整块立方体关=door/开=door_open（entityCutoutNoCull 透明门洞）+ 方块轮廓特效（Fx.dooropen 外扩/close 内缩，10 tick 淡出）；getRenderBoundingBox 扩到整组喵 |
| `assets/blockdustry/blockstates/titanium_wall.json` | 全 9 corner → 单 cube 模型喵 |
| `assets/blockdustry/blockstates/titanium_wall_large.json` | 全 9 corner → 四象限模型喵 |
| `assets/blockdustry/blockstates/door.json` / `door_large.json` | 全 9 corner → 空模型（particle 门贴图），世界渲染交给 BER 喵 |
| `assets/blockdustry/models/block/titanium_wall{,_large,_large_{nw,ne,sw,se}}.json` | 墙 cube / 大墙四象限 cube（顶面贴墙图、侧面 stone，UV 每格 8×8/16）喵 |
| `assets/blockdustry/models/block/door{,_large}.json` | 门/大门显示 cube（物品栏图标用，全 6 面门图）喵 |
| `assets/blockdustry/models/block/door_nw.json` + `door_large_{nw,ne,sw,se}.json` | 空模型（particle only，blockstate 用）喵 |
| `assets/blockdustry/models/item/titanium_wall{,_large}.json` + `door{,_large}.json` | 物品模型（门走显示 cube）喵 |
| `assets/blockdustry/textures/block/titanium_wall{,_large}.png` | 拷原版 titanium-wall.png(32×32)/titanium-wall-large.png(64×64)，md5 与原版一致喵 |
| `assets/blockdustry/textures/block/door{,_open,_large,_large_open}.png` | 拷原版 4 张门贴图（door-open/large-open 含透明门洞），md5 一致喵 |
| `assets/blockdustry/textures/research/blocks/titanium_wall{,_large}.png` + `door{,_large}.png` | 科技树图标（拷原版同图）喵 |
| `assets/blockdustry/sounds/door.ogg` + `sounds.json` | 拷原版 Sounds.door（door.ogg），注册音效事件 blockdustry:door；**sounds.json 为本任务新建**，后续任务若加音效需在其上追加而非覆盖喵 |

## 二、主会话挂载点（按序合并，全部必须）喵

### 1. register — `Blockdustry.java` 构造器
在 `com.blockdustry.building.PlastaniumCompressorRegistrar.register(modEventBus);` 之后加一行：
```java
com.blockdustry.defense.DefenseRegistrar.register(modEventBus);
```

### 2. tab — `BlockdustryBlocks.java` DEFENSE_TAB（Mindustry Category.defense 归防御 tab）
在 `output.accept(FuseArcRegistrar.ARC_ITEM);` 之后加 4 行：
```java
output.accept(com.blockdustry.defense.DefenseRegistrar.TITANIUM_WALL_ITEM);
output.accept(com.blockdustry.defense.DefenseRegistrar.TITANIUM_WALL_LARGE_ITEM);
output.accept(com.blockdustry.defense.DefenseRegistrar.DOOR_ITEM);
output.accept(com.blockdustry.defense.DefenseRegistrar.DOOR_LARGE_ITEM);
```

### 3. ResearchNodes — `ResearchNodes.java` all()，加 4 节点
> 原版 SerpuloTechTree L264-279：duo→copperWall→copperWallLarge→titaniumWall→{titaniumWallLarge, door→{doorLarge}}。copperWall 未迁移，故 titanium_wall 临时挂已存在的 turret（duo）下；copper 墙迁移后可改回原版链喵。
```java
ResearchNode.builder("titanium_wall")
        .parent("turret")                 // 原版 parent=copperWallLarge（未迁，先挂 duo 下）喵
        .unlockBlock(com.blockdustry.defense.DefenseRegistrar.TITANIUM_WALL.get())
        .buildRequirement(titanium, 6)    // Mindustry titanium-wall = 钛×6 喵
        .build(),
ResearchNode.builder("titanium_wall_large")
        .parent("titanium_wall")
        .unlockBlock(com.blockdustry.defense.DefenseRegistrar.TITANIUM_WALL_LARGE.get())
        .buildRequirement(titanium, 24)   // Mindustry titanium-wall-large = 4×钛×6 喵
        .build(),
ResearchNode.builder("door")
        .parent("titanium_wall")
        .unlockBlock(com.blockdustry.defense.DefenseRegistrar.DOOR.get())
        .buildRequirement(titanium, 6)    // Mindustry door = 钛×6 硅×4 喵
        .buildRequirement(silicon, 4)
        .build(),
ResearchNode.builder("door_large")
        .parent("door")
        .unlockBlock(com.blockdustry.defense.DefenseRegistrar.DOOR_LARGE.get())
        .buildRequirement(titanium, 24)   // Mindustry door-large = 4×(钛6+硅4) 喵
        .buildRequirement(silicon, 16)
        .build(),
```
> 注：需在 `all()` 顶部材料映射里取 `titanium`（`com.blockdustry.item.BlockdustryItems.TITANIUM.get()`）与 `silicon`（`BlockdustryBlocks.SILICON.get()`）局部变量；若该文件无 titanium/silicon 变量，参照 laser_drill 节点的写法用 `com.blockdustry.item.BlockdustryItems.TITANIUM.get()` 内联喵。

### 4. 渲染 — `BlockdustryClient.java` registerRenderers
在 plastanium-compressor 渲染器行之后加一行：
```java
event.registerBlockEntityRenderer(com.blockdustry.defense.DefenseRegistrar.DOOR_ENTITY.get(), com.blockdustry.client.DoorBlockEntityRenderer::new);
```
> 墙无渲染器（方块模型直渲），门有渲染器（BER 画门体+开关特效）喵。

### 5. ResearchIcons — `ResearchIcons.java` nodeTexture
在 `case "plastanium_compressor" -> ...` 之后加 4 行：
```java
case "titanium_wall" -> ResourceLocation.tryParse(base + "titanium_wall.png");
case "titanium_wall_large" -> ResourceLocation.tryParse(base + "titanium_wall_large.png");
case "door" -> ResourceLocation.tryParse(base + "door.png");
case "door_large" -> ResourceLocation.tryParse(base + "door_large.png");
```

### 6. 多格组血量 — `BlockdustryBlocks.java` registerBlockHealthDefaults
在 blast-drill 行之后加 2 行：
```java
registerGroupMaxHp(com.blockdustry.defense.DefenseRegistrar.TITANIUM_WALL_LARGE.get(), 2); // 大型钛墙 2×2 组血 1760（strength 43 → 单格 440）喵
registerGroupMaxHp(com.blockdustry.defense.DefenseRegistrar.DOOR_LARGE.get(), 2);          // 大门 2×2 组血 1600（strength 39 → 单格 400）喵
```

### 7. lang — `lang/en_us.json` + `lang/zh_cn.json` 追加
en_us:
```json
"block.blockdustry.titanium_wall": "Titanium Wall",
"block.blockdustry.titanium_wall_large": "Large Titanium Wall",
"block.blockdustry.door": "Door",
"block.blockdustry.door_large": "Large Door"
```
zh_cn:
```json
"block.blockdustry.titanium_wall": "钛墙",
"block.blockdustry.titanium_wall_large": "大型钛墙",
"block.blockdustry.door": "门",
"block.blockdustry.door_large": "大门"
```
（官方 bundle_zh_CN：钛墙/大型钛墙/门/大门；en：Titanium Wall/Large Titanium Wall/Door/Large Door 喵）

## 三、核心数据库登记（协调者要求字段）喵
- 本批对应 Mindustry 注册名：titanium-wall / titanium-wall-large / door / door-large
- 中文官方名：钛墙 / 大型钛墙 / 门 / 大门
- 类别：墙体（Category.defense，归防御 tab）
- 依赖：无（不依赖电力/液体/逻辑）；size 1×1 与 2×2（2×2 无需扩 Corner，当前 Corner 已支持）
- 主会话请把核心数据库 4.6 墙体 4 项从「未迁移」移到「已迁移」，更新建筑计数 +23→+27，并同步「迁移中」批次状态喵。

## 四、行为要点（验收对照）喵
- 放置：钛墙 1×1 实心方块；大型钛墙 2×2 四象限模型 + 组碰撞 + 组血 1760；门/大门同款放置 + 组血 400/1600 喵
- 门开关：空手右键（同队/derelict 可交互）→ 开/关 + 连锁同开同关 + Sounds.door 音效 + 方块轮廓特效（开外扩/关内缩 10 tick）；开门可通行、关门实心；开门中门内有实体不能关；右键冷却 60 tick 喵
- 门渲染：关=door.png 实心门、开=door-open.png 透明门洞（entityCutoutNoCull）喵
- 科技树：titanium_wall 挂 turret 下（原版链待 copper 墙迁移后改），titanium_wall_large/door/door_large 依序子节点喵

## 五、风险 / 待人工排查喵
- **编译阻塞（外部）**：`./gradlew compileJava` 失败，唯一错误在**另一子任务**的 `building/MenderBlockEntity.java` L218 `NbtUtils.readBlockPos(list.getCompound(i))`（1.21.1 签名需 `(CompoundTag, String)`）。本任务 5 个 Java 文件无任何报错（同一次 javac 已类型检查）。**未擅改他人占用文件**，请协调者通知对应任务修复或由主会话修喵。
- **门未做逻辑控制**（Mindustry `control(LAccess.enabled)` / `sense(LAccess.enabled)`）：Blockdustry 无逻辑 VM（4.11 整组 P3 未迁），属已知简化喵。
- **pathfinder.updateTile 未实现**：MC mob 寻路周期性重算；关门后 mob 不立即绕行属已知简化喵。
- **sounds.json 为本任务新建**：后续任何任务加音效需在既有 sounds.json 上追加键，勿整文件覆盖喵。
- 主会话合并后请跑 `./gradlew compileJava` + 游戏冒烟：防御 tab 出现钛墙/大型钛墙/门/大门；放置 2×2 组血正确；右键门开关有音效+方块轮廓特效、开门可穿行、连锁门同开同关、门内有生物不能关；科技树 turret 下新节点喵。
