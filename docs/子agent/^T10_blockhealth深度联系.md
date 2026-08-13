# T10 BlockHealth 深度联系（方块血量 × 多格建筑）阶段产出喵

## 目标喵
确认「钻机等 2×2/3×3 多格建筑每格独立血量、裂纹只出现在被攻击格」的现状成因，理清 BlockHealth 裂纹渲染机制，给出「整组共享血量 + 裂纹覆盖整组」的迁移方案与改动文件清单喵。

## 一、现状确认（用户观察成立，逐条代码佐证）喵

### 1. 血量存储：per-pos 的 Map，无「组」概念喵
`BlockHealthAttachments.java` 把血量挂在 **ServerLevel 附件**上，类型是 `Map<BlockPos, Float>`（键=方块坐标，值=当前血量），每个格子一条记录喵。`BlockHealthApi.getHp/getMaxHp/damage` 全部只接受**单个 BlockPos**，内部 `map.getOrDefault(pos, max)` / `map.put(pos, next)` 都是按单格读写喵。整库里**搜不到 anchor / group / 多格**任何概念喵。

### 2. 炮弹只命中一个格子喵
`BlockdustryBulletEntity.tick()`（L79-88）：
```java
// 只取炮弹中心所在的那个方块喵
BlockPos pos = this.blockPosition();
BlockEntity be = this.level().getBlockEntity(pos);
if (be instanceof BlockdustryBuildingEntity building && ...) {
    BlockHealthApi.damage(serverLevel, pos, BlockdustryArmor.applyToBuilding(building, this.damage), null, DamageType.PROJECTILE);
    this.discard();
    return;
}
```
炮弹 `noPhysics=true` 且**不做碰撞箱 raycast**，只判中心所在方块的 BE。钻机 4 格是 4 个独立 `DrillBlockEntity`，所以命中哪格就只给哪格扣血喵。

### 3. 裂纹渲染 per-pos，只覆盖被攻击格喵
- 服务端 `BlockHealthApi.syncCrack(level, pos)`（private）只对**该 pos** 发 `BlockHealthCrackPayload(pos, stage)` 给 chunk 内玩家喵。
- 客户端 `BlockHealthNetwork.handle` 只写缓存 `pos.asLong → stage`（`BlockHealthCrackCache`）喵。
- 渲染 `LevelRendererCrackMixin.renderLevel`（每帧）遍历缓存，对**每个 pos** 单独画一块 1×1×1 的原版 DESTROY 裂纹贴片（`SheetedDecalTextureGenerator` + `renderBreakingTexture`）喵。
- 因此裂纹是「贴着方块模型的裂纹贴片」，只出现在被攻击的那一个 1×1×1 格上，符合用户观察喵。

### 4. 钻机格有实心立方模型 → 裂纹贴片可见喵
`assets/blockdustry/models/block/drill_nw/ne/sw/se.json` 都是实心 16×16×16 立方体，所以 `renderBreakingTexture` 能在这格的六个面上画出裂纹喵。⚠️ 注意 `core_c.json` 等核心模型是**空模型**（只有 particle，无 elements，视觉靠 CoreBlockEntityRenderer 画 3×3×3），若给核心格刷裂纹贴片会**画不出来**（空模型没面可贴）——这是后续做「核心整组裂纹」时的坑喵。

### 5. 击破是「级联拆组」但血量是「独立扣」喵
`BlockHealthApi.breakBlock` 只 `destroyBlock(命中格)`；`BlockdustryBuildingBlock.onRemove` 检测到是组内一格被拆时，级联拆掉其余格（L148-165）。所以「整组一起塌」是**拆方块级联**实现的，与「每格独立扣血」不冲突——四格各自扣血，其中一格扣到 0 触发级联拆全组喵。

## 二、裂纹机制（BlockHealth 是怎么显示裂纹的）喵

- **不是**方块模型替换、**不是**方块状态改变、**不是** BE 渲染器、**不是** block update；而是 **LevelRenderer 每帧叠加的 DESTROY 裂纹贴片（decal）**喵。
- 阶段计算（服务端 `syncCrack`）：`frac = 当前血/最大血`；`frac >= CRACK_THRESHOLD(默认1.0) ? -1 : min(9, max(1, (int)((1-frac)*10)))`。即阈值 1.0 时任何掉血立即出裂纹，掉血越多裂纹越深（1~9 级），满血/免疫 → -1（无裂纹）喵。
- 网络：`BlockHealthCrackPayload(pos, stage)`，`PacketDistributor.sendToPlayersTrackingChunk` 按 chunk 广播，`LAST_CRACK` 静态表去重（阶段不变不重发）；玩家登录时 `sendAllCracks` 补发全部受损格喵。
- 客户端：`BlockHealthCrackCache`（`Long2IntMap`）缓存；`LevelRendererCrackMixin` 在 `renderLevel` 里遍历，32 格外剔除，`posestack.translate(pos - cam)` 后对 `level.getBlockState(pos)` 画 `ModelBakery.DESTROY_TYPES.get(stage)` 裂纹贴片喵。
- **本质：per-pos 的 decal**，库内无组、无锚点，驱动点是 `tryDamage/heal/setHp/remove` 各调一次 `syncCrack` 喵。

## 三、整组共享血量 + 裂纹覆盖整组的方案喵

### 方案分层（由浅入深）喵

#### Level 1：Blockdustry 侧把子弹伤害转发到锚点（最简，先解决「整组共享血量」）喵
`BlockdustryBulletEntity.tick()` 命中处改为：
```java
// 多格建筑统一扣锚点格，实现整组共享血量喵
BlockPos target = building.hasAnchor() ? building.getAnchor() : pos;
BlockHealthApi.damage(serverLevel, target, BlockdustryArmor.applyToBuilding(building, this.damage), null, DamageType.PROJECTILE);
```
- 优点：一行改动，锚点格扣血，锚点扣到 0 → `destroyBlock(锚点)` → `onRemove` 级联拆全组，行为正确喵。
- 缺点：裂纹只出现在锚点格（因为 `syncCrack` 只对锚点格发）；Jade 血条若不改仍读被看向格的独立血（见研究点4）；玩家**挖掘**非锚点格仍走库内 `ServerPlayerGameModeMixin` 的 `setHp(pos)` 逐格扣血，不受组转发影响喵。

#### Level 2：裂纹覆盖整组（需前置库开放一个公开方法 + Blockdustry 遍历组格）喵
BlockHealth 的裂纹是 per-pos 且 `syncCrack` 是 private。要在组内所有格显示同一裂纹阶段，需要：
- 前置库 `BlockHealthApi.java`：把裂纹阶段计算从 `syncCrack` 拆出公开方法 `syncCrackAt(ServerLevel level, BlockPos pos, float hpFraction)`（保留 `LAST_CRACK` 去重），原 `syncCrack` 调它并传 `getHpFraction(level, pos)`喵。
- Blockdustry 侧：扣锚点血后，用**锚点的血量比例**对组内每一格调 `syncCrackAt`：
```java
// 组内所有格显示锚点同一裂纹阶段喵
float frac = BlockHealthApi.getHpFraction(serverLevel, anchorPos);
for (int dx = 0; dx < size; dx++)
    for (int dz = 0; dz < size; dz++)
        BlockHealthApi.syncCrackAt(serverLevel, anchorPos.offset(dx, 0, dz), frac);
```
- ⚠️ 必须用锚点的 frac，不能对每格调 `syncCrack`：非锚点格血量仍满，`syncCrack` 会算出 frac=1 → stage=-1 → 把裂纹清掉喵。
- 效果：钻机/压机/scatter/工厂（实心立方模型）4~9 格全部显示同一裂纹，视觉上「裂纹覆盖整组」喵；核心因空模型仍画不出（见现状4）喵。

#### Level 3：前置库内建「组」抽象，统一所有伤害源（彻底方案）喵
现状所有伤害源（子弹=Blockdustry 侧；挖掘/左键=库内 `ServerPlayerGameModeMixin`；爆炸=库内 `BlockHealthExplosionHandler`；命令=BlockdustryCommands）都直接对单格 `damage/setHp`。要让它们**全部**尊重组血量且不重复扣组内多格，最干净是前置库加组注册表并在入口统一转发喵：

1. 前置库新增内存组注册表（类似 `DEFAULT_MAX_HP`，静态、运行时注册）：
   - `public static void registerGroup(ServerLevel level, BlockPos anchor, List<BlockPos> cells)` 与 `unregisterGroup(ServerLevel level, BlockPos anchor)`（内部 `ConcurrentHashMap<long 锚点key, List<BlockPos>>`，或按 level 分表）喵。
   - 在 `tryDamage` / `setHp` / `heal` / `remove` 入口做转发：若 pos 命中某组的非锚点格 → 把操作落到锚点格；扣血后对**组内所有格**用锚点 frac 调 `syncCrackAt`，实现组内统一裂纹喵。
   - 组锚点 break（`breakBlock` / `damageByExplosion`）时，级联 `remove` 组内所有格记录并 `unregisterGroup`，防旧血量残留喵。
   - 可选提供 `getGroupMaxHp`（=锚点 maxHp 或组总血）供 Jade 读取喵。
2. Blockdustry 侧注册/注销组：
   - 注册点：`BlockdustryBuildingEntity.onLoad()`（锚点格加载时，天然覆盖放置与 chunk 重载、服务端重启后重新加载）或 `BlockdustryBuildingItem.place()` 喵。
   - 注销点：`BlockdustryBuildingEntity.setRemoved()` / `BlockdustryBuildingBlock.onRemove()` 级联拆组处喵。
   - 组血量上限：用 `BlockHealthApi.setDefaultMaxHp(Block, float)` 给各多格建筑注册「整组总血」（如钻机 4×40=160），否则组 maxHp 默认就是锚点单格 40 喵。

### 组最大血量的口径（Mindustry 语义）喵
Mindustry 里每座建筑是**一个血条**（总血量固定，如 mechanical drill=160）。当前 Blockdustry 未调 `setDefaultMaxHp`，全走硬度公式 `10+10×硬度`：钻机 hardness=3 → 单格 40，四格独立共 160。若做整组共享血量，应显式 `setDefaultMaxHp(drill, 160f)`（或 `40×格数`）让组血量=原作喵。注意 `BlockHealthExplosionHandler` 遍历的是**逐个 pos**，若不做组转发，爆炸会对 4 格各扣一次 = 组血被打 4 倍，Level 3 的组转发在 `tryDamage` 里自动解决此问题喵。

## 四、研究点 4：Jade 联动应统一到锚点格喵

- 现状 `jade/ProgressServerProvider.java`（L46-49）血量条读的是**被看向格** `ba.getPosition()` 的 `getHp/getMaxHp`，而库存/电量/进度条都已统一读锚点格（`info` 变量）喵。即多格建筑从格看，血量条显示该格独立血（满血 40），与锚点格实际受损不一致喵。
- 改法：像 `info` 一样先算 `hpPos`（多格非锚点 → 锚点 pos），再 `getHp(level, hpPos)` / `getMaxHp(level, hpPos)` 喵。
- 若采用 Level 3（前置库 getHp 感知组、自动转锚点），则 Jade 无需改动喵。
- `BuildingInfoServerDataProvider` 已统一锚点（队伍/库存/电量），无需改；`BlockdustryCommands` 的 `/blockhealth` 是单格调试命令，可保留（或顺手转锚点）喵。

## 五、推荐方案与实现要点喵

### 推荐：Level 3 为主、Level 1+2 作即时降级喵
- **若追求快速验证子弹行为**：先做 Level 1（子弹转锚点）+ Level 2（公开 `syncCrackAt`、遍历组格刷裂纹）——改动小、立竿见影，但挖掘/爆炸仍逐格喵。
- **若想彻底**（挖掘、爆炸、命令、子弹、Jade 全部一致）：做 Level 3（前置库组注册表 + 入口统一转发），这是最符合 Mindustry「一座建筑一个血条」语义的长期方案喵。
- 前置库是独立 MIT 仓库（`D:\Blockdustry\子仓库(方块血量)`），改它需先 build lib 再 runClient 主工程（记忆文件已记此坑）喵。组注册表是纯内存、运行时注册，不涉及持久化，风险低；注意与 `BlockHealthBreakEvent`/`BlockHealthDamageEvent` 兼容（组转发在事件之后、`tryDamage` 内做，事件仍按命中格 pos 抛，若需按锚点抛可在 Level 3 里把事件 pos 也换成锚点）喵。

### 改动文件清单喵
前置库 `D:\Blockdustry\子仓库(方块血量)\src\main\java\com\blockdustry\lib\`：
- `BlockHealthApi.java`：
  - 拆公开 `syncCrackAt(ServerLevel, BlockPos, float hpFraction)`（保留 `LAST_CRACK` 去重）喵。
  - 新增 `registerGroup` / `unregisterGroup` / 内部组表（静态内存，Level 3）喵。
  - `tryDamage`/`setHp`/`heal`/`remove` 入口组转发 + 组内统一刷裂纹（Level 3）喵。
  - `breakBlock`/`damageByExplosion` 组锚点击破后清理组内所有格记录喵。
- 其余 lib 文件（Attachments/Handler/Mixin/Net）原则上不动；爆炸/挖掘因转发在 `tryDamage` 内自动生效，无需改喵。

Blockdustry `D:\Blockdustry\仓库\src\main\java\com\blockdustry\`：
- `entities/BlockdustryBulletEntity.java`：命中转锚点 `damage`（Level 1；Level 3 有 lib 转发时仍建议显式转锚点做防御）喵。
- `building/BlockdustryBuildingEntity.java`：`onLoad` 锚点格注册组、`setRemoved` 注销组（Level 3）喵。
- `building/BlockdustryBuildingBlock.java`：`onRemove` 级联拆组时清理组注册与各格 HP 记录（Level 3）喵。
- `building/BlockdustryBuildingItem.java`：`place` 时同步注册组（可选，onLoad 已覆盖放置）喵。
- `building/BlockdustryBlocks.java`：给多格建筑调 `BlockHealthApi.setDefaultMaxHp(block, 组总血)`（如 drill=160）喵。
- `jade/ProgressServerProvider.java`：血量条读锚点格 HP（Level 2 必改；Level 3 若 getHp 感知组则可免）喵。

### 风险/待人工排查喵
- 核心（3×3、空方块模型）裂纹贴片画不出，需决定「核心是否要裂纹」，若要得给核心格加可见模型或改 BER 画裂纹喵。
- 组注册表为纯内存，跨维度/服务器重启由 `onLoad` 重新注册兜底；务必在 `setRemoved`/`onRemove` 注销，防位置复用后残留旧组喵。
- `BlockHealthBreakEvent` 当前按命中格 pos 抛，Level 3 转发后事件 pos 语义需确认（建议统一为锚点，供主会话决策）喵。

## 占用与交接喵
- 占用文件: 见任务登记 `D:\Blockdustry\任务\T10_blockhealth深度联系.md`（已读 lib 全部、bullet、jade、building 相关、钻机模型）喵
- 交接给: 主会话（决策 Level 1/2/3 选哪档，再派实现任务）喵
- 风险/待人工排查: 核心空模型裂纹不可见、事件 pos 语义、组注册表生命周期喵

## 异常喵
无喵
