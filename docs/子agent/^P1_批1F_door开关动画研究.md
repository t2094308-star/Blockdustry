# ^P1 批1F door 开关动画/状态切换研究（忠实原版 Door.java + Fx）喵

> 任务 T44_1F_B_钛墙门。研究对象：`Mindustry/core/src/mindustry/world/blocks/defense/Door.java`、`Fx.java` L2594-2612、`Sounds.java`、`Wall.java` 喵。
> 结论：door 是**静态贴图切换 + 一次性方块轮廓特效 + 音效**，不是持续动画；钛墙/大型钛墙是纯静态方块无动画喵。

## 一、Door 机制逐条记录（原版 Door.java，行号实测）喵

### 1. 类继承与构造（L21-59）喵
- `Door extends Wall`；构造设 `solid = false`、`solidifes = true`、`consumesTap = true` 喵。
- 字段：`timerToggle`（timers++ 自增）、`openfx = Fx.dooropen`、`closefx = Fx.doorclose`、`doorSound = Sounds.door`、`chainEffect = false`、`@Load("@-open") openRegion`（开态贴图 door-open）喵。
- door-large 覆写 `openfx = Fx.dooropenlarge`、`closefx = Fx.doorcloselarge`（Blocks.java L1792-1793）喵。

### 2. 状态切换入口（config 处理器，L38-58）喵
- 点击（tapped）→ `configure(!open)` → 触发 `config(Boolean.class, ...)` 喵。
- 处理器行为：
  1. 非 world 生成期：`doorSound.at(base)` 播音效 + `base.effect()` 出特效喵。
  2. 遍历连锁门（`chained`，空则 base 自身）：跳过「关门时门内有实体」（`Units.anyEntities(tile) && !open`）与「已在目标状态」（`entity.open == open`）喵。
  3. `chainEffect=false` 时仅 base 出特效，连锁门只切状态喵。
  4. 每扇 `entity.open = open; entity.recache(); pathfinder.updateTile(entity.tile)`（刷新寻路）喵。

### 3. 特效方向（DoorBuild.effect，L110-112）喵
- `(open ? closefx : openfx).at(this, size)` —— 切换瞬间 base.open 仍是旧态：旧态开→closefx（关门），旧态关→openfx（开门）喵。
- `Effect.at(this, size)` 把 `size` 传作 `e.rotation`，供 Fx 计算方块边长喵。

### 4. 右键限制（tapped，L148-154）喵
- `if ((Units.anyEntities(tile) && open) || !origin().timer(timerToggle, 60f)) return;` —— 开门中且门内有实体不能关；右键冷却 60 tick（原点门计时）喵。
- 逻辑控制（control L93-104）：`LAccess.enabled` 非零即开，冷却 80 tick；`sense(LAccess.enabled)` 返回 open?1:0 喵。

### 5. 渲染与碰撞（L132-145）喵
- `draw()` = `Draw.rect(open ? openRegion : region, x, y)` —— 仅贴图二选一，无帧动画喵。
- `checkSolid()` = `!open` —— 开门无碰撞、可通行；关门实心喵。

### 6. 连锁（updateChained，L114-130）喵
- BFS 从自身出发，收集相邻（四邻）同 block 的门，`d.chained` 指向同一 Seq；onProximityAdded/Removed 重算喵。
- 同 block 才会连锁（不同门型不互链）喵。

### 7. 持久化（L162-171）喵
- `write/read` 存 `open` bool 喵。

## 二、开关特效（Fx.java L2594-2612，逐条换算）喵

| 特效 | 参数 | 含义（Mindustry 世界单位，tilesize=8） |
|---|---|---|
| dooropen | Effect(10) | `stroke(e.fout()×1.6)`；`Lines.square(x, y, e.rotation×8/2 + e.fin()×2)` —— 方块轮廓**外扩**（fin 0→1）喵 |
| doorclose | Effect(10) | `stroke(e.fout()×1.6)`；`Lines.square(x, y, e.rotation×8/2 + e.fout()×2)` —— 方块轮廓**内缩**（fout 1→0）喵 |
| dooropenlarge | Effect(10) | `Lines.square(x, y, 8 + e.fin()×2)` —— 2×2 外扩，半边长 8→10 单位喵 |
| doorcloselarge | Effect(10) | `Lines.square(x, y, 8 + e.fout()×2)` —— 2×2 内缩，半边长 10→8 单位喵 |

- `e.rotation = size`（door=1/door-large=2）→ 半边长 = size×8/2 = 4/8 单位 = size/2 格喵。
- 8 单位 = 1 MC 格换算：半边长 size 1 = 0.5 格、size 2 = 1 格；外扩/内缩幅度 2 单位 = 0.25 格；描边 1.6 单位 = 0.2 格，随 fout 淡出喵。
- 白色方块轮廓，描边从 1.6 线性淡出到 0，10 tick（MC 游戏刻）内完成喵。

## 三、实现对照（Blockdustry DoorBlockEntity/DoorBlockEntityRenderer）喵

| 原版 | Blockdustry 实现 |
|---|---|
| open 状态（BE 持久化） | `DoorBlockEntity.open`，NBT `bd_open`，`saveAdditional/loadAdditional` 喵 |
| tapped → configure(!open) | `DoorBlock.useWithoutItem`（服务端）→ `DoorBlockEntity.toggle()`；同队/derelict 才可交互（`canInteract`）喵 |
| Units.anyEntities && open 不能关 | `anyEntityInside()`（忽略 ItemEntity）检查，仅关门时校验喵 |
| timerToggle 60f 冷却 | `lastToggleGameTime` 冷却 60 游戏刻，全连锁门共享时刻喵 |
| 连锁门 BFS | `findChained()` BFS 四邻同 block 同队门，`setOpen` 遍历同切喵 |
| doorSound.at(base) | 服务端 `level.playSound`（注册 SoundEvent `blockdustry:door` + sounds.json 拷 door.ogg）喵 |
| effect()（openfx/closefx） | 记录 `lastToggleGameTime`/`lastToggleOpening`，随 BE 数据同步；渲染器 10 tick 内画方块轮廓喵 |
| draw() 贴图二选一 | `DoorBlockEntityRenderer` 画整块立方体（关=door.png / 开=door-open.png，entityCutoutNoCull 透明门洞）喵 |
| checkSolid() = !open | `DoorBlock.getCollisionShape`：开门返回 `Shapes.empty()`；`getShape` 保持满形状保证开门仍可点选喵 |
| pathfinder.updateTile | 未实现：MC 寻路周期性重算；`sendBlockUpdated` 已触发 ChunkHolder 块广播（含 BE 数据），移动方块碰撞变化依赖 mob 周期性重寻路，属已知简化喵 |

## 四、钛墙（Wall）动画结论喵
- `Wall.java` L19-50：纯静态方块，`drawCached=true`、`drawDynamic=false`（非 flashHit 时）、无动画、无特效喵。
- 无需渲染器/BER，方块模型 + blockstate（2×2 四象限模型）即可喵。

## 五、数据核对（Blocks.java，最终值）喵
| 块 | 类 | size | 配方 | health | openfx/closefx |
|---|---|---|---|---|---|
| titanium-wall L1718-1721 | Wall | 1 | 钛×6 | 110×4=440 | — |
| titanium-wall-large L1723-1727 | Wall | 2 | 钛×24 | 110×4×4=1760 | — |
| door L1785-1788 | Door | 1 | 钛×6+硅×4 | 100×4=400 | dooropen/doorclose |
| door-large L1790-1796 | Door | 2 | 钛×24+硅×16 | 100×4×4=1600 | dooropenlarge/doorcloselarge |
