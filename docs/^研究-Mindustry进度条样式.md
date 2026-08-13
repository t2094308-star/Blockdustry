# 研究：Mindustry 合成/制作进度条样式（Jade 复刻参考）

> 研究对象：`D:\Blockdustry\Mindustry`（v8 时代 ECS 重构版，git 共 21027 commits）喵。
> 结论先给：**当前版本没有 `DrawProgressBar.java`，它已在 v8 重写中被移除**；现役的合成/制作进度条是 UI 层 `Bar` 组件，进度条颜色是 `Pal.ammo` `#ff8947` 喵。

---

## 一、关键前提：本仓库没有 DrawProgressBar

- `core/src/mindustry/world/draw/` 目录下没有 `DrawProgressBar.java`，现有抽屉类为 `DrawDefault`、`DrawFlame`、`DrawHeatRegion` 等共 38 个，唯独没有进度条抽屉喵。
- 全仓库 grep `DrawProgressBar` / `drawProgress` / `drawBar` 均无命中喵。
- git 全历史（`git log --all -- core/src/mindustry/world/draw/DrawProgressBar.java`）为空，说明该文件从没出现在这条主干历史里喵。
- 旧版（v6/v7）的 `DrawProgressBar`（`world.draw` 包下、`DrawBlock` 子类）用于在方块上画世界内合成进度条，其默认 `barColor = Pal.accent`；该说法来自旧版记忆，**无法在本仓库核实**喵。
- 影响：不能在本仓库里找到「合成类机器进度条」的绘制类，只能研究当前版本实际存在的进度条渲染（UI `Bar` + `Pal` 颜色常量）喵。

## 二、当前版本实际存在的「进度条」：UI `Bar` 组件

`core/src/mindustry/ui/Bar.java` 是方块信息面板（点击/选中方块时左上角出现的条）的渲染类喵。它也是游戏里唯一会显示「制作/合成进度」的地方喵。

### 2.1 形态细节（来自 `Bar.draw()`）

- **背景**：`Draw.colorl(0.1f)` → RGB(0.1, 0.1, 0.1) = **#1a1a1a** 深灰，用 `Tex.bar` 圆角贴图铺满整个条喵。
- **填充**：`Tex.barTop` 贴图，从**左 → 右**填充，填充宽 = 总宽 × value，即进度值越大越往右延展喵。
- **颜色**：固定用构造传入的 `color`；值下降时 `blink` 短暂向 `blinkColor` 闪白，平时**不随 progress/warmup 变色**喵。
- **文字**：白字 + 深色描边（`Fonts.outline`），居中显示条名（如 "Build Progress"）喵。
- **动画**：`value` 每帧按 0.15 系数 lerp 逼近真实值，`blink` 按 0.2 衰减——显示上是平滑过渡、掉值闪一下喵。
- **可选边框**：`outline(Color, stroke)` 可画外描边，默认不画喵。
- **高度**：由 UI 布局决定，视觉上是一根细条（约 4~5px）喵。

### 2.2 颜色与进度值完全解耦

`Bar` 的颜色只取决于创建时传入的 `Color` 喵。`GenericCrafter` 系机器（石墨压缩机等）本版本**既不在世界内画进度条，也不在 setBars 里注册 progress 条**，所以它们没有「随进度变色的条」喵。

## 三、Pal 颜色常量（确切 hex，`core/src/mindustry/graphics/Pal.java`）

| 常量 | 定义 | hex | 用途 |
|---|---|---|---|
| `accent` | `Color.valueOf("ffd37f")` | **#ffd37f** | UI 强调色/选中色/通用 accent |
| `stat` | `Color.valueOf("ffd37f")` | #ffd37f | 与 accent 相同 |
| `accentBack` | `Color.valueOf("d4816b")` | #d4816b | accent 的深色底 |
| `ammo` | `Color.valueOf("ff8947")` | **#ff8947** | **当前版本合成/制作进度条实际用色** |
| `powerBar` | `Color.valueOf("ec7b4c")` | #ec7b4c | 电力条 |
| `power` | `Color.valueOf("fbad67")` | #fbad67 | 电力辅助色 |
| `health` | `Color.valueOf("ff341c")` | #ff341c | 血量条 |
| `heal` | `Color.valueOf("98ffa9")` | #98ffa9 | 治疗条 |
| `items` | `Color.valueOf("2ea756")` | #2ea756 | 物品条 |
| `gray` | `Color.valueOf("454545")` | #454545 | 通用灰 |
| `bar` | `Color.slate` | **#708090** | arc 的 `Color.slate = new Color(0x708090ff)`（青灰板岩色），本仓库 core 中未使用 |

> 注：`Pal.bar = Color.slate`，`slate` 定义在 arc 库 `arc.graphics.Color`（arc 版本 hash `c2e2d470c8`）：`public static final Color slate = new Color(0x708090ff)` 喵。

## 四、具体到 graphite-press / GenericCrafter

- `graphite-press` 定义在 `core/src/mindustry/content/Blocks.java:1040`：`new GenericCrafter("graphite-press")`，`size = 2`、`craftTime = 90f`、`drawer` 用默认 `DrawDefault`，未覆盖喵。
- `GenericCrafter`（`core/src/mindustry/world/blocks/production/GenericCrafter.java:50`）默认 `public DrawBlock drawer = new DrawDefault()`，`draw()` 直接调 `drawer.draw(this)`，`DrawDefault` 只画方块 region，**不画任何进度条**喵。
- `GenericCrafter.setBars()`（同文件 82 行起）只给液体输出加条，**不注册 `progress` 条**喵。
- 本版本全工程凡显示 `bar.progress`（"Build Progress"）的进度条，一律用 **`Pal.ammo` #ff8947**：`UnitFactory.java:121`、`Reconstructor.java:71`、`UnitAssembler.java:128`、`BlockProducer.java:74`、`PayloadDeconstructor.java:48`、`LaunchPad.java:70` 喵。

## 五、Jade（1.21-neoforge）复刻建议

参考本地 Jade 仓库 `origin/1.21-neoforge` 分支的 `SimpleProgressStyle` / `ProgressStyle` / `ProgressElement` 喵。

### 5.1 API 形状

```java
ProgressStyle style = new SimpleProgressStyle()
    .color(0xFF8947)                       // color(int) 等价于 color(c, c)
    .color(0xFF8947, 0xFF8947)             // color(int,int) 主色 + 副色（副色画横纹）
    .textColor(0xFFFFFFFF)                 // 可选：覆盖文字颜色
    .direction(ScreenDirection.RIGHT)      // 左→右填充
    .fitContentX(true).fitContentY(true);
ProgressView view = new ProgressView(part, text, style, BoxStyle.nestedBox());
tooltip.add(JadeUI.progress(view));
```

### 5.2 复刻要点

- **主色 `color`**：用 `#ff8947`（`Pal.ammo`）最贴近当前版合成进度条喵。若要复刻旧版 DrawProgressBar 的 accent 观感，用 `#ffd37f`（`Pal.accent`）喵。
- **`color2` 建议**：Jade 的 `color2` 在 `color != color2` 时会在填充区画 2px 交替横纹（加载条纹感）；Mindustry 的条是**纯色**，没有横纹，所以 `color2` 传与 `color` 相同的值即可（直接用 `color(int)` 最省事）喵。
- **渐变**：Jade 的 `SimpleProgressStyle.render` 默认画竖向渐变（顶部 0.7×明度的 lighter → 中间主色 → 底部 lighter），恰好复刻 Mindustry 填充贴图「上缘略亮」的观感，无需额外处理喵。
- **`textColor`**：Mindustry 是白字+描边；Jade 自动文字色逻辑是「副色 HSV 明度 > 0.75 用黑字，否则用主题白字」，#ff8947 明度约 0.55 会自动选白字，可不设；为稳妥可显式 `textColor(0xFFFFFFFF)` 喵。
- **背景/边框**：Mindustry 的条底色是 #1a1a1a 深灰。Jade 默认 `BoxStyle.nestedBox()` 是 1px 灰边（0xFF808080）+ 透明底，跟 Mindustry 的深底不一致；想要完全一致可自定义 BoxStyle（`backgroundColor = 0xFF1A1A1A`）或接受 Jade 默认观感喵。
- **动画**：Jade 的 `ProgressElement` 自带进度平滑跟踪（`ProgressTrackInfo`），与 Mindustry 的 0.15 lerp 平滑非常接近；构造时 `canDecrease(true)` 对应 Mindustry「掉值闪白」的可下降行为喵。
- **文字**：Mindustry 面板条有居中条名；Jade 用 `ProgressView.text` 传入 `Component`（如 "Build Progress" 或百分比）喵。
- **尺寸**：Jade `ProgressElement` 默认高：无文字 8px、有文字 14px；Mindustry 视觉上是细条（约 4~5px 高），可把高度调小更贴近原版喵。

### 5.3 一段最小示例（1.21-neoforge 版 Jade）

```java
snownee.jade.api.ui.ProgressStyle style = new snownee.jade.impl.ui.SimpleProgressStyle()
        .color(0xFF8947)
        .textColor(0xFFFFFFFF)
        .direction(snownee.jade.api.ui.ScreenDirection.RIGHT);
snownee.jade.api.view.ProgressView view = new snownee.jade.api.view.ProgressView(
        snownee.jade.api.view.ProgressView.Part.of(progress),     // 0..1 合成进度
        Component.translatable("blockdustry.jade.crafting"),       // 可选条名
        style,
        snownee.jade.api.ui.BoxStyle.nestedBox());
tooltip.add(snownee.jade.api.ui.JadeUI.progress(view));
```

## 六、结论速览

- 进度条主色：**#ff8947**（当前版合成进度 `Pal.ammo`）喵。
- 备选 accent 色：**#ffd37f**（旧版 DrawProgressBar.barColor 默认值）喵。
- 背景色：#1a1a1a 深灰，Jade 里用 BoxStyle 定制喵。
- 形态：左→右实心填充、无渐变、无横纹、白字居中、细条、掉值闪白、平滑动画喵。
- 本仓库没有 DrawProgressBar.java，无法核实其 width=4、边框、warmup 变色等细节喵。
