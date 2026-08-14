# ^P1 批1F copper+scrap 墙整合清单（T43_1F_A）喵

> 任务：迁移 Mindustry **copper-wall / copper-wall-large / scrap-wall / scrap-wall-large**（类 Wall，静态防御墙）喵
> 状态：已交付，待主会话合并。自包含代码/资源均已落地，**未触碰任何共享注册文件**喵

## 一、核心数据库登记喵
> 主会话整合时请把以下 4 项在 `docs/核心数据库.md` 4.6 墙体节由「未迁移」移到「已迁移」，并更新「二、已迁移内容」与「总览计数」（建筑已迁移 +4）喵

| Mindustry注册名 | 中文官方名 | 类别 | 类 | 依赖 | 关键数值 |
|---|---|---|---|---|---|
| copper-wall | 铜墙 | 墙体 | Wall | 无电力/液体 | copper6；health 320；size 1；researchCostMultiplier 0.1f |
| copper-wall-large | 大型铜墙 | 墙体 | Wall | 无 | 4×copper6；health 1280；size 2 |
| scrap-wall | 废墙 | 墙体 | Wall | 无 | scrap6；health 240；size 1；variants=5；buildCostMultiplier 4f |
| scrap-wall-large | 大型废墙 | 墙体 | Wall | 无 | 4×scrap6；health 960；size 2；variants=4 |

## 二、新建文件清单（均已落地，唯一文件名，不与其他任务冲突）喵

### Java（包 `com.blockdustry.defense`）
- `src/main/java/com/blockdustry/defense/WallBlock.java` — 防御墙方块基类（extends BlockdustryBuildingBlock，size 1/2，高 1 层）喵
- `src/main/java/com/blockdustry/defense/WallBlockEntity.java` — 共享墙 BE（extends BlockdustryBuildingEntity，空 tickAnchor，已由协调者改为引用 `DefenseRegistrar.WALL_ENTITY`）喵
- ⚠️ `WallRegistrar.java` **已删除**：与 T44 的 `DefenseRegistrar` 重复注册同名 `wall` BE 类型会启动冲突，且协调者已把 `WallBlockEntity` 指向 `DefenseRegistrar.WALL_ENTITY`。铜/废墙并入 `DefenseRegistrar`（见第三节）喵

### 资源 `src/main/resources/assets/blockdustry/`
- `blockstates/copper_wall.json`、`copper_wall_large.json`、`scrap_wall.json`、`scrap_wall_large.json`
- `models/block/copper_wall.json`、`copper_wall_large.json`、`copper_wall_large_{nw,ne,sw,se}.json`、`scrap_wall.json`、`scrap_wall_large.json`、`scrap_wall_large_{nw,ne,sw,se}.json`
- `models/item/copper_wall.json`、`copper_wall_large.json`、`scrap_wall.json`、`scrap_wall_large.json`
- `textures/block/copper_wall.png`、`copper_wall_large.png`、`scrap_wall.png`、`scrap_wall_large.png`（**拷原版 PNG，未重绘**）
- `textures/research/blocks/copper_wall.png`、`copper_wall_large.png`、`scrap_wall.png`、`scrap_wall_large.png`（研究图标，拷原版）
- 贴图来源：`Mindustry/core/assets-raw/sprites/blocks/walls/`。`scrap_wall.png` = `scrap-wall1.png`、`scrap_wall_large.png` = `scrap-wall-large1.png`（原版 variants=5/4 随机贴图，MC 静态方块取默认变体 1，**未实现随机变体**，属已知简化）喵

### 模型说明
- 1×1：六面整张墙贴图，uv [0,0,16,16] 喵
- 2×2 四象限：顶面按象限裁剪大型贴图（nw[0,0,8,8]/ne[8,0,16,8]/sw[0,8,8,16]/se[8,8,16,16]）；南北面左右半裁剪拼出整面 2 格宽墙；东西面用 1×1 贴图全 uv。UV 均在 [0,16] 合法范围（坑文档·方块模型 §3/§4）喵

## 三、DefenseRegistrar 合并指令（T43 → T44 的 `com.blockdustry.defense.DefenseRegistrar`）喵

在 `DefenseRegistrar` 内补以下内容（主会话合并，勿覆盖 T44 钛墙/门部分）喵：

```java
// —— 方块（铜墙/废墙，批1F T43 并入）——
public static final DeferredBlock<Block> COPPER_WALL =
        BLOCKS.register("copper_wall", () -> wall(31f, 1));
public static final DeferredBlock<Block> COPPER_WALL_LARGE =
        BLOCKS.register("copper_wall_large", () -> wall(31f, 2));
public static final DeferredBlock<Block> SCRAP_WALL =
        BLOCKS.register("scrap_wall", () -> wall(23f, 1));
public static final DeferredBlock<Block> SCRAP_WALL_LARGE =
        BLOCKS.register("scrap_wall_large", () -> wall(23f, 2));

// —— 方块物品 ——
public static final DeferredItem<BlockdustryBuildingItem> COPPER_WALL_ITEM =
        ITEMS.register("copper_wall", () -> new BlockdustryBuildingItem(COPPER_WALL.get(), new Item.Properties(), 1));
public static final DeferredItem<BlockdustryBuildingItem> COPPER_WALL_LARGE_ITEM =
        ITEMS.register("copper_wall_large", () -> new BlockdustryBuildingItem(COPPER_WALL_LARGE.get(), new Item.Properties(), 2));
public static final DeferredItem<BlockdustryBuildingItem> SCRAP_WALL_ITEM =
        ITEMS.register("scrap_wall", () -> new BlockdustryBuildingItem(SCRAP_WALL.get(), new Item.Properties(), 1));
public static final DeferredItem<BlockdustryBuildingItem> SCRAP_WALL_LARGE_ITEM =
        ITEMS.register("scrap_wall_large", () -> new BlockdustryBuildingItem(SCRAP_WALL_LARGE.get(), new Item.Properties(), 2));

// 工厂方法：strength 31 → 单格血 320（铜）；strength 23 → 单格血 240（废料）；组血 = 单格 × size² 喵
private static BlockdustryBuildingBlock wall(float strength, int size) {
    return new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(strength),
            () -> WALL_ENTITY.get(), size);
}
```

并把 `WALL_ENTITY` 的合法方块列表扩到全部 8 墙（原 T44 两钛墙 + 本任务 4 墙）喵：

```java
public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WallBlockEntity>> WALL_ENTITY =
        BLOCK_ENTITY_TYPES.register("wall",
                () -> BlockEntityType.Builder.of(WallBlockEntity::new,
                        TITANIUM_WALL.get(), TITANIUM_WALL_LARGE.get(),
                        COPPER_WALL.get(), COPPER_WALL_LARGE.get(),
                        SCRAP_WALL.get(), SCRAP_WALL_LARGE.get()).build(null));
```

## 四、主会话挂载点（精确到行/方法）喵

1. **`Blockdustry.java` 构造器**（~L61 后，与其他 registrar 并排）加一行：
   `com.blockdustry.defense.DefenseRegistrar.register(modEventBus);`
   （若 T44 已加，跳过；铜/废墙随 DefenseRegistrar 一起挂载）喵

2. **`BlockdustryBlocks.registerBlockHealthDefaults()`**（L383-397）追加 4 行组血量（strength 已使单格血 320/240）喵：
   ```java
   registerGroupMaxHp(com.blockdustry.defense.DefenseRegistrar.COPPER_WALL.get(), 1);        // 组血 320
   registerGroupMaxHp(com.blockdustry.defense.DefenseRegistrar.COPPER_WALL_LARGE.get(), 2);  // 组血 1280
   registerGroupMaxHp(com.blockdustry.defense.DefenseRegistrar.SCRAP_WALL.get(), 1);         // 组血 240
   registerGroupMaxHp(com.blockdustry.defense.DefenseRegistrar.SCRAP_WALL_LARGE.get(), 2);   // 组血 960
   ```

3. **`BlockdustryBlocks` 创造栏 DEFENSE_TAB**（L216-227 `displayItems` 内）追加 4 个物品喵：
   ```java
   output.accept(com.blockdustry.defense.DefenseRegistrar.COPPER_WALL_ITEM);
   output.accept(com.blockdustry.defense.DefenseRegistrar.COPPER_WALL_LARGE_ITEM);
   output.accept(com.blockdustry.defense.DefenseRegistrar.SCRAP_WALL_ITEM);
   output.accept(com.blockdustry.defense.DefenseRegistrar.SCRAP_WALL_LARGE_ITEM);
   ```

4. **`ResearchNodes.java`**（`all()` 列表末尾，`turret` 已存在）追加 4 节点，科技链照抄 SerpuloTechTree L264-269（duo→copperWall→copperWallLarge→scrapWall→scrapWallLarge）喵：
   ```java
   // —— 批1F 墙体：turret(duo)→copper_wall→copper_wall_large→scrap_wall→scrap_wall_large（SerpuloTechTree L264-269）——
   ResearchNode.builder("copper_wall")
           .parent("turret")
           .unlockBlock(com.blockdustry.defense.DefenseRegistrar.COPPER_WALL.get())
           .costMultiplier(0.1f)              // Mindustry copperWall researchCostMultiplier = 0.1 喵
           .buildRequirement(copper, 6)       // Mindustry copper-wall = 铜×6 喵
           .build(),
   ResearchNode.builder("copper_wall_large")
           .parent("copper_wall")
           .unlockBlock(com.blockdustry.defense.DefenseRegistrar.COPPER_WALL_LARGE.get())
           .buildRequirement(copper, 24)      // Mindustry copper-wall-large = 4×铜×6 喵
           .build(),
   ResearchNode.builder("scrap_wall")
           .parent("copper_wall_large")
           .unlockBlock(com.blockdustry.defense.DefenseRegistrar.SCRAP_WALL.get())
           .buildRequirement(com.blockdustry.item.BlockdustryItems.SCRAP.get(), 6)  // Mindustry scrap-wall = 废料×6 喵
           .build(),
   ResearchNode.builder("scrap_wall_large")
           .parent("scrap_wall")
           .unlockBlock(com.blockdustry.defense.DefenseRegistrar.SCRAP_WALL_LARGE.get())
           .buildRequirement(com.blockdustry.item.BlockdustryItems.SCRAP.get(), 24) // Mindustry scrap-wall-large = 4×废料×6 喵
           .build(),
   ```
   > 注意：Blockdustry 的 costMultiplier 沿父链累乘，故 copper_wall_large/scrap_wall/scrap_wall_large 有效倍率均继承 0.1，
   > 有效研究成本约 铜墙 20 / 大型铜墙 70 / 废墙 20 / 大型废墙 70（公式 60×mult + 建造量^1.11×20×mult）。
   > Mindustry 各墙默认 researchCostMultiplier=1（不继承），若需更贴原版可改用 `.researchCost(...)` 显式覆盖——是否调整由主会话定夺喵

5. **`ResearchIcons.java`**（`nodeTexture` switch，L26-57）追加 4 case 喵：
   ```java
   case "copper_wall" -> ResourceLocation.tryParse(base + "copper_wall.png");
   case "copper_wall_large" -> ResourceLocation.tryParse(base + "copper_wall_large.png");
   case "scrap_wall" -> ResourceLocation.tryParse(base + "scrap_wall.png");
   case "scrap_wall_large" -> ResourceLocation.tryParse(base + "scrap_wall_large.png");
   ```

6. **lang**（`lang/zh_cn.json` + `lang/en_us.json`）合并 `^P1_批1F_copperScrapWall_lang_{zh,en}.json` 的 4 条（中文用原版官方 bundle：铜墙/大型铜墙/废墙/大型废墙，严禁自创）喵

## 五、数据核对（数据不串）喵

| 块 | 原版 health | 原版 armor | strength | 单格血(公式 10+10×strength) | 组血(size²×单格) |
|---|---|---|---|---|---|
| copper-wall | 320 | 0 | 31 | 320 | 320 |
| copper-wall-large | 1280 | 0 | 31 | 320 | 1280 |
| scrap-wall | 240 | 0 | 23 | 240 | 240 |
| scrap-wall-large | 960 | 0 | 23 | 240 | 960 |

- armor 均 0（原版 Block.armor 默认 0f，铜/废墙未设置），**未与钛墙(440)/门(400) 混淆**喵
- 特效：原版 Wall 纯静态无动画/特效（scrap 仅多贴图变体），**未自编任何特效**；受击裂纹走 BlockHealth 既有系统喵
- 已知简化：scrap 随机变体未实现；scrap buildCostMultiplier 4f（Mindustry 建造耗时系数）MC 无等价项未映射喵

## ⚠️ 待主会话确认事项喵
- **墙物品插入**：协调者改 `WallBlockEntity` 时移除了 `acceptsItem(Item)→false` 覆盖（据协调意图不还原）。
  基类 `BlockdustryBuildingEntity.acceptsItem` 在库存空时返回 true，若传送带对墙调用 `dumpItem`，物品会被塞入墙的隐藏 10 格库存。
  钛墙/门（T44）同样共用此 BE，建议主会话在合并时统一评估：要么恢复 `acceptsItem→false`，要么在基类加被动标记；否则需确认墙不受物品插入影响喵

## 六、自查记录喵
- `./gradlew compileJava`：本任务三文件（WallBlock/WallBlockEntity/原WallRegistrar）均编译通过（.class 已生成）。
  全量编译仍被并行任务遗留错误阻塞：`building/ForceProjectorRegistrar.java L35,37`、`building/MenderBlockEntity.java L218`（与本任务无关）喵
- 已删除 `WallRegistrar.java` 消除与 DefenseRegistrar 的 `wall` BE 重复注册冲突喵
