# T21 BlockHealth 整组血量改造 阶段产出喵

## 目标喵
实现 T10 研究（Level 3）方案：多格建筑（钻机 2×2 / 核心 3×3 等）整组共享血量 + 裂纹覆盖整组，前置库与主工程协同改造，主工程 `compileJava` 通过喵。

## 前置库改动（`D:\Blockdustry\子仓库(方块血量)`，需重新 build 出 jar）喵

### 1. `BlockHealthApi.java`（核心：组注册表 + 全入口统一转发）喵
- 新增组注册表（静态内存、运行时注册、不落盘）喵：
  - `registerGroup(ServerLevel, BlockPos anchorPos, Set<BlockPos> cells)` / `unregisterGroup(ServerLevel, BlockPos)` 喵。
  - 内部两张表：`GROUP_CELLS`（level → 锚点key → 组内全部格）、`CELL_TO_ANCHOR`（level → 格key → 锚点key，O(1) 反查）喵。
- 全伤害/读写入口统一转发到锚点（挖掘、爆炸、子弹、命令、Jade 全一致）喵：
  - `getHp(Level, pos)`：组内格 → 返回锚点血量喵。
  - `damage(...)`：先 `groupAnchor` 转发到锚点再抛 `BlockHealthDamageEvent` / 扣血喵。
  - `tryDamage` / `damageByExplosion` / `heal` / `setHp`：统一转发锚点喵。
  - `remove(ServerLevel, pos)`：转发锚点后**清整组**（清全部格 HP 记录 + 发取消裂纹 + `unregisterGroup`）喵。
- 整组裂纹：`syncGroupCracks(level, anchorPos)` 用**锚点血量比例**对组内全部格刷同一裂纹阶段（`syncCrackAt`），并把原 private `syncCrack` 拆出公开 `syncCrackAt(ServerLevel, BlockPos, float hpFraction)` 喵。
- `sendAllCracks`（玩家登录补发）把组内各格一并推给玩家，避免整组裂纹缺格喵。
- 公开 `isGroupCell(ServerLevel, BlockPos)`（非锚点组成员？）与 `groupAnchorOf(ServerLevel, BlockPos)` 供爆炸处理器跳过从格喵。

### 2. `BlockHealthExplosionHandler.java`
- 爆炸遍历时 `if (BlockHealthApi.isGroupCell(serverLevel, pos)) continue;`：非锚点格跳过，爆炸伤害由锚点格承受**一次**（避免同组多格各扣一次 = 组血被打 4 倍）喵。

### 3. 构建
- 已在 `D:\Blockdustry\子仓库(方块血量)` 执行 `./gradlew build`，产出 `build/libs/blockhealth-1.0.0.jar`（Aug 13 15:52）喵。

## 主工程改动（`D:\Blockdustry\仓库`）喵

### 1. `building/BlockdustryBuildingEntity.java`（组注册/注销）
- 新增字段 `boolean healthGroupRegistered` 喵。
- `onLoad` / `clearRemoved`（服务端）：注册建筑管理器后调 `registerHealthGroup()`；`setRemoved`：注销后调 `unregisterHealthGroup()` 喵。
- `registerHealthGroup()`：仅当 `hasAnchor() && isAnchor() && getSize()>1` 时把「锚点 + size×size 全部格」注册进 `BlockHealthApi.registerGroup` 喵。
  - ⚠️ 关键时序坑：fresh 放置时 onLoad 早于 `setAnchor` 触发，各格 anchor 皆 null 会误判为锚点；故 onLoad 只处理「已从 NBT 载入 anchor 的 chunk 重载」，fresh 放置由 place 显式调 `registerHealthGroupExplicit()` 喵。
- 新增公开 `registerHealthGroupExplicit()` 供放置逻辑调用喵。

### 2. `building/BlockdustryBuildingItem.java`（fresh 放置显式注册组）
- 多格放置循环设完各格 anchor 后，对 `base` 的 BE 调 `baseBe.registerHealthGroupExplicit()` 喵。

### 3. `building/BlockdustryBuildingBlock.java`（拆除注销组）
- `onRemove` 级联拆组前调 `BlockHealthApi.unregisterGroup((ServerLevel) level, anchor)`（幂等）喵。

### 4. `entities/BlockdustryBulletEntity.java`（子弹命中转发锚点）
- 命中敌对建筑时 `BlockPos target = building.hasAnchor() ? building.getAnchor() : pos;` 再 `BlockHealthApi.damage(serverLevel, target, ...)` 喵（显式转发，防御性；lib 组转发生效时也正确）喵。

### 5. `jade/ProgressServerProvider.java`（血量条统一读锚点格）
- 新增 `BlockPos hpPos`：多格非锚点 → 锚点 pos，血量条 `getHp/getMaxHp(level, hpPos)` 喵（lib `getHp` 组转发兜底，这里显式化更清晰）喵。

### 6. `building/BlockdustryBlocks.java` + `Blockdustry.java`（组总血）
- 新增 `registerBlockHealthDefaults()`：对钻机/石墨压机/对空炮塔/核心/单位工厂调 `registerGroupMaxHp(block, size)`，用 `BlockHealthApi.getMaxHpForState(state, null, null)` 算单格血 × 格数注册 `setDefaultMaxHp`（保持与原每格独立血总血一致：钻机 160、压机/scatter 200、核心 1170、工厂 540）喵。
- `Blockdustry.java` 的 `commonSetup` 调用 `BlockdustryBlocks.registerBlockHealthDefaults()`（双端执行，客户端 tooltip/挖掘进度也用）喵。

### 7. 编译
- 主工程 `./gradlew compileJava` 通过（BUILD SUCCESSFUL，仅既有 deprecation 警告）喵。

## 构建/运行顺序喵
1. 改前置库后：`cd "D:\Blockdustry\子仓库(方块血量)" && ./gradlew build` 出新 jar 喵。
2. 主工程：`cd "D:\Blockdustry\仓库" && ./gradlew compileJava`（依赖 build/libs/blockhealth-1.0.0.jar）喵。

## 验证点喵
- 放置 2×2 钻机：`/blockhealth query <任一格>` 应显示同一血量（锚点血），四格共享 160 喵。
- 炮弹命中钻机**任意一格**：整组四格同时出现裂纹（同一阶段），而不是只有被击格喵；连续命中至 0 整组塌落喵。
- 挖掘钻机任意一格：血条/裂纹以整组 160 计，任一格挖到 0 整组塌落喵。
- TNT 爆炸：钻机只被扣一次（非 4 次），整组裂纹同步喵。
- Jade 准星指向钻机从格：血量条显示整组血量（与锚点一致）喵。
- 拆除/级联后 `/blockhealth query` 旧格不再有残留记录（组已注销）喵。

## 占用与交接喵
- 占用文件: 见上改动清单（前置库 2 个 java + 主工程 6 个 java + 2 个构建产物）喵
- 交接给: 主会话（如需 runClient 实测；改动已 compileJava 通过）喵
- 风险/待人工排查: 核心方块模型为空（`core_c.json` 无 elements），裂纹贴片在核心格画不出（BER 画 3×3×3），需后续决定核心裂纹方案喵；旧档无 anchor 的多格建筑不回退组血量（按格独立），属可接受降级喵

## 异常喵
无喵
