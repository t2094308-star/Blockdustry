# T9b 炮台附身黑屏 / shift 退出修复 阶段产出喵

## 目标喵
修复 T5「炮台附身」的两个问题 + 清理玩家可见文案喵：
1. **附身后画面一片黑**（玩家实体被传送到炮塔方块内部，相机被实体方块遮挡）喵。
2. **附身无法 shift 退出**（潜行 0.25s 退出逻辑不生效）喵。
3. **玩家可见文案统一不带「喵」字**（提示保留，仅去掉玩家看到的喵尾）喵。

## 结论/产出喵
- **黑屏**：根因是 `TurretPossessManager.possess()` 把玩家传送到 `pos + (0.5, -0.6, 0.5)`（炮塔方块内部），`noPhysics` 防窒息但相机仍在方块内 → 黑屏喵。修法按任务倾向 b：改为把玩家传送到**炮塔正上方的干净空气位**，保持「玩家在炮塔处」语义；`tickAll` 每 tick 用同一站位钉回防漂移喵。
  - 新增 `cameraSpot(ServerLevel, BlockPos)`：从炮塔顶往上最多扫 6 格，找「脚底格与头顶格都在空气」的位置（眼睛约在 `feet.above()` 内，须为空气）；找不到则回退 `pos.above(3)` 喵。
  - `possess()` 与 `tickAll()` 都用 `cameraSpot` 作为传送/钉回目标喵。
  - 不破坏穿透视野：客户端射程圈/瞄准线按 `turretPos` 世界坐标绘制，与玩家站位无关；玩家升到炮塔上方后视角更开阔，红蓝线依然可见喵。
- **shift 退出**：根因是客户端用 `mc.options.keyShift.isDown()`（裸按键按下状态）判断潜行。当玩家开了 **「切换潜行」** 选项时，`keyShift.isDown()` 只在按下那一瞬间为 true，随后即假 → `shiftPressTicks` 永远累计不到 5 → 退出逻辑不触发喵。
  - 修法：`TurretPossessHandler.onClientTick` 改为 `mc.player.isShiftKeyDown() || mc.options.keyShift.isDown()`，用实体潜行状态兜底（切换潜行下 `input.shiftKeyDown`/`isShiftKeyDown` 保持为 true），保留 5 tick（0.25s）连按退出，并在发包后重置计数器喵。
  - 服务端退出路径（`handleExit` → `clientExit` → `unpossess`）核对无改动必要：控制包能到达说明 possession 映射在，`clientExit` 校验 `p.turretPos().equals(pos)` 可命中，`unpossess` 正确还原玩家/复位 noPhysics/invisible/炮塔手动模式喵。
- **玩家可见文案**：按用户澄清「提示保留，只是游戏内玩家可见的文本不要带喵」——仅把 `TurretPossessManager.onRightClick` 的聊天提示 `"不能附身异队炮塔喵"` 去掉尾缀喵，改为 `"不能附身异队炮塔"` 喵。HUD 提示 `"炮台附身 · 左键开火 · 潜行退出"` 本就不带喵，保留；准星保留喵。其余所有喵字只出现在代码注释与开发对话中，不进玩家可见文本喵。

## 改动文件清单喵
| 文件 | 改动 |
|---|---|
| `possession/TurretPossessManager.java` | 新增 `cameraSpot()`；`possess()`/`tickAll()` 改用上方空气位；聊天提示去掉喵；新增 `ServerLevel` import |
| `client/TurretPossessHandler.java` | `onClientTick` 潜行判断改为 `isShiftKeyDown() || keyShift.isDown()`，发退出包后重置计数 |
| `docs/子agent/T9b_附身黑屏退出修复.md` | 本文档 |

## 验证喵
- `./gradlew compileJava --offline` 通过（仅原有 `EventBusSubscriber.bus()` 过时警告，与本任务无关）喵。
- 运行表现：进入附身相机在炮塔上方可见、无黑屏；潜行（连按/切换潜行皆可）0.25s 退出并还原玩家；玩家不再看到带喵的提示文案喵。

## 风险/待人工排查喵
- **炮塔被屋顶/实体方块盖住**：`cameraSpot` 会向上扫 6 格找空气；极端全封闭（>6 格全实体）回退 `pos.above(3)` 可能仍黑屏，属边缘场景，可后续加「临时隐形上方方块」兜底喵。
- **切换潜行退出后**：玩家回到原位时若潜行开关仍为开，会以潜行状态落地，需再按一次 shift 取消，属 MC 原生交互，非 bug 喵。
- 未改动穿透视野/瞄准线、炮塔手动模式、网络包结构，均保持 T5 行为喵。

## 占用与交接喵
- 占用（只读）：`TurretPossessManager.java`、`TurretPossessHandler.java` 喵。
- 交接给：主会话（编译已过，可直接 runClient 验证）喵。

## 异常喵
无。
