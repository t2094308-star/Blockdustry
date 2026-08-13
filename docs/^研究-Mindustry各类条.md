# 研究：Mindustry 各类「条状 UI/渲染」的具体颜色与形态喵

> 用途：为 Minecraft 模组「方块工业」的 Jade 联动复刻提供依据喵。
> 研究基于 `D:\Blockdustry\Mindustry`（Mindustry 现役源码，core/src/）喵。
> 关键文件：`mindustry/ui/Bar.java`、`mindustry/graphics/Pal.java`、`mindustry/world/Block.java`、`mindustry/entities/comp/BuildingComp.java`、`mindustry/ui/fragments/HudFragment.java`、`core/assets-raw/sprites/ui/bar.9.png` 与 `bar-top.9.png` 喵。

---

## 0. 一句话结论喵

- 血量条是**纯红** `#ff341c`（`Pal.health`），受击时闪白，**没有红→绿渐变**，背景是深灰喵。
- 合成/进度条是**橙** `#ff8947`（`Pal.ammo`）喵。
- 电量/功率条是**橙** `#ec7b4c`（`Pal.powerBar`），**不是蓝色**喵（蓝色是液体水 `#596ab8` 造成的错觉）喵。
- 物品条是**绿** `#2ea756`（`Pal.items`）喵。
- 液体条用液体自身颜色（`Liquid.barColor()`）喵。
- 这些「条」全部是**纯色填充（无渐变、无描边）**，填充方向**从左到右**，上面叠**白色描边文字**喵。
- Mindustry 核心**没有世界内浮空血条**（建筑损伤靠裂纹+变暗表现），条只出现在：方块信息面板（hover/选中时屏幕顶部的面板）和玩家 HUD 左下角血条喵。

---

## 1. 调色板（`mindustry/graphics/Pal.java`）确切 hex 喵

| 常量 | hex | 用途 |
|---|---|---|
| `Pal.health` | `#ff341c` | 血量条（红）喵 |
| `Pal.ammo` | `#ff8947` | 进度条/钻速条（橙）喵 |
| `Pal.powerBar` | `#ec7b4c` | 功率/电量条（橙）喵 |
| `Pal.power` | `#fbad67` | 电力概念色（激光、闪电等，非条）喵 |
| `Pal.powerLight` | `#fbd367` | 电力发光/图标（黄）喵 |
| `Pal.items` | `#2ea756` | 物品条（绿）喵 |
| `Pal.lightOrange` | `#f68021` | 热量条（HeatCrafter/NuclearReactor 的热条）喵 |
| `Pal.accent` | `#ffd37f` | HUD 护盾条/强调色（黄）喵 |
| `Pal.bar` | `#708090`（`Color.slate`） | 已定义但核心未使用，供 mod 用喵 |
| `Pal.darkishGray` | `#4d4d4d`（RGB 0.3,0.3,0.3） | HUD 血条背景喵 |
| `Pal.darkerGray` | `#333333`（RGB 0.2,0.2,0.2） | 其他 HUD 背景喵 |
| 方块面板条背景 | `#1a1a1a`（`Draw.colorl(0.1f)`） | Bar 组件的底色喵 |

---

## 2. UI 条组件 `Bar`（`mindustry/ui/Bar.java`）渲染机制喵

方块信息面板里所有条都是 `Bar extends Element` 组件喵。`draw()` 流程：

1. **背景**：`Tex.bar`（白色 9-patch 贴图 `bar.9.png`，内区纯白 `#ffffff`）用 `Draw.colorl(0.1f)` 染色 → 视觉为深灰 `#1a1a1a`，叠加 `Draw.alpha(parentAlpha)` 喵。
2. **填充**：`Tex.barTop`（`bar-top.9.png`，上 68% 纯白、下 26% 浅灰蓝 `#c1c4cf` 的竖向渐变贴图）用 `Draw.color(color, blinkColor, blink)` 染色，宽 = `width * value`（**从左到右**填），值太小用 scissor 裁剪喵。
3. **闪烁**：`blink = Mathf.lerpDelta(blink, 0f, 0.2f)` 衰减；受击/掉值时 `blink=1`，颜色向 `blinkColor` 混合（血量条 `blinkColor=Color.white` → 闪白）喵。
4. **数值动画**：`value = Mathf.lerpDelta(value, computed, 0.15f)`，条会平滑趋近目标值喵。
5. **描边**：默认 `outlineRadius=0`，**无描边**；只有调用 `.outline(color, stroke)` 才画（Boss 血条/Core 血条用 `.outline(new Color(0,0,0,0.6f), 7f)`）喵。
6. **文字**：`Fonts.outline`（带黑描边的字体），白色 `#ffffff`，水平居中、垂直居中叠在条上方，显示条的名称（如 "Health"、"Progress"、功率数值等）喵。
7. **尺寸**：信息面板里 `growX().height(18f).pad(4)`，即高度 18px、四周 padding 4、宽度占满喵。

---

## 3. 血量条（Health）喵

### 3.1 方块信息面板血量条（`world/Block.java` line 697）喵

```java
addBar("health", entity -> new Bar("stat.health", Pal.health, entity::healthf).blink(Color.white));
```

- 颜色：`Pal.health` = **`#ff341c`**（亮红），恒定不变喵。
- 受击闪烁：`.blink(Color.white)`，掉血时闪白后衰减喵。
- 背景：`#1a1a1a` 深灰喵。
- 文字：`Fonts.outline` 白色，显示 "Health" 喵。
- **无红→绿渐变**（常见误解）喵。

### 3.2 玩家 HUD 左下血条（`HudFragment.java` line 1026，SideBar 类）喵

- 填充色：`Pal.health` `#ff341c`，受击闪白（`lerp(Color.white, blink)`）喵。
- 背景：`Pal.darkishGray` = **`#4d4d4d`**（半透明深灰），`drawBack=true` 时先画背景喵。
- 形态：用 `Fill.quad` 画的**平行四边形/菱形**（六边形图标两侧的斜边造型），不是直角矩形喵。
- 尺寸：宽 40f、高占满左侧栏（`growY`）喵。
- 护盾条用 `Pal.accent` `#ffd37f`、无背景（`drawBack=false`）喵。

### 3.3 Boss/Core 血条（`HudFragment.java` line 642-650）喵

- `Pal.health` 红 + `.blink(Color.white)` + `.outline(new Color(0,0,0,0.6f), 7f)` 黑描边喵。
- 高 60f、宽 320f，顶部居中喵。

### 3.4 世界内渲染（重要修正）喵

- 本版本**没有**世界浮空血条，`drawHealth`/`drawProgress`/`drawBar` 均不存在喵。
- 建筑受损的世界表现是**裂纹贴图 + 整体变暗**：`BuildingComp.java` line 1240-1244，`Draw.colorl(0.2f, 0.1f + (1f - healthf()) * 0.6f)`，越残越暗黑喵。
- 单位血量也没有世界浮条，只在 HUD/信息面板显示喵。

---

## 4. 进度条（Progress）喵

- **重要修正**：现役 `GenericCrafter`（通用工厂）**并没有注册 progress 条**，`setBars()` 只处理液体输出条喵。
- 真正带 progress 条（全部用 `Pal.ammo` = **`#ff8947`**）的方块：
  - `UnitFactory` line 121：`new Bar("bar.progress", Pal.ammo, e::fraction)` 喵。
  - `UnitAssembler` line 128：`Pal.ammo`，`e.progress` 喵。
  - `Reconstructor` line 71：`Pal.ammo`，`entity::fraction` 喵。
  - `BlockProducer` line 74：`Pal.ammo`，`entity.progress / recipe().buildTime` 喵。
  - `PayloadDeconstructor` line 48：`Pal.ammo`，`e.progress` 喵。
  - `LaunchPad` line 70：`Pal.ammo`，发射冷却喵。
- 钻速条（也是 `Pal.ammo` 橙）：`Drill` line 118、`BeamDrill` line 94、`WallCrafter` line 72、`SolidPump` line 53，名字显示钻速/效率数字，填充用 `e.warmup()` 喵。
- 形态：纯橙填充、深灰 `#1a1a1a` 底、白描边文字、左→右、无渐变喵。

---

## 5. 电量/功率条（Power）喵

- **重要修正**：功率条是**橙色** `Pal.powerBar` = **`#ec7b4c`**，**不是蓝色系**喵。
- 基础条（`Block.java` line 703-708）：
  - buffered（电池类）：名字 `bar.poweramount`（显示已存电量数字），填充 `entity.power.status`（0~1），颜色 `Pal.powerBar` 喵。
  - 非 buffered（电网）：名字 `bar.power`，填充 `entity.power.status`，颜色 `Pal.powerBar` 喵。
- 发电机类（`PowerGenerator` line 75、`ImpactReactor` line 48）：覆盖 power 条，名字 `bar.poweroutput`（显示输出功率数值），填充 `entity.productionEfficiency`，颜色 `Pal.powerBar` 喵。
- `PowerNode` line 119-138：`bar.powerlines` / `bar.powerbalance` / `bar.powerstored` 等条，全部 `Pal.powerBar` 喵。
- `PowerDiode` line 33-34：input/output 条，`Pal.powerBar` 喵。
- `Accelerator` line 108：电量缓冲条，`Pal.powerBar` 喵。
- 「蓝色」的真正来源：液体条里的水 `Liquid water.color` = `#596ab8`（蓝），以及 `Pal.techBlue` `#8ca9e8`（仅用于蓝图预览等非条 UI）喵。

---

## 6. 其他条喵

| 条 | 颜色 | 来源 |
|---|---|---|
| 物品条 items | `Pal.items` `#2ea756` 绿 | `Block.java` line 712，仅 `hasItems && configurable` 时出现，名字 `bar.items` 显示物品数喵 |
| 液体条 liquid | 液体自身颜色 `Liquid.barColor()`（透明度强制 1） | `Block.java` line 679-693，名字显示液体名，填充 = 量/容量喵 |
| 热量条 heat | `Pal.lightOrange` `#f68021` | `HeatCrafter` line 31、`NuclearReactor` line 86，名字显示热量百分比喵 |
| 护盾条 shield | `Pal.accent` `#ffd37f` | HUD SideBar，无背景喵 |
| 加载条（Loading） | `Pal.accent` 掺黑 50% / 警告红 `Pal.breakInvalid` `#d44b3d` | `LoadRenderer.java`，与游戏内条无关喵 |

---

## 7. 复刻到 Minecraft Jade 的建议喵

### 7.1 通用形态喵

- 所有条都用 **color 纯色 + 深灰背景**、**无渐变**（若要立体感可加极浅同色系 color2 做微渐变）喵。
- 填充方向统一**从左到右**，比例 = 数值（血 `health/maxHealth`、电 `stored/capacity` 或 `status`、进度 `progress/craftTime`）喵。
- 文字白 `#ffffff`，居中，显示数值/名称喵。
- 背景建议 `#1a1a1a`（深灰，若在浅色主题里可用更浅 `#808080` 保证对比）喵。

### 7.2 血量条 → Jade（建筑/实体血条）喵

- `color`：`#ff341c`（Pal.health）喵。
- `color2`：`#ffffff`（模拟受击闪白，或不用）喵。
- `textColor`：`#ffffff` 喵。
- 背景：`#1a1a1a`（Jade 血条若带描边可仿 `.outline(黑60%透明)`）喵。
- 不要做红→绿渐变，保留纯红身份喵。

### 7.3 进度条 → Jade（合成进度）喵

- `color`：`#ff8947`（Pal.ammo 橙）喵。
- `color2`：略深橙 `#d97a3d` 做微渐变可选喵。
- `textColor`：`#ffffff`，文字如 "进度 63%" 喵。
- 背景：`#1a1a1a` 喵。

### 7.4 电量条 → Jade（电力/能量）喵

- `color`：`#ec7b4c`（Pal.powerBar 橙）——**不要用蓝色**喵。
- `color2`：略浅橙 `#ff9a6b` 可选喵。
- `textColor`：`#ffffff`，文字如 "电力 500/1000" 喵。
- 背景：`#1a1a1a` 喵。

### 7.5 物品/液体条 → Jade 喵

- 物品条：`color` `#2ea756`（绿），背景 `#1a1a1a` 喵。
- 液体条：`color` 用液体颜色（水蓝 `#596ab8`、油等按方块内容），背景 `#1a1a1a` 喵。

---

## 8. 源文件索引喵

- `mindustry/ui/Bar.java` — 条组件全部渲染逻辑喵。
- `mindustry/graphics/Pal.java` — 全部颜色常量喵。
- `mindustry/world/Block.java`（setBars line 696-753）— 基础 health/power/items/liquid 条注册喵。
- `mindustry/entities/comp/BuildingComp.java`（displayBars line 1668、drawCracks line 1240）— 面板条装配与裂纹喵。
- `mindustry/ui/fragments/HudFragment.java`（SideBar line 921-995、血条 line 1026、Boss条 line 642）— HUD 血条喵。
- `core/assets-raw/sprites/ui/bar.9.png`、`bar-top.9.png` — 条底/条顶贴图喵。
